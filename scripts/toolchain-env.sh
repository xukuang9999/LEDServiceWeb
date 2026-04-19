#!/usr/bin/env bash

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

prepend_path() {
  if [[ -d "$1" ]]; then
    PATH="$1:$PATH"
  fi
}

if command -v brew >/dev/null 2>&1; then
  if OPENJDK_PREFIX="$(brew --prefix openjdk 2>/dev/null)"; then
    if [[ -d "$OPENJDK_PREFIX/libexec/openjdk.jdk/Contents/Home" ]]; then
      export JAVA_HOME="$OPENJDK_PREFIX/libexec/openjdk.jdk/Contents/Home"
      prepend_path "$JAVA_HOME/bin"
    fi
  fi

  if MAVEN_PREFIX="$(brew --prefix maven 2>/dev/null)"; then
    prepend_path "$MAVEN_PREFIX/bin"
  fi
fi

if [[ -d "$ROOT_DIR/.local/apache-maven-3.9.14/bin" ]]; then
  prepend_path "$ROOT_DIR/.local/apache-maven-3.9.14/bin"
fi

if [[ -d "$ROOT_DIR/.local/runtime-userfiles" ]]; then
  export ZHGLXT_PROFILE="${ZHGLXT_PROFILE:-$ROOT_DIR/.local/runtime-userfiles}"
else
  mkdir -p "$ROOT_DIR/.local/runtime-userfiles"
  export ZHGLXT_PROFILE="${ZHGLXT_PROFILE:-$ROOT_DIR/.local/runtime-userfiles}"
fi

mkdir -p "$ROOT_DIR/.local/runtime-logs"
export PATH
