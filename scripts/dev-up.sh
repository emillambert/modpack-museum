#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

./scripts/check-environment.sh
./scripts/validate-static.sh

if [[ "${SKIP_BUILD:-0}" != "1" ]]; then
  ./gradlew build
fi

mkdir -p server/local-mods
rm -f server/local-mods/modpack-museum-*.jar
JAR=$(find build/libs -maxdepth 1 -type f -name 'modpack-museum-*.jar' ! -name '*-sources.jar' | head -n1)
if [[ -z "${JAR:-}" ]]; then
  echo "No built Modpack Museum jar found in build/libs" >&2
  exit 1
fi
cp "$JAR" server/local-mods/

docker compose -f server/compose.yml up -d

echo "Development server starting on localhost:25565"
echo "Use: docker compose -f server/compose.yml logs -f minecraft"
