# M1 Vanilla-Client Acceptance Test

This is the mandatory manual gate before M1 can be marked complete in `PROGRESS.md`.

## Preconditions

- Host uses Java 25.
- Docker + Compose are available (or equivalent Fabric server is configured).
- `./gradlew clean test build` passes.
- No Fabric/Forge/NeoForge installation exists on the test client; use a clean stock Minecraft 26.2 profile.

## Server setup

```bash
./scripts/check-environment.sh
./scripts/validate-static.sh
./gradlew clean test build --stacktrace
./scripts/dev-up.sh
./scripts/dev-logs.sh
```

Inspect startup logs. Treat dependency failures, registry errors, Polymer exceptions, resource-pack build errors, or repeated warnings from Modpack Museum as failures to investigate.

## Pack generation check

From the server console/operator account:

```text
/polymer generate-pack
```

Confirm Polymer reports success and the generated pack contains at least:

- `assets/modpack_museum/items/archive_token.json`
- `assets/modpack_museum/items/iron_dust.json`
- `assets/modpack_museum/items/test_machine.json`
- corresponding models/textures/lang data

Record the pack SHA-256 in the test notes if available.

## Clean-client test

1. Launch stock Minecraft Java 26.2.
2. Join `localhost:25565` (or the dev host address).
3. Confirm the server resource pack is automatically offered/required and loads successfully.
4. Run `/museum status` and confirm the server identifies the 26.2/Fabric/Polymer target.
5. Run `/give @s modpack_museum:archive_token`.
6. Confirm it is named **Archive Token**, uses the project texture, can be moved/dropped/stored, and does not turn into its vanilla Polymer carrier item.
7. Run `/give @s modpack_museum:iron_dust` and repeat the representation/inventory check.
8. Run `/give @s modpack_museum:test_machine`.
9. Place the machine. Confirm the custom machine model is visible rather than the fallback/donor state.
10. Break and replace it. Confirm correct item/drop identity.
11. Right-click the machine with an empty hand. Confirm a vanilla three-row chest GUI titled **Museum Test Machine** opens.
12. Put a distinctive small set of vanilla items into several GUI slots, close the GUI, reopen it, and confirm the exact stacks remain.
13. Disconnect and reconnect. Confirm item identities and the Test Machine inventory remain correct.
14. Restart the server. Rejoin and confirm the placed custom block still loads/renders correctly and its inventory remains exactly once.
15. Move far enough away to unload the chunk (or use a controlled chunk-unload procedure), return, and confirm the inventory is unchanged.

## Failure evidence to capture

For any failure record:

- exact server commit
- Java/Minecraft/Fabric/Polymer versions
- server log excerpt
- client disconnect/resource-pack message
- reproduction steps
- whether a modded Polymer client behaves differently from a stock client (only as a diagnostic; stock-client behavior remains the gate)

## Pass criterion

M1 passes only when the clean stock client successfully completes the entire path: **join → automatic pack → custom item → custom block → server-driven GUI → reconnect/restart**, with no client mod installation.

After passing, update `PROGRESS.md` with the date and actual evidence before starting M2.
