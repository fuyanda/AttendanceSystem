#!/usr/bin/env bash
# 重启后台人脸服务
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

bash "$SCRIPT_DIR/stop.sh"
bash "$SCRIPT_DIR/start.sh" "$@"
