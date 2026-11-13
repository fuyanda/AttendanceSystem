"""API 请求/响应模型。"""

from datetime import datetime

from pydantic import BaseModel, Field


class PersonCreateRequest(BaseModel):
    name: str = Field(..., min_length=1, max_length=128, description="人员姓名或标识")
    external_id: str | None = Field(
        default=None,
        max_length=64,
        description="业务侧唯一编号，可选",
    )


class PersonResponse(BaseModel):
    id: int
    name: str
    external_id: str | None
    face_count: int
    created_at: datetime


class FaceResponse(BaseModel):
    id: int
    person_id: int
    person_name: str
    image_path: str
    created_at: datetime


class SearchMatch(BaseModel):
    face_id: int
    person_id: int
    person_name: str
    external_id: str | None
    similarity: float


class SearchResponse(BaseModel):
    query_face_detected: bool
    matches: list[SearchMatch]


class HealthResponse(BaseModel):
    status: str
    model: str
    enrolled_faces: int
    enrolled_persons: int
