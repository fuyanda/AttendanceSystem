"""InsightFace 封装：检测、对齐、提取 512 维特征。"""

from __future__ import annotations

import logging
from pathlib import Path

import cv2
import numpy as np
from insightface.app import FaceAnalysis

logger = logging.getLogger(__name__)


class FaceEngine:
    def __init__(self, model_root: str, model_name: str) -> None:
        # InsightFace 实际目录为 {root}/models/{name}
        model_dir = Path(model_root) / "models" / model_name
        if not model_dir.is_dir():
            raise FileNotFoundError(
                f"未找到模型目录: {model_dir}\n"
                "请在有网络的机器执行 scripts/prepare_models.sh，"
                "再将 models/ 目录拷贝到内网服务器。"
            )

        self._app = FaceAnalysis(
            name=model_name,
            root=model_root,
            providers=["CPUExecutionProvider"],
        )
        # det_size 过大时小图/戴眼镜人脸可能漏检；480 在本场景更稳
        self._app.prepare(ctx_id=-1, det_size=(480, 480))
        logger.info("InsightFace 已加载: %s (CPU)", model_name)

    def extract_largest_face(self, image_bgr: np.ndarray) -> np.ndarray | None:
        """提取图中面积最大的人脸特征，返回 L2 归一化后的 512 维向量。"""
        if image_bgr is None or image_bgr.size == 0:
            return None

        faces = self._app.get(image_bgr)
        if not faces:
            return None

        face = max(
            faces,
            key=lambda item: (item.bbox[2] - item.bbox[0]) * (item.bbox[3] - item.bbox[1]),
        )
        embedding = np.asarray(face.normed_embedding, dtype=np.float32)
        if embedding.shape[0] != 512:
            raise ValueError(f"特征维度异常: {embedding.shape}")
        return embedding

    @staticmethod
    def decode_image_bytes(content: bytes) -> np.ndarray:
        buffer = np.frombuffer(content, dtype=np.uint8)
        image = cv2.imdecode(buffer, cv2.IMREAD_COLOR)
        if image is None:
            raise ValueError("无法解析图片，请上传 jpg/png/webp 格式")
        return image
