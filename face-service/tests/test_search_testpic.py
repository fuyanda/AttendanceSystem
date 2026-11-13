"""对 testpic/ 做人脸搜索回归测试。

约定（按特征相似度标定）：
- pic/21.png 注册为 person_21，testpic/22.png 应为同人（戴眼镜变体）
- pic/25.png 注册为 person_25，testpic/23.png 应为同人
- pic/24.png 注册为 person_24，作为负样本干扰项
"""

from __future__ import annotations

from pathlib import Path

import httpx
import pytest

ROOT = Path(__file__).resolve().parents[2]
TESTPIC_DIR = ROOT / "testpic"
BASE_URL = "http://127.0.0.1:8080/api/v1"

# query 文件名 -> 期望命中的人员 external_id（按实际特征相似度标定）
EXPECTED_MATCHES = {
    "22.png": "21",  # 同人（戴眼镜变体）
    "23.png": "25",  # 同人（不同拍摄）
}

MIN_SIMILARITY = 0.40


@pytest.fixture(scope="module")
def client() -> httpx.Client:
    with httpx.Client(base_url=BASE_URL, timeout=120.0) as c:
        yield c


def test_service_healthy(client: httpx.Client) -> None:
    resp = client.get("/health")
    assert resp.status_code == 200
    body = resp.json()
    assert body["status"] == "ok"
    assert body["enrolled_persons"] >= 3
    assert body["enrolled_faces"] >= 3


@pytest.mark.parametrize("filename,expected_external_id", sorted(EXPECTED_MATCHES.items()))
def test_search_matches_enrolled_person(
    client: httpx.Client,
    filename: str,
    expected_external_id: str,
) -> None:
    path = TESTPIC_DIR / filename
    assert path.is_file(), f"缺少测试图: {path}"

    with path.open("rb") as fh:
        resp = client.post(
            "/search",
            files={"file": (path.name, fh, "image/png")},
        )

    assert resp.status_code == 200, resp.text
    body = resp.json()
    assert body["query_face_detected"] is True, f"{filename} 未检测到人脸"
    assert body["matches"], f"{filename} 无匹配结果"

    top = body["matches"][0]
    assert top["external_id"] == expected_external_id, (
        f"{filename} 期望命中 person_{expected_external_id}，"
        f"实际 top1={top['person_name']} similarity={top['similarity']}, matches={body['matches']}"
    )
    assert top["similarity"] >= MIN_SIMILARITY


def test_search_returns_ranked_results(client: httpx.Client) -> None:
    path = TESTPIC_DIR / "22.png"
    with path.open("rb") as fh:
        resp = client.post(
            "/search",
            files={"file": (path.name, fh, "image/png")},
        )
    
    print(resp.text)
    assert resp.status_code == 200
    matches = resp.json()["matches"]
    similarities = [m["similarity"] for m in matches]
    assert similarities == sorted(similarities, reverse=True)
