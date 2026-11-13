#!/usr/bin/env bash
# 供 start/stop/restart 共用
# shellcheck disable=SC2034

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

RUN_DIR="$ROOT_DIR/run"
LOG_DIR="$ROOT_DIR/logs"
PID_FILE="$RUN_DIR/face-service.pid"
LOG_FILE="$LOG_DIR/face-service.log"

mkdir -p "$RUN_DIR" "$LOG_DIR"

load_env() {
  if [ ! -f ".env" ]; then
    cp .env.example .env
  fi

  HOST="0.0.0.0"
  PORT="8080"
  while IFS= read -r line || [ -n "$line" ]; do
    case "$line" in
      ''|\#*) continue ;;
    esac
    key="${line%%=*}"
    val="${line#*=}"
    key="$(echo "$key" | tr -d '[:space:]')"
    val="$(echo "$val" | sed 's/^[[:space:]]*//;s/[[:space:]]*$//')"
    case "$key" in
      HOST) HOST="$val" ;;
      PORT) PORT="$val" ;;
    esac
  done < .env
}

_python_has_app() {
  local py="$1"
  "$py" -c "import uvicorn, fastapi, insightface" >/dev/null 2>&1
}

resolve_cmd() {
  # 1) 项目本地 venv
  if [ -x ".venv/bin/uvicorn" ]; then
    CMD=(.venv/bin/uvicorn app.main:app --host "$HOST" --port "$PORT")
    return 0
  fi
  if [ -x ".venv/bin/python" ] && _python_has_app ".venv/bin/python"; then
    CMD=(.venv/bin/python -m app.main)
    return 0
  fi

  # 2) 当前已激活的 conda 环境
  if [ -n "${CONDA_PREFIX:-}" ]; then
    if [ -x "$CONDA_PREFIX/bin/uvicorn" ]; then
      CMD=("$CONDA_PREFIX/bin/uvicorn" app.main:app --host "$HOST" --port "$PORT")
      return 0
    fi
    if [ -x "$CONDA_PREFIX/bin/python" ] && _python_has_app "$CONDA_PREFIX/bin/python"; then
      CMD=("$CONDA_PREFIX/bin/python" -m app.main)
      return 0
    fi
  fi

  # 3) PATH 上的 uvicorn / python（conda activate 后常见）
  if command -v uvicorn >/dev/null 2>&1; then
    CMD=(uvicorn app.main:app --host "$HOST" --port "$PORT")
    return 0
  fi
  if command -v python >/dev/null 2>&1 && _python_has_app "$(command -v python)"; then
    CMD=(python -m app.main)
    return 0
  fi
  if command -v python3 >/dev/null 2>&1 && _python_has_app "$(command -v python3)"; then
    CMD=(python3 -m app.main)
    return 0
  fi

  # 4) uv
  if command -v uv >/dev/null 2>&1; then
    CMD=(uv run uvicorn app.main:app --host "$HOST" --port "$PORT")
    return 0
  fi

  echo "未找到可用 Python 环境，请任选其一:" >&2
  echo "  # venv" >&2
  echo "  python3 -m venv .venv && .venv/bin/pip install -r requirements.txt" >&2
  echo "  # conda" >&2
  echo "  conda activate face-service && pip install -r requirements.txt" >&2
  exit 1
}

is_running() {
  if [ ! -f "$PID_FILE" ]; then
    return 1
  fi
  local pid
  pid="$(cat "$PID_FILE" 2>/dev/null || true)"
  if [ -z "$pid" ]; then
    return 1
  fi
  if kill -0 "$pid" 2>/dev/null; then
    return 0
  fi
  return 1
}
