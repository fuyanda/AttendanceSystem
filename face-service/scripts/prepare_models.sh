#!/usr/bin/env bash
# 在有互联网的机器上执行，下载 InsightFace 模型到 ./models
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

MODEL_NAME="${INSIGHTFACE_MODEL:-buffalo_sc}"
MODEL_ROOT="${INSIGHTFACE_ROOT:-.}"

if [ -x ".venv/bin/python" ]; then
  PYTHON=".venv/bin/python"
elif command -v python3 >/dev/null 2>&1; then
  PYTHON="python3"
else
  echo "未找到 Python，请先创建虚拟环境并安装依赖" >&2
  exit 1
fi

"$PYTHON" - <<PY
from insightface.app import FaceAnalysis

model_name = "${MODEL_NAME}"
model_root = "${MODEL_ROOT}"
print(f"下载模型 {model_name} 到 {model_root}/models/{model_name} ...")
app = FaceAnalysis(name=model_name, root=model_root, providers=["CPUExecutionProvider"])
app.prepare(ctx_id=-1, det_size=(640, 640))
print("模型准备完成")
PY

echo "请将目录打包拷贝到内网: ${MODEL_ROOT}/models/${MODEL_NAME}"
