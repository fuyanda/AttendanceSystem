#!/usr/bin/env bash
set -eo pipefail

# RuoYi-Vue-fast 一键启动（SDKMAN 管理 Java + Maven spring-boot:run）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# pom.xml 指定 java.version=1.8，默认使用本机已安装的 Amazon Corretto 8
JAVA_VERSION="${JAVA_VERSION:-8.0.472-amzn}"
MAVEN_GOALS="${MAVEN_GOALS:-spring-boot:run}"

usage() {
  cat <<'EOF'
用法: ./scripts/run.sh [选项] [-- Maven 额外参数]

选项:
  -h, --help    显示帮助
  -b, --build   启动前先 mvn clean package -DskipTests

环境变量:
  JAVA_VERSION  覆盖 SDKMAN Java 版本（默认 8.0.472-amzn）
  MAVEN_GOALS   覆盖 Maven 目标（默认 spring-boot:run）

示例:
  ./scripts/run.sh
  ./scripts/run.sh -b
  ./scripts/run.sh -- -Dspring-boot.run.jvmArguments="-Xmx512m"
EOF
}

DO_BUILD=false
MAVEN_ARGS=()

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)
      usage
      exit 0
      ;;
    -b|--build)
      DO_BUILD=true
      shift
      ;;
    --)
      shift
      MAVEN_ARGS+=("$@")
      break
      ;;
    *)
      MAVEN_ARGS+=("$1")
      shift
      ;;
  esac
done

SDKMAN_DIR="${SDKMAN_DIR:-$HOME/.sdkman}"
if [[ ! -s "${SDKMAN_DIR}/bin/sdkman-init.sh" ]]; then
  echo "错误: 未找到 SDKMAN，请先安装 https://sdkman.io" >&2
  exit 1
fi

# shellcheck source=/dev/null
source "${SDKMAN_DIR}/bin/sdkman-init.sh"

if ! sdk use java "${JAVA_VERSION}" >/dev/null 2>&1; then
  echo "错误: 无法切换到 Java ${JAVA_VERSION}" >&2
  echo "已安装版本: sdk list java | grep installed" >&2
  echo "安装示例: sdk install java 8.0.472-amzn" >&2
  exit 1
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "错误: 未找到 mvn，请安装 Maven 并加入 PATH" >&2
  exit 1
fi

cd "${PROJECT_ROOT}"

echo "========================================"
echo " RuoYi-Vue-fast"
echo " 目录: ${PROJECT_ROOT}"
echo " Java: $(java -version 2>&1 | head -n 1)"
echo " Maven: $(mvn -version 2>&1 | head -n 1 | cut -d' ' -f1-3)"
echo " 端口: 8091 (application.yml)"
echo "========================================"

if [[ "${DO_BUILD}" == true ]]; then
  echo ">> mvn clean package -DskipTests"
  mvn clean package -DskipTests
fi

echo ">> mvn ${MAVEN_GOALS}"
exec mvn "${MAVEN_GOALS}" "${MAVEN_ARGS[@]}"
