# face-service 服务器部署说明

面向 **x86_64 / aarch64（ARM64）通用 Linux 服务器**。使用 **传统 Python（venv + pip）**，纯 CPU、可内网离线运行。

> **不支持**：RV1126/RV1109 等 armv7l 嵌入式门禁板（无 Python、无 onnxruntime 可用 wheel）。这类设备请使用其自带 RockFace/门禁 API。

---

## 1. 环境要求

| 项 | 要求 |
|---|---|
| 系统 | Linux x86_64 或 aarch64 |
| Python | **3.10+**（推荐 3.11），带 `venv` / `pip` |
| 内存 | 建议 ≥ 2GB 可用（模型 + 进程常驻约几百 MB） |
| 磁盘 | 建议 ≥ 1GB（代码 + `buffalo_sc` 模型 + 数据） |
| 网络 | 首次装依赖/下模型需外网；之后可完全离线 |
| GPU | **不需要** |

开放端口：默认 **8080**（可在 `.env` 修改）。

确认 Python：

```bash
python3 --version    # 需 >= 3.10
python3 -m venv -h   # 确认有 venv 模块
python3 -m pip -V
```

若系统缺 `venv`（Debian/Ubuntu 常见）：

```bash
sudo apt update
sudo apt install -y python3-venv python3-pip
```

---

## 2. 部署方式选择

| 场景 | 做法 |
|---|---|
| 服务器较新且能上网 | conda/venv + pip 安装依赖后启动 |
| 服务器太旧，pip 失败 | 优先 Docker；否则用 requirements-legacy.txt（见 2.1） |
| 服务器纯内网 | pack_offline.sh 同架构打包后拷贝 |
| 长期运行 | systemd 或 docker compose |

---

## 2.1 老服务器 pip 依赖安装失败怎么办

线上常见失败原因不是「没网」，而是系统太旧：

- CentOS 7 / glibc < 2.28：`onnxruntime`、`opencv-python` 找不到匹配 wheel，或报 `GLIBC_2.2x not found`
- Python < 3.10：很多新包无 wheel
- 架构不一致：aarch64 用了 x86_64 包

先诊断：

```bash
uname -m
cat /etc/os-release
ldd --version | head -1
python -V
```

### 处理优先级

#### 方案 A：Docker（最稳，优先）

主机再旧，只要能跑 Docker 即可。

```bash
cd /opt/face-service
cp .env.example .env
# 先准备 models/buffalo_sc
docker compose build && docker compose up -d
curl -s http://127.0.0.1:8080/api/v1/health
```

#### 方案 B：conda + 兼容依赖

不要直接装 `requirements.txt`（其中 `onnxruntime>=1.19` 在老机常失败）。

```bash
cd /opt/face-service
bash scripts/install_conda_legacy.sh
bash scripts/start.sh
```

等价手动：

```bash
conda create -n face-service python=3.11 -y
conda activate face-service
conda install -y -c conda-forge numpy=1.26 pillow opencv=4.8 onnx=1.16 onnxruntime=1.16
pip install -r requirements-legacy.txt
```

#### 方案 C：同架构离线包

打包机架构/系统要尽量与线上一致；不要在 macOS 上下 wheel 拷到 CentOS 7。

```bash
# 外网
bash scripts/pack_offline.sh legacy
# 线上
pip install --no-index --find-links=offline-wheels -r requirements-legacy.txt
```

#### 方案 D：换较新机器或升级系统

长期建议 Ubuntu 22.04+ / Rocky 8+，或固定用 Docker。

相关文件：`Dockerfile`、`requirements-legacy.txt`、`scripts/install_conda_legacy.sh`、`scripts/pack_offline.sh`

---

## 3. Conda 部署（线上推荐）

适合服务器已装 Anaconda / Miniconda。`scripts/start.sh` 支持：先 `conda activate`，再启动。

### 3.1 放置代码

```bash
mkdir -p /opt/face-service
cd /opt/face-service
```

### 3.2 创建环境并安装依赖

```bash
conda create -n face-service python=3.11 -y
conda activate face-service

cd /opt/face-service
# 可选: pip config set global.index-url https://pypi.tuna.tsinghua.edu.cn/simple
pip install -U pip
pip install -r requirements.txt

python -c "import fastapi, insightface, onnxruntime; print('ok')"
```

### 3.3 模型与配置

```bash
bash scripts/prepare_models.sh    # 需外网；内网则事先拷贝 models/buffalo_sc
cp .env.example .env              # 按需改 PORT / 阈值
```

### 3.4 启动

```bash
conda activate face-service
cd /opt/face-service
bash scripts/start.sh             # 后台
# bash scripts/start.sh --fg      # 前台
bash scripts/stop.sh
bash scripts/restart.sh

curl -s http://127.0.0.1:8080/api/v1/health
```

也可：`python -m app.main`

### 3.5 systemd（conda 绝对路径）

```bash
conda activate face-service
which python
# 例: /home/xxx/miniconda3/envs/face-service/bin/python
```

```ini
[Service]
WorkingDirectory=/opt/face-service
EnvironmentFile=/opt/face-service/.env
ExecStart=/home/xxx/miniconda3/envs/face-service/bin/python -m app.main
```

systemd **不会**自动 `conda activate`，必须写 env 内 python 的绝对路径。

### 3.6 内网离线

外网同架构机：`pip download -r requirements.txt -d offline-wheels`，连同 `models/` 打包。  
内网机：`pip install --no-index --find-links=offline-wheels -r requirements.txt` 后启动。

录入底库示例：

```bash
conda activate face-service
python scripts/test_face_match.py --face-dir /path/to/face --enroll-only
```

---

## 4. 在线服务器部署（venv + pip）

### 3.1 放置代码

把整个 `face-service/` 目录放到服务器，例如：

```bash
sudo mkdir -p /opt/face-service
# 任选其一同步:
#   scp -r face-service/* user@SERVER:/opt/face-service/
#   rsync -avz --exclude '.venv' --exclude 'data' face-service/ user@SERVER:/opt/face-service/
cd /opt/face-service
```

仓库根目录旁的 `pic/`、`testpic/` 若要做录入/回归测试，放到与本地一致的相对位置（`enroll_pic.py` 默认找仓库根下的 `pic/`）。

### 3.2 创建虚拟环境并安装依赖

```bash
cd /opt/face-service
python3 -m venv .venv
source .venv/bin/activate

# 可选：国内镜像加速
# export PIP_INDEX_URL=https://pypi.tuna.tsinghua.edu.cn/simple

pip install -U pip
pip install -r requirements.txt
```

需要跑测试时再装开发依赖：

```bash
pip install -r requirements-dev.txt
```

退出虚拟环境：`deactivate`。后续也可用 `.venv/bin/python` / `.venv/bin/pip` 直接调用，不必每次 activate。

### 3.3 准备模型

```bash
cd /opt/face-service
bash scripts/prepare_models.sh
# 完成后应存在: models/buffalo_sc/*.onnx
ls models/buffalo_sc/
```

脚本会优先用 `.venv/bin/python`。

### 3.4 配置环境变量

```bash
cp .env.example .env
# 按需编辑 .env（监听地址、阈值、数据路径等）
```

常用项：

| 变量 | 默认 | 说明 |
|---|---|---|
| `HOST` / `PORT` | `0.0.0.0` / `8080` | 监听地址与端口；`start.sh` / `python -m app.main` 会读取 |
| `INSIGHTFACE_MODEL` | `buffalo_sc` | 轻量 CPU 模型；精度不够可改 `buffalo_l`（更大更慢） |
| `SIMILARITY_THRESHOLD` | `0.40` | 1:N 最低相似度 |
| `DATABASE_PATH` | `./data/face.db` | SQLite 人脸库 |
| `UPLOAD_DIR` | `./data/uploads` | 上传原图目录 |

### 3.5 启动 / 停止 / 重启

后台运行（不占终端）：

```bash
bash scripts/start.sh            # 后台启动
bash scripts/stop.sh             # 停止
bash scripts/restart.sh          # 重启
bash scripts/start.sh --fg       # 前台调试
```

- PID：`run/face-service.pid`
- 日志：`logs/face-service.log`（可用 `tail -f logs/face-service.log`）

也可直接：

```bash
# 端口取自 .env；或显式指定
.venv/bin/python -m app.main
```

验证（端口以 `.env` 的 `PORT` 为准，默认 8080）：

```bash
curl -s http://127.0.0.1:${PORT:-8080}/api/v1/health
# 浏览器打开: http://<服务器IP>:<PORT>/docs
```

---

## 5. 内网离线部署（venv）

### 4.1 外网机器准备（与目标机同架构）

```bash
cd face-service
python3 -m venv .venv
source .venv/bin/activate
pip install -U pip
pip install -r requirements.txt
bash scripts/prepare_models.sh
```

**方式 A：下载 wheel 包带到内网再装（推荐）**

```bash
mkdir -p offline-wheels
pip download -r requirements.txt -d offline-wheels
# 连同 models/、代码一起打包
tar czvf face-service-offline.tar.gz \
  --exclude='.venv' \
  --exclude='data' \
  --exclude='.pytest_cache' \
  --exclude='__pycache__' \
  -C .. face-service
```

内网机：

```bash
cd /opt/face-service
python3 -m venv .venv
source .venv/bin/activate
pip install --no-index --find-links=offline-wheels -r requirements.txt
cp .env.example .env
bash scripts/start.sh
```

**方式 B：直接拷贝同架构 `.venv`**

源机与目标机需同为 x86_64 或同为 aarch64，且 glibc 相近。把整个 `face-service`（含 `.venv` 与 `models/`）拷过去后直接：

```bash
cd /opt/face-service
cp .env.example .env
bash scripts/start.sh
```

建议打包内容：

```text
face-service/
  app/
  scripts/
  models/buffalo_sc/     # 必带
  requirements.txt
  requirements-dev.txt   # 可选
  offline-wheels/        # 方式 A 必带
  .env.example
  README.md
  DEPLOY.md
  tests/                 # 可选
```

---

## 6. 录入人脸与联调（可选）

服务已启动后（默认 `127.0.0.1:8080`）：

```bash
source .venv/bin/activate

# 将仓库根目录 pic/ 下的照片注册进库
python scripts/enroll_pic.py

# 用 testpic/ 做搜索回归（需先 pip install -r requirements-dev.txt）
pytest tests/test_search_testpic.py -v
```

API 手工调用：

```bash
# 建人员
curl -s -X POST http://127.0.0.1:8080/api/v1/persons \
  -H 'Content-Type: application/json' \
  -d '{"name":"张三","external_id":"EMP001"}'

# 录入人脸（person_id 以实际返回为准）
curl -s -X POST http://127.0.0.1:8080/api/v1/persons/1/faces \
  -F 'file=@/path/to/photo.jpg'

# 1:N 搜索
curl -s -X POST http://127.0.0.1:8080/api/v1/search \
  -F 'file=@/path/to/query.jpg'
```

---

## 7. systemd 常驻（venv）

创建 `/etc/systemd/system/face-service.service`：

```ini
[Unit]
Description=Intranet Face Search Service
After=network.target

[Service]
Type=simple
User=root
WorkingDirectory=/opt/face-service
EnvironmentFile=/opt/face-service/.env
ExecStart=/opt/face-service/.venv/bin/python -m app.main
Restart=on-failure
RestartSec=5
LimitNOFILE=65535

[Install]
WantedBy=multi-user.target
```

`User`、`WorkingDirectory` 按实际修改。不要用系统全局 `uvicorn`，必须指向本项目 `.venv`。

> 监听地址/端口以 `.env` 的 `HOST`/`PORT` 为准。若坚持用 uvicorn CLI，需显式传参，例如：  
> `ExecStart=.../uvicorn app.main:app --host 0.0.0.0 --port 9080`  
> **仅改 `.env` 而 CLI 仍写 `--port 8080` 不会生效。**

启用：

```bash
sudo systemctl daemon-reload
sudo systemctl enable --now face-service
sudo systemctl status face-service
journalctl -u face-service -f
```

防火墙（按发行版）：

```bash
# firewalld 示例
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

前面可加 Nginx 反代并限制来源 IP / `client_max_body_size`（上传默认上限 5MB）。

---

## 8. 备份与升级

**备份：**

- `data/face.db` — 人员与特征向量（必备）
- `data/uploads/` — 原图（可选，特征已在库中）

**升级代码：**

```bash
sudo systemctl stop face-service
# rsync / 解压新版本，注意不要覆盖 data/ 与 .env
cd /opt/face-service
source .venv/bin/activate
pip install -r requirements.txt
sudo systemctl start face-service
```

---

## 9. 常见问题

| 现象 | 处理 |
|---|---|
| `ensurepip` / `venv` 不可用 | `sudo apt install python3-venv python3-pip` |
| `未找到模型目录 models/buffalo_sc` | 执行 `bash scripts/prepare_models.sh`，或从外网机拷贝整个 `models/` |
| `onnxruntime` / 依赖安装失败 | 确认架构为 x86_64 或 aarch64；Python ≥ 3.10；换国内 PyPI 镜像 |
| `ModuleNotFoundError` | 确认已 `source .venv/bin/activate`，或用 `.venv/bin/python` / `.venv/bin/uvicorn` |
| 启动后搜不到人 / 相似度低 | 调低 `SIMILARITY_THRESHOLD`（如 `0.35`）；录入用正面、光照均匀、单人脸照片 |
| 端口不通 | 查 `ss -tlnp \| grep 8080`、防火墙、安全组是否放行 |
| armv7l / 门禁板无法安装 | 换通用 Linux 服务器部署本服务；板端用厂商人脸方案 |

国内 pip 镜像示例：

```bash
pip install -r requirements.txt -i https://pypi.tuna.tsinghua.edu.cn/simple
```

---

## 10. 最短检查清单

```text
[ ] Linux x86_64 或 aarch64，Python 3.10+
[ ] python3 -m venv .venv 成功
[ ] pip install -r requirements.txt 成功
[ ] models/buffalo_sc 存在
[ ] .env 已配置
[ ] curl http://127.0.0.1:8080/api/v1/health 正常
[ ] （生产）systemd 已 enable，防火墙已放行 8080
[ ] （可选）录入 pic/ 并跑 pytest
```

部署完成后访问：`http://<服务器IP>:8080/docs`
