# 内网人脸库服务 (face-service)

面向 **小规模（几百张人脸）**、**纯 CPU 内网** 场景的离线 1:N 人脸搜索服务。

## 技术选型结论


| 维度                    | 选择                                                | 原因                                             |
| --------------------- | ------------------------------------------------- | ---------------------------------------------- |
| 推理引擎                  | **InsightFace** `buffalo_sc` + ONNX Runtime (CPU) | 精度够用、纯 CPU、可完全离线；比 CompreFace 更轻、比虹软 SDK 无授权依赖 |
| 向量检索                  | **内存矩阵点积**（启动时从 SQLite 加载）                        | 几百张人脸检索 <1ms，无需 Milvus/FAISS                   |
| 元数据存储                 | **SQLite**                                        | 零运维、单文件备份、内网友好                                 |
| API 框架                | **FastAPI**                                       | 异步上传、OpenAPI 文档、部署简单                           |
| 为何不用 CompreFace       | 组件多（Postgres/Admin/API 多容器）、最后更新 2023             | 小库场景过重                                         |
| 为何不用 Java SmartJavaAI | 可用，但模型与 JNI 打包更重                                  | 若团队强 Java，可后续用 HTTP 调本服务                       |


**模型说明**：`buffalo_sc` 为 InsightFace 小型包，CPU 上单张推理约 200~500ms（视图片与核数而定）。若 CPU 仍有余量且要更高精度，可在 `.env` 改为 `buffalo_l`。

## 目录结构

```
face-service/
├── app/
│   ├── main.py          # 启动入口
│   ├── routes.py        # REST API
│   ├── face_engine.py   # InsightFace 封装
│   ├── face_store.py    # SQLite + 内存索引
│   ├── config.py
│   └── schemas.py
├── scripts/
│   ├── prepare_models.sh  # 外网机器下载模型
│   └── start.sh           # 本地启动（优先 .venv）
├── models/              # 模型目录（离线拷贝，不入 Git）
├── data/                # SQLite + 上传图片
├── requirements.txt
├── requirements-dev.txt
└── .env.example
```



## 服务器部署

完整步骤见 **[DEPLOY.md](./DEPLOY.md)**（**venv + pip**，无需 uv）。

快速启动（x86_64 / aarch64 Linux，Python 3.10+）：

```bash
cd face-service
python3 -m venv .venv
source .venv/bin/activate
pip install -U pip
pip install -r requirements.txt
bash scripts/prepare_models.sh   # 需外网；内网则事先拷贝 models/buffalo_sc
cp .env.example .env
bash scripts/start.sh            # 后台启动
# bash scripts/stop.sh           # 停止
# bash scripts/restart.sh        # 重启
# bash scripts/start.sh --fg     # 前台调试
```

启动后访问：`http://<服务器IP>:<PORT>/docs`（端口见 `.env`）

进程管理：

| 命令 | 说明 |
|------|------|
| `bash scripts/start.sh` | 后台启动，日志写入 `logs/face-service.log` |
| `bash scripts/stop.sh` | 停止 |
| `bash scripts/restart.sh` | 先停再启 |
| `bash scripts/start.sh --fg` | 前台运行（占终端，便于看报错） |

录入与测试：

```bash
source .venv/bin/activate
pip install -r requirements-dev.txt   # 仅测试需要
python scripts/enroll_pic.py
pytest tests/test_search_testpic.py -v
```



## API 说明



### 健康检查

```http
GET /api/v1/health
```



### 创建人员

```http
POST /api/v1/persons
Content-Type: application/json

{"name": "张三", "external_id": "EMP001"}
```



### 录入人脸（绑定到某人员）

```http
POST /api/v1/persons/{person_id}/faces
Content-Type: multipart/form-data

file: <人脸照片>
```

同一人可录入多张照片，搜索时取最高相似度。

### 1:N 人脸搜索

```http
POST /api/v1/search
Content-Type: multipart/form-data

file: <待识别照片>
```

响应示例：

```json
{
  "query_face_detected": true,
  "matches": [
    {
      "face_id": 12,
      "person_id": 3,
      "person_name": "张三",
      "external_id": "EMP001",
      "similarity": 0.7821
    }
  ]
}
```

`similarity` 为余弦相似度，默认阈值 `0.40`（可在 `.env` 调整 `SIMILARITY_THRESHOLD`）。

### 其他

- `GET /api/v1/persons` — 人员列表
- `DELETE /api/v1/persons/{id}` — 删除人员及其所有人脸
- `GET /api/v1/faces?person_id=` — 人脸记录列表
- `DELETE /api/v1/faces/{id}` — 删除单条人脸



## 调用示例（curl）

```bash
# 1. 创建人员
curl -s -X POST http://127.0.0.1:8080/api/v1/persons \
  -H 'Content-Type: application/json' \
  -d '{"name":"张三","external_id":"EMP001"}'

# 2. 录入人脸（假设 person_id=1）
curl -s -X POST http://127.0.0.1:8080/api/v1/persons/1/faces \
  -F 'file=@/path/to/photo.jpg'

# 3. 搜索
curl -s -X POST http://127.0.0.1:8080/api/v1/search \
  -F 'file=@/path/to/query.jpg'
```



## 配置项（.env）


| 变量                     | 默认值            | 说明         |
| ---------------------- | -------------- | ---------- |
| `INSIGHTFACE_MODEL`    | buffalo_sc     | 模型包名       |
| `SIMILARITY_THRESHOLD` | 0.40           | 搜索最低相似度    |
| `SEARCH_TOP_K`         | 5              | 最多返回条数     |
| `MAX_UPLOAD_BYTES`     | 5242880        | 上传大小上限 5MB |
| `DATABASE_PATH`        | ./data/face.db | 人脸库数据库     |
| `UPLOAD_DIR`           | ./data/uploads | 原图存储目录     |




## 备份与迁移

定期备份：

- `data/face.db` — 人员与特征
- `data/uploads/` — 原始照片（可选，特征已在库中）



## 生产建议

1. **阈值调优**：用 20~~30 对正负样本在测试集上扫 `0.35~~0.50`，选定 FRR/FAR 平衡点。
2. **录入规范**：正面、光照均匀、单人脸；侧脸/遮挡会降低召回。
3. **进程守护**：用 systemd 或 supervisor 托管 uvicorn。
4. **反向代理**：前面加 Nginx 限制来源 IP、请求体大小。
5. **合规**：人脸属敏感个人信息，库内按需留存原图，建议仅保留特征 + 业务 ID。



## 依赖环境

- Python 3.10+
- Linux x86_64 / ARM64（需对应 onnxruntime wheel）
- 无需 GPU、无需外网（模型已本地化后）

