"""SQLite 人脸库 + 内存索引，适合数百至数千规模。"""

from __future__ import annotations

import logging
import sqlite3
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path

import numpy as np

logger = logging.getLogger(__name__)


def utc_now() -> datetime:
    return datetime.now(timezone.utc)


@dataclass
class StoredFace:
    face_id: int
    person_id: int
    person_name: str
    external_id: str | None
    embedding: np.ndarray


class FaceStore:
    def __init__(self, db_path: str) -> None:
        self._db_path = db_path
        Path(db_path).parent.mkdir(parents=True, exist_ok=True)
        self._conn = sqlite3.connect(db_path, check_same_thread=False)
        self._conn.row_factory = sqlite3.Row
        self._init_schema()
        self._faces: list[StoredFace] = []
        self._matrix: np.ndarray | None = None
        self.reload_index()

    def _init_schema(self) -> None:
        self._conn.executescript(
            """
            CREATE TABLE IF NOT EXISTS persons (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                external_id TEXT UNIQUE,
                created_at TEXT NOT NULL
            );

            CREATE TABLE IF NOT EXISTS faces (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                person_id INTEGER NOT NULL,
                image_path TEXT NOT NULL,
                embedding BLOB NOT NULL,
                created_at TEXT NOT NULL,
                FOREIGN KEY (person_id) REFERENCES persons(id) ON DELETE CASCADE
            );

            CREATE INDEX IF NOT EXISTS idx_faces_person_id ON faces(person_id);
            """
        )
        self._conn.commit()

    def reload_index(self) -> None:
        rows = self._conn.execute(
            """
            SELECT f.id AS face_id,
                   f.person_id,
                   p.name AS person_name,
                   p.external_id,
                   f.embedding
            FROM faces f
            JOIN persons p ON p.id = f.person_id
            ORDER BY f.id
            """
        ).fetchall()

        self._faces = []
        vectors: list[np.ndarray] = []
        for row in rows:
            embedding = np.frombuffer(row["embedding"], dtype=np.float32)
            stored = StoredFace(
                face_id=row["face_id"],
                person_id=row["person_id"],
                person_name=row["person_name"],
                external_id=row["external_id"],
                embedding=embedding,
            )
            self._faces.append(stored)
            vectors.append(embedding)

        if vectors:
            self._matrix = np.stack(vectors, axis=0)
        else:
            self._matrix = None

        logger.info("人脸索引已加载: %d 条", len(self._faces))

    def create_person(self, name: str, external_id: str | None) -> int:
        now = utc_now().isoformat()
        try:
            cursor = self._conn.execute(
                "INSERT INTO persons(name, external_id, created_at) VALUES (?, ?, ?)",
                (name.strip(), external_id.strip() if external_id else None, now),
            )
        except sqlite3.IntegrityError as exc:
            raise ValueError("external_id 已存在") from exc
        self._conn.commit()
        return int(cursor.lastrowid)

    def get_person(self, person_id: int) -> sqlite3.Row | None:
        return self._conn.execute(
            "SELECT * FROM persons WHERE id = ?",
            (person_id,),
        ).fetchone()

    def get_person_by_external_id(self, external_id: str) -> sqlite3.Row | None:
        if not external_id or not external_id.strip():
            return None
        return self._conn.execute(
            """
            SELECT p.id,
                   p.name,
                   p.external_id,
                   p.created_at,
                   COUNT(f.id) AS face_count
            FROM persons p
            LEFT JOIN faces f ON f.person_id = p.id
            WHERE p.external_id = ?
            GROUP BY p.id
            """,
            (external_id.strip(),),
        ).fetchone()

    def list_persons(self) -> list[sqlite3.Row]:
        return self._conn.execute(
            """
            SELECT p.id,
                   p.name,
                   p.external_id,
                   p.created_at,
                   COUNT(f.id) AS face_count
            FROM persons p
            LEFT JOIN faces f ON f.person_id = p.id
            GROUP BY p.id
            ORDER BY p.id
            """
        ).fetchall()

    def delete_person(self, person_id: int) -> bool:
        row = self.get_person(person_id)
        if row is None:
            return False
        self._conn.execute("DELETE FROM persons WHERE id = ?", (person_id,))
        self._conn.commit()
        self.reload_index()
        return True

    def add_face(self, person_id: int, image_path: str, embedding: np.ndarray) -> int:
        if self.get_person(person_id) is None:
            raise ValueError("人员不存在")

        now = utc_now().isoformat()
        cursor = self._conn.execute(
            """
            INSERT INTO faces(person_id, image_path, embedding, created_at)
            VALUES (?, ?, ?, ?)
            """,
            (person_id, image_path, embedding.astype(np.float32).tobytes(), now),
        )
        self._conn.commit()
        self.reload_index()
        return int(cursor.lastrowid)

    def list_faces(self, person_id: int | None = None) -> list[sqlite3.Row]:
        if person_id is None:
            return self._conn.execute(
                """
                SELECT f.id, f.person_id, p.name AS person_name, f.image_path, f.created_at
                FROM faces f
                JOIN persons p ON p.id = f.person_id
                ORDER BY f.id DESC
                """
            ).fetchall()

        return self._conn.execute(
            """
            SELECT f.id, f.person_id, p.name AS person_name, f.image_path, f.created_at
            FROM faces f
            JOIN persons p ON p.id = f.person_id
            WHERE f.person_id = ?
            ORDER BY f.id DESC
            """,
            (person_id,),
        ).fetchall()

    def delete_face(self, face_id: int) -> bool:
        cursor = self._conn.execute("DELETE FROM faces WHERE id = ?", (face_id,))
        self._conn.commit()
        if cursor.rowcount == 0:
            return False
        self.reload_index()
        return True

    def search(
        self,
        query_embedding: np.ndarray,
        top_k: int,
        threshold: float,
    ) -> list[tuple[StoredFace, float]]:
        if self._matrix is None or not self._faces:
            return []

        # 特征已 L2 归一化，余弦相似度 = 点积；按人员去重，每人只保留最高分
        scores = self._matrix @ query_embedding.astype(np.float32)
        order = np.argsort(scores)[::-1]

        results: list[tuple[StoredFace, float]] = []
        seen_persons: set[int] = set()
        for idx in order:
            score = float(scores[idx])
            if score < threshold:
                break
            face = self._faces[idx]
            if face.person_id in seen_persons:
                continue
            seen_persons.add(face.person_id)
            results.append((face, score))
            if len(results) >= top_k:
                break
        return results

    @property
    def face_count(self) -> int:
        return len(self._faces)

    @property
    def person_count(self) -> int:
        row = self._conn.execute("SELECT COUNT(*) AS cnt FROM persons").fetchone()
        return int(row["cnt"]) if row else 0
