#!/usr/bin/env bash
set -eo pipefail

# RuoYi-Vue-fast 打包（SDKMAN 管理 Java + mvn package）
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

# pom.xml 指定 java.version=1.8，默认使用本机已安装的 Amazon Corretto 8
JAVA_VERSION="${JAVA_VERSION:-8.0.472-amzn}"
MAVEN_GOALS="${MAVEN_GOALS:-clean package}"

usage() {
  cat <<'USAGE'
用法: ./scripts/package.sh [选项] [-- Maven 额外参数]

选项:
  -h, --help     显示帮助
  -s, --skip-clean  跳过 clean，仅执行 package -DskipTests

环境变量:
  JAVA_VERSION  覆盖 SDKMAN Java 版本（默认 8.0.472-amzn）
  MAVEN_GOALS   覆盖 Maven 目标（默认 clean package）

示例:
  ./package.sh
  ./scripts/package.sh
  ./package.sh -s
  ./package.sh -- -DskipTests=false
USAGE
}

SKIP_CLEAN=false
MAVEN_ARGS=(-DskipTests)

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--help)
      usage
      exit 0
      ;;
    -s|--skip-clean)
      SKIP_CLEAN=true
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

if [[ "${SKIP_CLEAN}" == true ]]; then
  MAVEN_GOALS="package"
fi

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
echo " RuoYi-Vue-fast 打包"
echo " 目录: ${PROJECT_ROOT}"
echo " Java: $(java -version 2>&1 | head -n 1)"
echo " Maven: $(mvn -version 2>&1 | head -n 1 | cut -d' ' -f1-3)"
echo " 目标: mvn ${MAVEN_GOALS} ${MAVEN_ARGS[*]}"
echo "========================================"

echo ">> mvn ${MAVEN_GOALS} ${MAVEN_ARGS[*]}"
mvn ${MAVEN_GOALS} "${MAVEN_ARGS[@]}"

JAR_PATH="${PROJECT_ROOT}/target/RuoYi-Vue-fast-Face.jar"
if [[ -f "${JAR_PATH}" ]]; then
  echo "========================================"
  echo " 打包成功: ${JAR_PATH}"
  ls -lh "${JAR_PATH}"
  echo "========================================"
else
  echo "错误: 未找到产物 ${JAR_PATH}" >&2
  exit 1
fi
