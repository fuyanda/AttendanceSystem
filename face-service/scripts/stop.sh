#!/usr/bin/env bash
# 停止后台人脸服务
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
# shellcheck source=scripts/_common.sh
source "$SCRIPT_DIR/_common.sh"

if ! is_running; then
  echo "服务未在运行"
  rm -f "$PID_FILE"
  exit 0
fi

pid="$(cat "$PID_FILE")"
echo "正在停止服务 (pid=$pid) ..."
kill "$pid" 2>/dev/null || true

for _ in 1 2 3 4 5 6 7 8 9 10; do
  if ! kill -0 "$pid" 2>/dev/null; then
    break
  fi
  sleep 0.5
done

if kill -0 "$pid" 2>/dev/null; then
  echo "进程未退出，强制结束..."
  kill -9 "$pid" 2>/dev/null || true
fi

rm -f "$PID_FILE"
echo "服务已停止"
