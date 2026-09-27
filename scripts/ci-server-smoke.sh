#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

log_file="${SMOKE_LOG:-server-smoke.log}"
timeout_seconds="${SMOKE_TIMEOUT:-240}"

cleanup() {
  docker compose -f server/compose.yml logs --no-color minecraft > "$log_file" 2>&1 || true
  docker compose -f server/compose.yml down -v >/dev/null 2>&1 || true
}
trap cleanup EXIT

SKIP_BUILD=1 ./scripts/dev-up.sh

deadline=$((SECONDS + timeout_seconds))
while (( SECONDS < deadline )); do
  state=$(docker inspect -f '{{.State.Status}}' modpack-museum-dev 2>/dev/null || true)
  if [[ "$state" == "exited" || "$state" == "dead" ]]; then
    echo "Minecraft container exited before readiness" >&2
    docker compose -f server/compose.yml logs --no-color minecraft >&2 || true
    exit 1
  fi

  logs=$(docker compose -f server/compose.yml logs --no-color minecraft 2>/dev/null || true)
  if grep -Eq 'Done \([0-9.]+s\)!' <<<"$logs"; then
    echo "Server smoke test: PASS"
    if grep -Eiq '(Exception|ERROR|Failed to load|Could not load)' <<<"$logs"; then
      echo "Server reached readiness but error-like log lines were detected:" >&2
      grep -Ein '(Exception|ERROR|Failed to load|Could not load)' <<<"$logs" >&2 || true
      exit 1
    fi
    exit 0
  fi

  sleep 5
done

echo "Timed out waiting for Minecraft server readiness after ${timeout_seconds}s" >&2
docker compose -f server/compose.yml logs --no-color minecraft >&2 || true
exit 1
