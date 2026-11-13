"""应用配置，支持 .env 与环境变量。"""

from pathlib import Path

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    host: str = "0.0.0.0"
    port: int = 8080

    # InsightFace root：模型实际位于 {root}/models/{model_name}
    insightface_root: str = "."
    insightface_model: str = "buffalo_sc"

    database_path: str = "./data/face.db"
    upload_dir: str = "./data/uploads"

    search_top_k: int = 5
    similarity_threshold: float = 0.40
    max_upload_bytes: int = 5 * 1024 * 1024

    def resolve_paths(self, base_dir: Path) -> None:
        """将相对路径解析为基于项目根目录的绝对路径。"""
        self.insightface_root = str((base_dir / self.insightface_root).resolve())
        self.database_path = str((base_dir / self.database_path).resolve())
        self.upload_dir = str((base_dir / self.upload_dir).resolve())


settings = Settings()
