#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

fail=0

printf 'Modpack Museum environment check\n'
printf '%-18s %s\n' 'Repository:' "$(pwd)"

if command -v java >/dev/null 2>&1; then
  java_line=$(java -version 2>&1 | head -n1)
  java_major=$(java -version 2>&1 | sed -n '1s/.*version "\([0-9][0-9]*\).*/\1/p')
  printf '%-18s %s\n' 'Java:' "$java_line"
  if [[ "${java_major:-0}" -lt 25 ]]; then
    echo 'ERROR: Java 25+ is required for the Minecraft 26.2 build.' >&2
    fail=1
  fi
else
  echo 'ERROR: java not found' >&2
  fail=1
fi

if command -v docker >/dev/null 2>&1; then
  printf '%-18s %s\n' 'Docker:' "$(docker --version)"
  if docker compose version >/dev/null 2>&1; then
    printf '%-18s %s\n' 'Docker Compose:' "$(docker compose version --short 2>/dev/null || docker compose version)"
  else
    echo 'ERROR: docker compose plugin not available' >&2
    fail=1
  fi
else
  echo 'WARN: docker not found (builds may work; dev server cannot).' >&2
fi

if [[ -x ./gradlew ]]; then
  printf '%-18s %s\n' 'Gradle wrapper:' './gradlew present'
else
  echo 'ERROR: ./gradlew missing/not executable' >&2
  fail=1
fi

printf '%-18s %s\n' 'Minecraft:' "$(grep '^minecraft_version=' gradle.properties | cut -d= -f2)"
printf '%-18s %s\n' 'Fabric Loader:' "$(grep '^loader_version=' gradle.properties | cut -d= -f2)"
printf '%-18s %s\n' 'Fabric API:' "$(grep '^fabric_api_version=' gradle.properties | cut -d= -f2)"
printf '%-18s %s\n' 'Polymer:' "$(grep '^polymer_version=' gradle.properties | cut -d= -f2)"

exit "$fail"
