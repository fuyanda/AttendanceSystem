#!/usr/bin/env bash
# �ڡ�������������ͬ�ܹ����Ļ����ϴ���������� + ģ��
# �÷�:
#   bash scripts/pack_offline.sh              # ��ϵͳ���� requirements.txt
#   bash scripts/pack_offline.sh legacy       # ��ϵͳ���� requirements-legacy.txt
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

MODE="${1:-normal}"
REQ="requirements.txt"
OUT="face-service-offline.tar.gz"
if [ "$MODE" = "legacy" ]; then
  REQ="requirements-legacy.txt"
  OUT="face-service-offline-legacy.tar.gz"
fi

mkdir -p offline-wheels
python3 -m pip install -U pip
python3 -m pip download -r "$REQ" -d offline-wheels

if [ ! -d "models/buffalo_sc" ]; then
  echo "��������ģ�ͣ���������..."
  bash scripts/prepare_models.sh || true
fi

tar czvf "$OUT" \
  --exclude='.venv' \
  --exclude='data' \
  --exclude='logs' \
  --exclude='run' \
  --exclude='.pytest_cache' \
  --exclude='__pycache__' \
  --exclude='.git' \
  app scripts models offline-wheels \
  requirements.txt requirements-legacy.txt \
  .env.example README.md DEPLOY.md \
  Dockerfile docker-compose.yml .dockerignore \
  pyproject.toml 2>/dev/null || true

echo
echo "������: $ROOT_DIR/$OUT"
echo "������������:"
echo "  tar xzvf $OUT"
echo "  conda activate face-service"
echo "  pip install --no-index --find-links=offline-wheels -r $REQ"
