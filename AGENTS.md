# Codex / Agent Instructions

You are working on **Modpack Museum** — a preservation-oriented modern Minecraft project whose defining product constraint is that players can join with an **unmodified current Minecraft Java client**.

## Mandatory startup protocol

Before changing anything, read these files in order:

1. `AGENTS.md` (this file)
2. `PROGRESS.md` — source of truth for what is actually complete/tested
3. `docs/PROJECT_PLAN.md` — canonical immediate + long-term plan
4. `ROADMAP.md` — concise release roadmap
5. `docs/architecture.md` and `docs/legal-and-attribution.md` when relevant

Do not infer status from old chat context, issue titles, or planned code. `PROGRESS.md` wins.

## Mandatory shutdown / handoff protocol

After any meaningful code, configuration, architecture, testing, dependency, or documentation work:

1. update `PROGRESS.md` in the same change;
2. record what changed;
3. record the tests/evidence that **actually ran**;
4. record blockers/failures honestly;
5. update milestone checkboxes only when evidence justifies it;
6. leave explicit next actions for the next agent.

Never mark a runtime/client feature complete because files exist or code compiles. Distinguish **authored**, **compiled**, **server-boot verified**, and **vanilla-client verified**.

## Product goal

The long-term experience is:

```text
Current Minecraft Java
  → play.modpackmuseum.net
  → automatic resource pack
  → The Archive lobby
  → choose Classic Tech / Classic Adventure / Classic Warfare / later collection
  → choose PvE or PvP where appropriate
  → play without installing Forge, Fabric, a modpack, old Java, or a legacy launcher
```

The full roadmap — Classic Tech, The Archive, PvE/PvP, Classic Adventure, Classic Warfare, preservation/fidelity, productionization, and later museum wings — is maintained in `docs/PROJECT_PLAN.md`.

### Rough complete-program sequence

Keep this long-term sequence in mind, but do not skip the current gates:

1. **Core compatibility proof** — stock modern client, Polymer presentation, automatic resource pack, custom item/block/GUI, then the tiny Classic Tech vertical slice.
2. **Full Classic Tech** — expand the proven primitives into the recognizable Tekkit-era industrial experience: IC2-style power/machines, BuildCraft-style quarry/logistics, EE2-style EMC, then selected later systems where fidelity and licensing allow.
3. **The Archive network** — one public address and lobby that routes players to isolated collection servers/resource packs without requiring a launcher or client mod.
4. **PvE and PvP modes** — persistent cooperative worlds plus competitive/seasonal worlds where the historical pack naturally supports them.
5. **Classic Adventure** — Hexxit-era exploration: large structures/dungeons, loot, bosses, equipment, mobs, and dimensions, recreated server-side.
6. **Classic Warfare** — Voltz-era industry/warfare: electricity, reactors, radiation, missiles, radar, forcefields, and related systems, recreated server-side.
7. **Preservation/fidelity program** — documented historical behavior, provenance, recipe/balance references, per-component license/permission decisions, original replacement assets where redistribution is not permitted, and clear non-affiliation/attribution.
8. **Productionization** — deployment automation, monitoring, backups, moderation, exploit/security hardening, migrations, documentation, website/status surfaces, and contributor workflows.
9. **Additional museum wings** — skyblock, magic, B-Team/Yogbox-era and other historically important experiences, only after the common platform and preservation process are mature.

The product test at every stage remains: a player should be able to use the current stock Minecraft Java client, join, accept server resources, and play.

## Current scope — enforce aggressively

Current work is **only** the core compatibility layer and tiny Classic Tech/Tekkit-style vertical slice through M6 in `docs/PROJECT_PLAN.md`.

Do **not** begin these until the technology preview proves the foundation:

- Hexxit / Classic Adventure implementation
- Voltz / Classic Warfare implementation
- Velocity / public Archive lobby
- PvP infrastructure
- nuclear reactors
- ComputerCraft recreation
- RedPower/Railcraft breadth
- fluids/multiblocks beyond a demonstrated need
- elaborate world generation
- mass recipe/content import

The narrow target is Generator → Cable → BatBox → Macerator plus Quarry → Pipe → Chest plus EMC/Transmutation, all usable from a stock client.

## Current technical baseline

As last verified in `PROGRESS.md` on 2026-09-27:

- Minecraft 26.2
- Java 25
- Fabric Loader 0.19.3
- Fabric Loom 1.17-SNAPSHOT
- Fabric API 0.156.0+26.2
- Polymer 0.17.4+26.2

Do not casually upgrade versions. Verify compatibility and update `PROGRESS.md` with evidence.

## Non-negotiable constraints

- Vanilla clients must not need Fabric, Forge, NeoForge, or a modpack.
- Prefer Polymer for server-defined blocks/items/entities/resource-pack behavior.
- Behavior is server-authoritative; never trust client quantities/state.
- Do not copy or redistribute old closed/restricted mod code, textures, sounds, or other assets.
- Do not claim affiliation with Technic, Tekkit, Hexxit, Voltz, or historical mod authors.
- Persistent state requires explicit schema/version/migration thinking before public release.
- Avoid unnecessary work on the main server tick.
- Network topology should be cached/invalidated, not globally rediscovered every tick.
- Separate authoritative game logic from visual presentation/animation.
- Prefer Fabric/Minecraft/Polymer APIs over mixins; document any mixin that becomes necessary.
- Keep the codebase one module until real complexity justifies a split.

## Core correctness invariants

### Energy
- cannot appear from nowhere;
- cannot be negative;
- storage cannot exceed capacity.

### Items
- cannot duplicate;
- cannot silently disappear during normal operations/restart.

### EMC
- balance cannot become negative;
- transactions must be server-authoritative and atomic at the interaction level;
- overflow must fail safely.

### Networks
- topology invalidates on relevant placement/break/config changes;
- stale disconnected paths must not continue transferring.

### Persistence
- restart/chunk unload must not reset, replay or duplicate committed machine operations.

Tests should target these invariants, not only happy-path visuals.

## Architecture direction

Expected stable seams, introduced only as needed:

- `EnergyBuffer` / `EnergyNode`
- `MachineDefinition`
- `MachineRuntime`
- `MachineRecipe`
- `ItemTransportNode` / network
- `EmcRegistry` / value provider
- `PlayerKnowledgeStore`
- `MuseumContent`

Later only: fluid networks, multiblocks, broad compatibility layers.

**Do not over-engineer.** Build the first real Generator/Macerator/Pipe/etc., observe duplication, then extract abstractions. AI-generated framework breadth is not progress unless it supports an acceptance-test feature.

## Testing philosophy

For new systems consider, as applicable:

- unit tests for pure logic
- Java 25 Gradle build
- actual server boot
- startup-log inspection
- vanilla-client representation
- automatic pack delivery
- save/restart persistence
- chunk unload/reload
- invalid-state recovery
- duplication/loss exploits
- performance under many instances

A feature is not complete because it compiles.

## Immediate engineering priority

The first M2 world chain is now **authored but uncompiled**. Do not rebuild it from scratch. Close the runtime gates in this order:

1. run Java 25 Gradle build/tests and repair any Fabric/Polymer 26.2 API mismatches;
2. boot the Fabric 26.2 server and inspect startup/resource-pack logs;
3. join from a completely stock 26.2 client and close M1 (Archive Token, Iron Dust, Test Machine visual/GUI/persistence);
4. exercise the authored Generator → Cable → BatBox → Cable → Macerator chain;
5. verify coal generation, bounded transfer, raw iron → exactly 2 Iron Dust, and dust → ingot smelting;
6. break/replace cables and confirm topology invalidates/reconnects;
7. restart server and unload/reload chunks with partial generator/batbox/macerator state;
8. inspect duplication/loss and energy conservation evidence;
9. only after M2 passes, proceed to M3 Pipe/Quarry work.
10. update `PROGRESS.md` with evidence.

M2 now already includes the authored Generator/Cable/BatBox/Macerator world adapters, Fabric lifecycle-backed topology bridge, persistence DTOs/runtimes, and provisional survival recipes. None of that Minecraft-dependent M2 layer is compiled or runtime-verified yet. Future agents should repair/verify this implementation rather than replacing it with a parallel machine system.

## Evidence discipline

Use these terms precisely:

- **implemented/authored** — code/config exists;
- **static-validated** — parsers/lint/manual structural checks ran;
- **compiled** — Java/Gradle compile passed;
- **server-verified** — real server boot/behavior inspected;
- **client-verified** — clean stock client behavior observed;
- **complete** — milestone acceptance criterion satisfied.

When environment limitations prevent a test, state that in `PROGRESS.md`; never fill the gap with assumptions.
