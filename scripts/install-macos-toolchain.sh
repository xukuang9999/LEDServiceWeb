#!/usr/bin/env bash
set -euo pipefail

if [[ "$(uname -s)" != "Darwin" ]]; then
  echo "This helper currently targets macOS only." >&2
  exit 1
fi

if ! command -v brew >/dev/null 2>&1; then
  echo "Homebrew is required for this installer." >&2
  exit 1
fi

packages=(openjdk maven)

if [[ "${1:-}" == "--with-node" ]]; then
  packages+=(node)
fi

brew install "${packages[@]}"
