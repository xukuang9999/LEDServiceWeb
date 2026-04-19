#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/toolchain-env.sh"

print_version() {
  local name="$1"
  local command_name="$2"

  if command -v "$command_name" >/dev/null 2>&1; then
    printf "%-12s %s\n" "$name" "$("$command_name" --version 2>/dev/null | head -n 1)"
  else
    printf "%-12s %s\n" "$name" "missing"
  fi
}

echo "Recommended baseline:"
echo "  Java runtime: OpenJDK 25.x"
echo "  Maven:        3.9.14"
echo "  Node.js:      24.x LTS"
echo

if command -v java >/dev/null 2>&1; then
  printf "%-12s %s\n" "java" "$(java -version 2>&1 | head -n 1)"
else
  printf "%-12s %s\n" "java" "missing"
fi

print_version "mvn" "mvn"
print_version "node" "node"
print_version "npm" "npm"

if [[ -n "${JAVA_HOME:-}" ]]; then
  printf "%-12s %s\n" "JAVA_HOME" "$JAVA_HOME"
fi
