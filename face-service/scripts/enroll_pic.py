#!/usr/bin/env python3
"""将 pic/ 目录下的人脸照片注册到本地人脸库（人员已有人脸则跳过）。"""

from __future__ import annotations

import sys
from pathlib import Path

import httpx

ROOT = Path(__file__).resolve().parents[2]
PIC_DIR = ROOT / "pic"
BASE_URL = "http://127.0.0.1:8080/api/v1"


def main() -> int:
    images = sorted(PIC_DIR.glob("*.png")) + sorted(PIC_DIR.glob("*.jpg"))
    if not images:
        print(f"未找到图片: {PIC_DIR}", file=sys.stderr)
        return 1

    with httpx.Client(base_url=BASE_URL, timeout=120.0) as client:
        health = client.get("/health")
        health.raise_for_status()
        print("服务状态:", health.json())

        for path in images:
            name = f"person_{path.stem}"
            external_id = path.stem
            created = client.post(
                "/persons",
                json={"name": name, "external_id": external_id},
            )
            if created.status_code == 409:
                persons = client.get("/persons").json()
                person = next(p for p in persons if p["external_id"] == external_id)
                person_id = person["id"]
                print(f"人员已存在: {name} id={person_id}")
            else:
                created.raise_for_status()
                person_id = created.json()["id"]
                print(f"创建人员: {name} id={person_id}")

            existing = client.get("/faces", params={"person_id": person_id}).json()
            if existing:
                print(f"已有 {len(existing)} 张人脸，跳过录入: {path.name}")
                continue

            with path.open("rb") as fh:
                enroll = client.post(
                    f"/persons/{person_id}/faces",
                    files={"file": (path.name, fh, "image/png")},
                )
            if enroll.status_code >= 400:
                print(f"录入失败 {path.name}: {enroll.status_code} {enroll.text}")
                return 1
            print(f"录入成功: {path.name} -> face_id={enroll.json()['id']}")

        health = client.get("/health").json()
        print("注册完成:", health)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
