#!/usr/bin/env bash
# �Ϸ����� + conda�������� conda-forge װ�����ƣ��� pip װ���ݰ�ʣ������
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

if ! command -v conda >/dev/null 2>&1; then
  echo "δ�ҵ� conda" >&2
  exit 1
fi

ENV_NAME="${FACE_SERVICE_CONDA_ENV:-face-service}"

# shellcheck disable=SC1091
source "$(conda info --base)/etc/profile.d/conda.sh"

if ! conda env list | awk '{print $1}' | grep -qx "$ENV_NAME"; then
  conda create -n "$ENV_NAME" python=3.11 -y
fi
conda activate "$ENV_NAME"

echo "��ǰ Python: $(which python) ($(python -V))"

# ������������ conda-forge�����ϵͳ glibc / pip wheel ��ƥ��
conda install -y -c conda-forge \
  "numpy=1.26.*" \
  "pillow=10.*" \
  "opencv=4.8.*" \
  "onnx=1.16.*" \
  "onnxruntime=1.16.*" \
  "libgomp" || true

pip install -U pip
if [ -d offline-wheels ] && [ "$(ls -A offline-wheels 2>/dev/null || true)" ]; then
  pip install --no-index --find-links=offline-wheels -r requirements-legacy.txt
else
  pip install -r requirements-legacy.txt
fi

python - <<'PY'
import onnxruntime, cv2, numpy, insightface, fastapi
print("�������ͨ��")
print("onnxruntime", onnxruntime.__version__)
print("opencv", cv2.__version__)
print("numpy", numpy.__version__)
PY

echo
echo "��װ��ɡ�����ǰ��ȷ�� models/buffalo_sc ���ڣ�Ȼ��:"
echo "  conda activate $ENV_NAME"
echo "  cd $ROOT_DIR && bash scripts/start.sh"
