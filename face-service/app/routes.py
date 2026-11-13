"""FastAPI 路由。"""

from __future__ import annotations

import logging
import uuid
from pathlib import Path

from fastapi import APIRouter, File, HTTPException, UploadFile

from app.config import settings
from app.schemas import (
    FaceResponse,
    HealthResponse,
    PersonCreateRequest,
    PersonResponse,
    SearchMatch,
    SearchResponse,
)

router = APIRouter(prefix="/api/v1")
logger = logging.getLogger(__name__)

ALLOWED_CONTENT_TYPES = {
    "image/jpeg",
    "image/png",
    "image/webp",
}


def _file_meta(file: UploadFile, content: bytes | None = None) -> str:
    size = len(content) if content is not None else "?"
    return (
        f"filename={file.filename!r}, content_type={file.content_type!r}, "
        f"bytes={size}"
    )


async def _read_upload(file: UploadFile) -> bytes:
    if file.content_type not in ALLOWED_CONTENT_TYPES:
        logger.warning("upload rejected content_type, %s", _file_meta(file))
        raise HTTPException(status_code=400, detail="仅支持 jpg/png/webp 图片")

    content = await file.read()
    if not content:
        logger.warning("upload empty, %s", _file_meta(file, content))
        raise HTTPException(status_code=400, detail="上传文件为空")
    if len(content) > settings.max_upload_bytes:
        logger.warning(
            "upload too large, max=%s, %s",
            settings.max_upload_bytes,
            _file_meta(file, content),
        )
        raise HTTPException(
            status_code=400,
            detail=f"图片过大，最大 {settings.max_upload_bytes // 1024 // 1024}MB",
        )
    return content


def _save_upload(content: bytes, suffix: str) -> str:
    upload_dir = Path(settings.upload_dir)
    upload_dir.mkdir(parents=True, exist_ok=True)
    filename = f"{uuid.uuid4().hex}{suffix}"
    path = upload_dir / filename
    path.write_bytes(content)
    return str(path)


@router.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    from app.main import face_engine, face_store

    return HealthResponse(
        status="ok",
        model=settings.insightface_model,
        enrolled_faces=face_store.face_count,
        enrolled_persons=face_store.person_count,
    )


@router.post("/persons", response_model=PersonResponse, status_code=201)
def create_person(body: PersonCreateRequest) -> PersonResponse:
    from app.main import face_store

    try:
        person_id = face_store.create_person(body.name, body.external_id)
    except ValueError as exc:
        logger.warning(
            "create person conflict, name=%r, external_id=%r, err=%s",
            body.name,
            body.external_id,
            exc,
        )
        raise HTTPException(status_code=409, detail=str(exc)) from exc

    row = face_store.get_person(person_id)
    assert row is not None
    return PersonResponse(
        id=row["id"],
        name=row["name"],
        external_id=row["external_id"],
        face_count=0,
        created_at=row["created_at"],
    )


@router.get("/persons", response_model=list[PersonResponse])
def list_persons() -> list[PersonResponse]:
    from app.main import face_store

    return [
        PersonResponse(
            id=row["id"],
            name=row["name"],
            external_id=row["external_id"],
            face_count=row["face_count"],
            created_at=row["created_at"],
        )
        for row in face_store.list_persons()
    ]


@router.get("/persons/by-external-id/{external_id}", response_model=PersonResponse)
def get_person_by_external_id(external_id: str) -> PersonResponse:
    from app.main import face_store

    row = face_store.get_person_by_external_id(external_id)
    if row is None:
        logger.info("person not found by external_id=%r", external_id)
        raise HTTPException(status_code=404, detail="人员不存在")
    return PersonResponse(
        id=row["id"],
        name=row["name"],
        external_id=row["external_id"],
        face_count=row["face_count"],
        created_at=row["created_at"],
    )


@router.delete("/persons/{person_id}", status_code=204)
def delete_person(person_id: int) -> None:
    from app.main import face_store

    if not face_store.delete_person(person_id):
        logger.info("delete person not found, person_id=%s", person_id)
        raise HTTPException(status_code=404, detail="人员不存在")


@router.post("/persons/{person_id}/faces", response_model=FaceResponse, status_code=201)
async def enroll_face(person_id: int, file: UploadFile = File(...)) -> FaceResponse:
    from app.main import face_engine, face_store

    if face_store.get_person(person_id) is None:
        logger.warning("enroll face person missing, person_id=%s, %s", person_id, _file_meta(file))
        raise HTTPException(status_code=404, detail="人员不存在")

    content = await _read_upload(file)
    suffix = Path(file.filename or "upload.jpg").suffix or ".jpg"

    image = None
    try:
        image = face_engine.decode_image_bytes(content)
        embedding = face_engine.extract_largest_face(image)
    except ValueError as exc:
        logger.warning(
            "enroll face decode/extract failed, person_id=%s, %s, image_shape=%s, err=%s",
            person_id,
            _file_meta(file, content),
            None if image is None else getattr(image, "shape", None),
            exc,
        )
        raise HTTPException(status_code=400, detail=str(exc)) from exc

    if embedding is None:
        logger.warning(
            "enroll face no face detected, person_id=%s, %s, image_shape=%s",
            person_id,
            _file_meta(file, content),
            getattr(image, "shape", None),
        )
        raise HTTPException(status_code=422, detail="未检测到人脸")

    image_path = _save_upload(content, suffix)
    face_id = face_store.add_face(person_id, image_path, embedding)
    row = next(item for item in face_store.list_faces(person_id) if item["id"] == face_id)

    return FaceResponse(
        id=row["id"],
        person_id=row["person_id"],
        person_name=row["person_name"],
        image_path=row["image_path"],
        created_at=row["created_at"],
    )


@router.get("/faces", response_model=list[FaceResponse])
def list_faces(person_id: int | None = None) -> list[FaceResponse]:
    from app.main import face_store

    return [
        FaceResponse(
            id=row["id"],
            person_id=row["person_id"],
            person_name=row["person_name"],
            image_path=row["image_path"],
            created_at=row["created_at"],
        )
        for row in face_store.list_faces(person_id)
    ]


@router.delete("/faces/{face_id}", status_code=204)
def delete_face(face_id: int) -> None:
    from app.main import face_store

    if not face_store.delete_face(face_id):
        logger.info("delete face not found, face_id=%s", face_id)
        raise HTTPException(status_code=404, detail="人脸记录不存在")


@router.post("/search", response_model=SearchResponse)
async def search_face(file: UploadFile = File(...)) -> SearchResponse:
    from app.main import face_engine, face_store

    content = await _read_upload(file)

    image = None
    try:
        image = face_engine.decode_image_bytes(content)
        embedding = face_engine.extract_largest_face(image)
    except ValueError as exc:
        logger.warning(
            "search decode/extract failed, %s, image_shape=%s, err=%s",
            _file_meta(file, content),
            None if image is None else getattr(image, "shape", None),
            exc,
        )
        raise HTTPException(status_code=400, detail=str(exc)) from exc

    if embedding is None:
        logger.info(
            "search no face detected, %s, image_shape=%s",
            _file_meta(file, content),
            getattr(image, "shape", None),
        )
        return SearchResponse(query_face_detected=False, matches=[])

    raw_matches = face_store.search(
        embedding,
        top_k=settings.search_top_k,
        threshold=settings.similarity_threshold,
    )

    matches = [
        SearchMatch(
            face_id=item.face_id,
            person_id=item.person_id,
            person_name=item.person_name,
            external_id=item.external_id,
            similarity=round(score, 4),
        )
        for item, score in raw_matches
    ]
    if not matches:
        logger.info(
            "search no match above threshold, %s, threshold=%s, enrolled=%s",
            _file_meta(file, content),
            settings.similarity_threshold,
            face_store.face_count,
        )
    return SearchResponse(query_face_detected=True, matches=matches)
