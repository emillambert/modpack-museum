# v0.1: Bootstrap Fabric + Polymer runtime

## Goal
Produce a reproducible Minecraft 26.2 / Java 25 Fabric server that loads Modpack Museum and Polymer and accepts an unmodified client.

## Acceptance criteria
- `./gradlew build` passes.
- `./scripts/dev-up.sh` starts the server.
- no dependency or registry errors in logs.
- vanilla 26.2 client can join.
- `/museum` responds.
- exact smoke-test steps documented.

## Notes
Use the current stable 26.2 Polymer API. Keep dependency versions centralized in `gradle.properties`.
