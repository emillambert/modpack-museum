#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

python - <<'PY'
import json
from pathlib import Path

for p in sorted(Path('.').rglob('*.json')):
    if any(part in {'.git', 'server/runtime', 'build', '.gradle-dist'} for part in p.parts):
        continue
    try:
        json.loads(p.read_text())
    except UnicodeDecodeError:
        continue
    except json.JSONDecodeError as exc:
        raise SystemExit(f'Invalid JSON: {p}: {exc}')
print('JSON: OK')

# Keep the human-readable version lock and build properties synchronized.
lock = json.loads(Path('versions.lock.json').read_text())
props = {}
for line in Path('gradle.properties').read_text().splitlines():
    if '=' in line and not line.lstrip().startswith('#'):
        key, value = line.split('=', 1)
        props[key.strip()] = value.strip()
expected = {
    'minecraft': props['minecraft_version'],
    'fabric_loom': props['loom_version'],
    'fabric_loader': props['loader_version'],
    'fabric_api': props['fabric_api_version'],
    'polymer': props['polymer_version'],
}
for key, value in expected.items():
    if str(lock.get(key)) != str(value):
        raise SystemExit(f'versions.lock.json mismatch for {key}: {lock.get(key)!r} != {value!r}')
print('Version lock parity: OK')

import struct
for p in sorted(Path('src/main/resources/assets').rglob('*.png')):
    data = p.read_bytes()
    if data[:8] != b"\x89PNG\r\n\x1a\n":
        raise SystemExit(f"Invalid PNG signature: {p}")
    if len(data) < 24 or data[12:16] != b"IHDR":
        raise SystemExit(f"Invalid PNG IHDR: {p}")
    width, height = struct.unpack(">II", data[16:24])
    if width <= 0 or height <= 0:
        raise SystemExit(f"Invalid PNG dimensions: {p}")
print('PNG assets: OK')
PY

for f in scripts/*.sh; do
  bash -n "$f"
done
echo 'Shell syntax: OK'

git diff --check
echo 'git diff whitespace: OK'

required=(
  PROGRESS.md
  docs/PROJECT_PLAN.md
  src/main/resources/assets/modpack_museum/items/archive_token.json
  src/main/resources/assets/modpack_museum/items/test_machine.json
  server/config/polymer/auto-host.json
  docs/testing/M2_INDUSTRIAL_CORE.md
  src/main/java/net/modpackmuseum/core/world/MuseumEnergyWorld.java
  src/main/java/net/modpackmuseum/core/blockentity/GeneratorBlockEntity.java
  src/main/java/net/modpackmuseum/core/blockentity/CableBlockEntity.java
  src/main/java/net/modpackmuseum/core/blockentity/BatBoxBlockEntity.java
  src/main/java/net/modpackmuseum/core/blockentity/MaceratorBlockEntity.java
  src/main/resources/data/modpack_museum/recipe/generator.json
  src/main/resources/data/modpack_museum/recipe/cable.json
  src/main/resources/data/modpack_museum/recipe/batbox.json
  src/main/resources/data/modpack_museum/recipe/macerator.json
  src/main/resources/data/modpack_museum/recipe/iron_ingot_from_iron_dust.json
)
for f in "${required[@]}"; do
  [[ -f "$f" ]] || { echo "Missing required file: $f" >&2; exit 1; }
done
echo 'Required files: OK'
