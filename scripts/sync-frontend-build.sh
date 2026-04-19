#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_DIR="$ROOT_DIR/frontend/dist"
TARGET_DIRS=(
  "$ROOT_DIR/zhglxt-web/src/main/resources/static/modern"
  "$ROOT_DIR/cloudrun-static/site/zhglxt/modern"
)

if [[ ! -d "$DIST_DIR" ]]; then
  echo "Missing frontend build output at $DIST_DIR" >&2
  exit 1
fi

for target_dir in "${TARGET_DIRS[@]}"; do
  mkdir -p "$target_dir"
  rsync -a --delete "$DIST_DIR"/ "$target_dir"/
done
