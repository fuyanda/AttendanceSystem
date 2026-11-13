#!/usr/bin/env python3
"""用人脸底库初始化，并用测试图做 1:N 匹配，输出姓名与耗时。

默认：
  底库目录: ./data/gallery
  测试目录: ./data/probe
  服务地址: http://127.0.0.1:8080/api/v1

底库文件名约定: {external_id}_{姓名}.jpg|png|webp
"""

from __future__ import annotations

import argparse
import mimetypes
import re
import statistics
import sys
import time
from pathlib import Path

import httpx

DEFAULT_FACE_DIR = Path("./data/gallery")
DEFAULT_TEST_DIR = Path("../face-admin/uploadPath/face")
DEFAULT_BASE_URL = "http://127.0.0.1:8080/api/v1"

IMAGE_SUFFIXES = {".jpg", ".jpeg", ".png", ".webp"}
NAME_PATTERN = re.compile(r"^(?P<external_id>.+?)_(?P<name>.+)$")


def list_images(directory: Path) -> list[Path]:
    return sorted(
        p for p in directory.iterdir() if p.is_file() and p.suffix.lower() in IMAGE_SUFFIXES
    )


def parse_face_filename(path: Path) -> tuple[str, str]:
    """从 `100_胡晟梵.jpg` 解析 (external_id, name)。"""
    stem = path.stem
    match = NAME_PATTERN.match(stem)
    if not match:
        raise ValueError(f"文件名不符合 {{id}}_{{姓名}} 约定: {path.name}")
    return match.group("external_id"), match.group("name")


def content_type_for(path: Path) -> str:
    guessed, _ = mimetypes.guess_type(path.name)
    if guessed in {"image/jpeg", "image/png", "image/webp"}:
        return guessed
    suffix = path.suffix.lower()
    if suffix in {".jpg", ".jpeg"}:
        return "image/jpeg"
    if suffix == ".png":
        return "image/png"
    if suffix == ".webp":
        return "image/webp"
    return "application/octet-stream"


def ensure_healthy(client: httpx.Client) -> dict:
    resp = client.get("/health")
    resp.raise_for_status()
    body = resp.json()
    if body.get("status") != "ok":
        raise RuntimeError(f"服务不可用: {body}")
    return body


def load_person_index(client: httpx.Client) -> dict[str, dict]:
    """external_id -> person。"""
    persons = client.get("/persons").json()
    return {
        person["external_id"]: person
        for person in persons
        if person.get("external_id")
    }


def enroll_faces(client: httpx.Client, face_dir: Path) -> tuple[int, int, int]:
    images = list_images(face_dir)
    if not images:
        raise FileNotFoundError(f"底库目录无图片: {face_dir}")

    created = 0
    enrolled = 0
    skipped = 0
    failed = 0
    person_index = load_person_index(client)

    print(f"\n=== 初始化底库: {face_dir} ({len(images)} 张) ===")
    for index, path in enumerate(images, start=1):
        try:
            external_id, name = parse_face_filename(path)
        except ValueError as exc:
            print(f"[{index}/{len(images)}] 跳过: {exc}")
            failed += 1
            continue

        person = person_index.get(external_id)
        if person is None:
            created_resp = client.post(
                "/persons",
                json={"name": name, "external_id": external_id},
            )
            if created_resp.status_code == 409:
                person_index = load_person_index(client)
                person = person_index.get(external_id)
                if person is None:
                    print(f"[{index}/{len(images)}] 创建冲突后仍找不到人员: {path.name}")
                    failed += 1
                    continue
            else:
                created_resp.raise_for_status()
                person = created_resp.json()
                person_index[external_id] = person
                created += 1
                print(f"[{index}/{len(images)}] 创建人员: {name} (id={person['id']})")
        else:
            print(
                f"[{index}/{len(images)}] 人员已存在: {person['name']} "
                f"(external_id={external_id}, id={person['id']})"
            )

        person_id = person["id"]
        if person.get("face_count", 0) > 0:
            print(f"  已有人脸，跳过录入: {path.name}")
            skipped += 1
            continue

        existing = client.get("/faces", params={"person_id": person_id}).json()
        if existing:
            person["face_count"] = len(existing)
            print(f"  已有 {len(existing)} 张人脸，跳过录入: {path.name}")
            skipped += 1
            continue

        with path.open("rb") as fh:
            enroll_resp = client.post(
                f"/persons/{person_id}/faces",
                files={"file": (path.name, fh, content_type_for(path))},
            )
        if enroll_resp.status_code >= 400:
            print(f"  录入失败 {path.name}: {enroll_resp.status_code} {enroll_resp.text}")
            failed += 1
            continue

        face_id = enroll_resp.json()["id"]
        person["face_count"] = 1
        enrolled += 1
        print(f"  录入成功: {path.name} -> face_id={face_id}")

    health = ensure_healthy(client)
    print(
        f"底库完成: 新建人员={created}, 新录入={enrolled}, 跳过={skipped}, 失败={failed}; "
        f"库内人员={health['enrolled_persons']}, 人脸={health['enrolled_faces']}"
    )
    return enrolled, skipped, failed


def search_tests(client: httpx.Client, test_dir: Path) -> int:
    images = list_images(test_dir)
    if not images:
        raise FileNotFoundError(f"测试目录无图片: {test_dir}")

    print(f"\n=== 匹配测试: {test_dir} ({len(images)} 张) ===")
    print(f"{'序号':<4} {'测试图':<28} {'匹配姓名':<12} {'相似度':>8} {'耗时(ms)':>10} {'状态'}")
    print("-" * 84)

    elapsed_ms_list: list[float] = []
    unique_similarities: list[float] = []
    matched = 0
    unique_matched = 0
    multi_matched = 0
    no_face = 0
    no_match = 0
    errors = 0

    for index, path in enumerate(images, start=1):
        started = time.perf_counter()
        try:
            with path.open("rb") as fh:
                resp = client.post(
                    "/search",
                    files={"file": (path.name, fh, content_type_for(path))},
                )
            elapsed_ms = (time.perf_counter() - started) * 1000
            elapsed_ms_list.append(elapsed_ms)

            if resp.status_code >= 400:
                errors += 1
                print(
                    f"{index:<4} {path.name:<28} {'-':<12} {'-':>8} "
                    f"{elapsed_ms:>10.1f} HTTP {resp.status_code}"
                )
                continue

            body = resp.json()
            if not body.get("query_face_detected"):
                no_face += 1
                print(
                    f"{index:<4} {path.name:<28} {'-':<12} {'-':>8} "
                    f"{elapsed_ms:>10.1f} 未检测到人脸"
                )
                continue

            matches = body.get("matches") or []
            if not matches:
                no_match += 1
                print(
                    f"{index:<4} {path.name:<28} {'-':<12} {'-':>8} "
                    f"{elapsed_ms:>10.1f} 无匹配"
                )
                continue

            top = matches[0]
            matched += 1
            name = top.get("person_name") or "-"
            similarity = float(top.get("similarity") or 0.0)
            is_unique = len(matches) == 1
            if is_unique:
                unique_matched += 1
                unique_similarities.append(similarity)
            else:
                multi_matched += 1
            status = "唯一匹配" if is_unique else "多候选"
            print(
                f"{index:<4} {path.name:<28} {name:<12} {similarity:>8.4f} "
                f"{elapsed_ms:>10.1f} {status}"
            )

            if len(matches) > 1:
                extras = ", ".join(
                    f"{m['person_name']}({m['similarity']:.4f})" for m in matches[1:3]
                )
                print(f"     其他候选: {extras}")
        except Exception as exc:  # noqa: BLE001 - 单张失败不影响后续
            elapsed_ms = (time.perf_counter() - started) * 1000
            elapsed_ms_list.append(elapsed_ms)
            errors += 1
            print(
                f"{index:<4} {path.name:<28} {'-':<12} {'-':>8} "
                f"{elapsed_ms:>10.1f} 异常: {exc}"
            )

    print("-" * 84)
    print(
        f"汇总: 成功匹配={matched}(唯一={unique_matched}, 多候选={multi_matched}), "
        f"未检测到人脸={no_face}, 无匹配={no_match}, 错误={errors}, 总计={len(images)}"
    )
    if unique_similarities:
        print(
            f"唯一匹配相似度: 最小={min(unique_similarities):.4f}, "
            f"最大={max(unique_similarities):.4f}, "
            f"平均={statistics.mean(unique_similarities):.4f}, "
            f"样本数={len(unique_similarities)}"
        )
    else:
        print("唯一匹配相似度: 无唯一匹配样本")
    if elapsed_ms_list:
        print(
            f"耗时: 平均={statistics.mean(elapsed_ms_list):.1f}ms, "
            f"中位={statistics.median(elapsed_ms_list):.1f}ms, "
            f"最小={min(elapsed_ms_list):.1f}ms, "
            f"最大={max(elapsed_ms_list):.1f}ms"
        )
    return 0 if errors == 0 else 1


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="人脸底库初始化 + 匹配耗时测试")
    parser.add_argument("--face-dir", type=Path, default=DEFAULT_FACE_DIR, help="底库图片目录")
    parser.add_argument("--test-dir", type=Path, default=DEFAULT_TEST_DIR, help="测试图片目录")
    parser.add_argument("--base-url", default=DEFAULT_BASE_URL, help="face-service API 前缀")
    parser.add_argument(
        "--skip-enroll",
        action="store_true",
        help="跳过底库初始化，仅做匹配测试",
    )
    parser.add_argument(
        "--enroll-only",
        action="store_true",
        help="仅初始化底库，不做匹配测试",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    if args.skip_enroll and args.enroll_only:
        print("不能同时指定 --skip-enroll 与 --enroll-only", file=sys.stderr)
        return 2

    timeout = httpx.Timeout(connect=10.0, read=180.0, write=180.0, pool=10.0)
    with httpx.Client(base_url=args.base_url, timeout=timeout) as client:
        health = ensure_healthy(client)
        print(f"服务状态: {health}")

        if not args.skip_enroll:
            if not args.face_dir.is_dir():
                print(f"底库目录不存在: {args.face_dir}", file=sys.stderr)
                return 1
            _, _, failed = enroll_faces(client, args.face_dir)
            if failed:
                print(f"警告: 有 {failed} 张底库图片处理失败", file=sys.stderr)

        if args.enroll_only:
            return 0

        if not args.test_dir.is_dir():
            print(f"测试目录不存在: {args.test_dir}", file=sys.stderr)
            return 1
        return search_tests(client, args.test_dir)


if __name__ == "__main__":
    raise SystemExit(main())
