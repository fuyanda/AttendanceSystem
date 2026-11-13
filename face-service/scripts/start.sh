#!/usr/bin/env bash
# 后台启动人脸服务
# 前台调试: bash scripts/start.sh --fg
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=scripts/_common.sh
source "$SCRIPT_DIR/_common.sh"

FOREGROUND=0
if [ "${1:-}" = "--fg" ] || [ "${1:-}" = "-f" ]; then
  FOREGROUND=1
fi

load_env

if [ ! -d "models/buffalo_sc" ]; then
  echo "未检测到模型，开始下载..."
  bash scripts/prepare_models.sh
fi

resolve_cmd

if is_running; then
  echo "服务已在运行 (pid=$(cat "$PID_FILE"))，端口 $PORT"
  echo "如需重启: bash scripts/restart.sh"
  exit 0
fi

if [ "$FOREGROUND" -eq 1 ]; then
  echo "前台启动: ${CMD[*]}  (HOST=$HOST PORT=$PORT)"
  exec "${CMD[@]}"
fi

nohup "${CMD[@]}" >>"$LOG_FILE" 2>&1 &
echo $! >"$PID_FILE"

sleep 1
if is_running; then
  echo "已后台启动 (pid=$(cat "$PID_FILE"))"
  echo "监听: http://${HOST}:${PORT}/docs"
  echo "日志: $LOG_FILE"
  echo "停止: bash scripts/stop.sh"
else
  echo "启动失败，请查看日志: $LOG_FILE" >&2
  rm -f "$PID_FILE"
  exit 1
fi
