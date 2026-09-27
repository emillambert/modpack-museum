#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
VERSION="9.5.1"
DIST="$ROOT/.gradle-dist/gradle-$VERSION"
ZIP="$ROOT/.gradle-dist/gradle-$VERSION-bin.zip"
if [[ ! -x "$DIST/bin/gradle" ]]; then
  mkdir -p "$ROOT/.gradle-dist"
  if [[ ! -f "$ZIP" ]]; then
    echo "Bootstrapping Gradle $VERSION..."
    curl -fL "https://services.gradle.org/distributions/gradle-$VERSION-bin.zip" -o "$ZIP"
  fi
  rm -rf "$DIST"
  unzip -q "$ZIP" -d "$ROOT/.gradle-dist"
fi
exec "$DIST/bin/gradle" "$@"
