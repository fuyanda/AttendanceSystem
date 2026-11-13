"""内网人脸库服务入口。"""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager
from pathlib import Path

from fastapi import FastAPI

from app.config import settings
from app.face_engine import FaceEngine
from app.face_store import FaceStore
from app.routes import router

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s - %(message)s",
)
logger = logging.getLogger(__name__)

BASE_DIR = Path(__file__).resolve().parent.parent
settings.resolve_paths(BASE_DIR)

face_engine: FaceEngine
face_store: FaceStore


@asynccontextmanager
async def lifespan(_: FastAPI):
    global face_engine, face_store

    logger.info("启动人脸服务，模型=%s", settings.insightface_model)
    face_engine = FaceEngine(settings.insightface_root, settings.insightface_model)
    face_store = FaceStore(settings.database_path)
    yield
    logger.info("服务已停止")


app = FastAPI(
    title="Face Search Service",
    description="内网离线人脸库：人员录入 + 1:N 搜索",
    version="1.0.0",
    lifespan=lifespan,
)
app.include_router(router)


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "app.main:app",
        host=settings.host,
        port=settings.port,
        reload=False,
    )
