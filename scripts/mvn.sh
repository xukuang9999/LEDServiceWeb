#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/toolchain-env.sh"

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven not found. Run ./scripts/install-macos-toolchain.sh first." >&2
  exit 1
fi

exec mvn "$@"
