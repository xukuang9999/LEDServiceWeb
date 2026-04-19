#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

export DB_URL="${DB_URL:-jdbc:mysql://localhost:3306/zhglxt?useUnicode=true&characterEncoding=utf8&zeroDateTimeBehavior=convertToNull&useSSL=false&serverTimezone=Australia%2FMelbourne}"
export DB_USERNAME="${DB_USERNAME:-root}"
export DB_PASSWORD="${DB_PASSWORD:-}"
export SERVER_PORT="${SERVER_PORT:-8889}"
export SERVER_CONTEXT_PATH="${SERVER_CONTEXT_PATH:-/zhglxt}"
export SPRING_PROFILES_ACTIVE="${SPRING_PROFILES_ACTIVE:-druid-dev,mybatis-dev,shiro-dev,doc-dev,file-manager}"

exec "$SCRIPT_DIR/mvn.sh" \
  -pl zhglxt-web \
  -am \
  spring-boot:run \
  "-Dspring-boot.run.arguments=--spring.profiles.active=${SPRING_PROFILES_ACTIVE} --server.port=${SERVER_PORT} --server.servlet.context-path=${SERVER_CONTEXT_PATH}"
