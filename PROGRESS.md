# Modpack Museum — Progress Ledger

**Last updated:** 2026-09-27
**Current focus:** M0/M1 runtime gate + compile/runtime verification of the authored M2 industrial loop
**Canonical plan:** `docs/PROJECT_PLAN.md`

This file records **observed progress**, not intent. Agents must update it after meaningful work. Do not mark runtime/client checks complete unless they were actually executed and the evidence was inspected.

## Agent handoff summary

The repository now contains the full M1 presentation implementation plus an authored first M2 industrial chain: persistent Generator, Cable, BatBox and Macerator block/block-entity adapters, a Fabric lifecycle bridge into the cached pure energy topology, provisional survival recipes/loot, and the versioned pure runtimes that back machine/generator state. The Minecraft-independent core compiles and passes its smoke suite locally under Java 21. The new Minecraft/Fabric-dependent M2 layer is source-checked but cannot be compiled in this execution environment because Minecraft 26.2 requires Java 25 and the shell cannot resolve Gradle dependencies; Docker is also unavailable.

### Verified upstream baseline (2026-09-27)

- Minecraft: **26.2**
- Java: **25**
- Fabric Loader: **0.19.3**
- Fabric Loom: **1.17-SNAPSHOT**
- Fabric API: **0.156.0+26.2**
- Polymer project pin: **0.17.4+26.2**
- Latest 26.2 Polymer release observed on 2026-09-27: **0.17.5+26.2** (not upgraded here; current source checks remain against the pinned 0.17.4 API plus current docs)

The Polymer M1 patterns were cross-checked against the exact `0.17.4+26.2` Polymer source tag. The persistent block-entity/container adapter was cross-checked against current Fabric 26.2 documentation patterns (`FabricBlockEntityTypeBuilder`, `BaseEntityBlock`, `ValueInput`/`ValueOutput`, `ContainerHelper`). This is source-level compatibility evidence, **not** a substitute for compiling the project.

Re-verify before any version upgrade.

## M0–M2 overall completion estimate

These percentages are intentionally split so authored code is not confused with passed acceptance gates.

| Milestone | Engineering implementation readiness | Acceptance / runtime evidence | Gate state |
| --- | ---: | ---: | --- |
| M0 Baseline | **~80%** | **~40%** | **OPEN** — real Java-25 Gradle build + Fabric/Docker boot still missing |
| M1 Vanilla client | **~95%** | **~15%** | **OPEN** — implementation/assets exist; no clean stock-client observation yet |
| M2 Industrial core | **~75%** | **~20%** | **OPEN** — full first world loop is authored, but Java-25 compile/server/client/restart evidence is still missing |
| **M0→M2 combined** | **~83% authored/readiness** | **~25% acceptance evidence** | **0/3 gates closed** |

Interpretation: the codebase is well past the blank-project stage, but the strict product proof is still early because this environment cannot execute the Java-25/Fabric/vanilla-client checks. The combined percentages are rough engineering estimates, not release metrics.

## Milestone status

| Milestone | Status | Evidence / next gate |
| --- | --- | --- |
| M0 Baseline | **PARTIAL** | Version pins, Gradle project, Java-25 CI, Docker definition exist. Real Java-25 build/server boot remain unverified here. |
| M1 Vanilla client | **IMPLEMENTED, NOT RUNTIME-VERIFIED** | Polymer item/block/resource-pack/GUI code and assets are present. Test Machine GUI now uses persistent block-entity inventory. Must build, boot, join clean vanilla client, inspect pack/model/GUI/reconnect. |
| M2 Industrial core | **WORLD LOOP AUTHORED, NOT COMPILED/RUNTIME-VERIFIED** | Generator/Cable/BatBox/Macerator blocks + block entities, lifecycle-backed world topology, persistence, provisional recipes and pure integration proof exist. Java-25 build/server/client/restart/chunk evidence remains. |
| M3 Automation | **NOT STARTED** | No pipe/quarry implementation. |
| M4 Tekkit identity | **STARTED (FOUNDATION ONLY)** | Pure `EmcLedger` + tests and design-only sample EMC data exist. No loaded registry/player persistence/table. |
| M5 Hardening | **NOT STARTED** | Invariants identified in plan; no runtime exploit/crash/stress suite yet. |
| M6 Proof release | **NOT STARTED** | Requires M1–M5 gates. |

## Completed implementation work

### Build/dependency baseline

- Replaced speculative dependency pins with versions verified against current Fabric/Polymer sources.
- Added Polymer `core`, `blocks`, `resource-pack`, and `autohost` to the build; nested Polymer runtime modules are bundled into the project jar.
- Kept Fabric API as the external normal server dependency.
- Added JUnit 5 test configuration.
- Pinned the development container to `itzg/minecraft-server:2026.9.1-java25` rather than a floating `latest` tag.
- Added CI server-boot smoke automation (`scripts/ci-server-smoke.sh`) for use once the repository is published.

### M1 presentation implementation

- Added `MuseumItems` registry.
- Added `modpack_museum:archive_token` as a real server-defined Polymer item.
- Added `modpack_museum:iron_dust` as the first Classic Tech material.
- Added `MuseumBlocks` registry.
- Added `modpack_museum:test_machine` using Polymer textured-block state allocation.
- Added an iron-block fallback if Polymer cannot allocate a `FULL_BLOCK` visual state.
- Added server-driven right-click GUI using a vanilla three-row chest menu.
- Marked the Polymer pack required and registered project assets with the Polymer pack builder.
- Added autohost development config (`server/config/polymer/auto-host.json`).
- Updated Docker dev definition to mount Polymer config and avoid downloading a duplicate external Polymer bundle.
- Added original 16×16 Archive Token, Iron Dust, and machine textures; no historical mod art is copied.
- Added item/client-item and machine block models plus English translations.

### M2 persistence + machine core

- Replaced the transient M1 `SimpleContainer` with a registered `TestMachineBlockEntity`.
- Test Machine now owns a persistent 27-slot `NonNullList<ItemStack>` saved/loaded with `ContainerHelper`.
- Test Machine block removal now uses the current container-block removal hook to drop stored contents instead of intentionally discarding them (source-checked; runtime verification pending).
- Added explicit Test Machine persistence schema version (`museum_schema = 1`).
- Added reusable `MuseumContainer` adapter for fixed block-entity inventories.
- Added `MachineRecipe`, `MachineSnapshot`, and deterministic `ProcessingMachineRuntime`.
- Processing runtime spends energy only when progress can advance; completion consumes input and creates output atomically within the pure state transition.
- Added snapshot restore validation for capacity, recipe identity, progress, and stack bounds.
- Added pure `GeneratorRuntime` with bounded output and committed burn-time state.
- Added versioned `GeneratorSnapshot` and restore validation so committed fuel/energy survives process restart.
- Added `EnergyPort` and `EnergyNetwork`.
- Energy-network conductor components are cached and invalidated only by topology mutation.
- Machine/storage ports are terminal endpoints, so a BatBox placed between two cable runs does not become a free conductor bridge.
- Added pure `GridPos` + `GridEnergyTopology`: six-neighbour block-coordinate placement/removal now automatically connects/disconnects cached energy vertices without rescanning an unchanged world each tick.
- Transfer scheduling is deterministic, conservation-safe, and bounded by a caller-supplied maximum transfer-operation count per tick.
- Added stale-path/disconnect tests and a 500-conductor cache/conservation test.
- Expanded `scripts/test-pure-core.sh` into an end-to-end pure Generator → cable graph → BatBox buffer → Macerator-runtime smoke path.
- The pure industrial smoke now serializes generator, BatBox and Macerator state mid-process, reconstructs the network from scratch, verifies stored-energy conservation across the simulated restart, and still completes raw iron → 2× Iron Dust.
- Added the vanilla smelting half of the survival loop: Iron Dust → Iron Ingot.
- Added in-world Generator, Cable, BatBox and Macerator block/block-entity adapters, using stable vanilla proxy visuals for the first functional pass.
- Added `MuseumEnergyWorld`, which mirrors relevant block entities through Fabric load/unload lifecycle events into `GridEnergyTopology` and performs one bounded transfer pass per level tick.
- Added in-place generator/machine snapshot restoration so lifecycle registration can retain stable `EnergyBuffer` object identity even if persisted data is loaded after the block-entity load event.
- Registered all custom block-entity types with `PolymerBlockUtils.registerBlockEntity(...)` so their custom type ids are not intentionally sent to vanilla clients.
- Added provisional technology-preview crafting recipes, block loot tables and pickaxe tags for Generator/Cable/BatBox/Macerator. These values are functional scaffolding, not historical-balance claims.

### M4 correctness groundwork already present

- Added pure `EmcLedger` with no-negative-balance and overflow-safe crediting.
- Added `EmcLedgerTest` covering insufficient-balance atomicity, negative values and overflow.
- Added a design-only EMC preview JSON, explicitly not loaded and not asserted to contain historically final values.

### Planning / agent continuity

- `docs/PROJECT_PLAN.md` is the canonical immediate + complete long-term plan.
- `AGENTS.md` mandates reading/updating this ledger and now points future work at the already-authored M2 primitives rather than rebuilding them.
- `ROADMAP.md` reflects the new M2 groundwork while preserving the runtime gates.

## Validation actually performed in this session

- [x] `./scripts/test-pure-core.sh` — **PASS** under Java 21 for all Minecraft-independent energy/EMC/machine/network code, including in-place restore buffer identity.
- [x] End-to-end pure Generator → cable graph → BatBox buffer → Macerator runtime smoke — **PASS**.
- [x] Simulated mid-process restart (Generator snapshot + BatBox stored energy + Macerator snapshot + topology rebuild) — **PASS**.
- [x] Terminal-port topology regression in the pure smoke — **PASS**; Generator cannot route through the BatBox vertex directly to the output cable network.
- [x] Grid world-adapter placement/removal smoke — **PASS**; axial cable placement connects endpoints and removal immediately invalidates cached connectivity.
- [x] `./scripts/validate-static.sh` — **PASS** (JSON, version-lock parity, PNG integrity, shell syntax, required files, git diff whitespace).
- [x] `git diff --check` — **PASS**.
- [ ] Full Gradle Java 25 build — still blocked before Gradle launch because shell DNS cannot resolve `services.gradle.org` and host Java is 21.
- [ ] JUnit through Gradle — same environment blocker; JUnit sources are authored but not executed through Gradle here.
- [ ] Fabric server boot — Docker unavailable in this environment.
- [ ] Polymer pack generation.
- [ ] Docker stack boot.
- [ ] clean vanilla-client join.
- [ ] custom item visual.
- [ ] placed custom machine visual.
- [ ] machine GUI interaction.
- [ ] Test Machine inventory persistence across restart/chunk unload.
- [ ] reconnect behavior.

### Environment limitation encountered

This execution environment has:

- Java 21, not Java 25;
- no system Gradle;
- no Docker binary;
- no populated Gradle dependency cache;
- no normal shell internet/DNS for Gradle dependency downloads.

A direct attempt to inject the Gradle 9.5.1 distribution through the available file downloader also failed, so the missing build could not be recovered inside this run.

Therefore **Minecraft-dependent code has not been compiled or runtime-tested here**. M0, M1 and M2 gates remain open. Pure Java primitives can be compiled/sanity-checked under Java 21 because they intentionally depend on neither Minecraft nor Java-25-only APIs.

## Next actions — strict order

1. On a Java-25 host, run `./scripts/check-environment.sh`.
2. Run `./gradlew clean test build --stacktrace` immediately; fix any 26.2 API mismatch before authoring more Minecraft-facing M2 code.
3. Specifically verify the newly introduced `TestMachineBlockEntity`, `MuseumContainer`, `BaseEntityBlock` codec, `ValueInput`/`ValueOutput`, and block-entity registration signatures.
4. Run `./scripts/dev-up.sh` and inspect startup logs.
5. Generate/inspect the Polymer pack and complete `docs/testing/M1_VANILLA_CLIENT.md` from a clean stock 26.2 client.
6. Put items into the Test Machine, close/reopen, restart the server, unload/reload the chunk, and confirm the same stacks remain exactly once.
7. Once M1 is green, create the in-world **Generator** block/entity backed by the existing `GeneratorRuntime`/`EnergyBuffer` rules.
8. Implement the world-position **Cable adapter** around `EnergyNetwork`; invalidate on placement/break/config changes and do not rediscover unchanged topology each tick.
9. Add **BatBox** storage using `EnergyBuffer` as the persistent node state.
10. Add **Macerator** using `ProcessingMachineRuntime`; start only with raw iron → 2× Iron Dust.
11. Add the minimal survival recipe/smelting loop.
12. Run restart/chunk-unload/duplication checks, then close Gate M2 only after Generator → Cable → BatBox → Cable → Macerator survives those tests.

## Known implementation risks to check first

- Minecraft 26.2 API signatures are source-cross-checked but uncompiled in this environment.
- Confirm Polymer 0.17.4 `FULL_BLOCK` allocation and model pipeline in the generated pack.
- Confirm nested Polymer `include(...)` dependencies are sufficient at runtime and do not conflict with server-installed dependencies.
- Verify autohost through the actual server/network configuration.
- Test Machine schema handling currently recognizes schema 1 only; future format changes need explicit migration before public worlds use it.
- `EnergyNetwork` now has a pure block-coordinate adapter (`GridEnergyTopology`), but the actual Fabric `BlockPos`/chunk lifecycle bridge is still missing. Loaded/unloaded world events must mirror positions into that adapter without stale nodes.
- Generator semantics currently pause only when completely full and may waste part of a final per-tick generation quantum when nearly full. Decide whether to preserve that simple behavior or carry fractional/remaining fuel energy before balance is frozen.
- Test Machine inventory-drop handling now uses `affectNeighborsAfterRemoval`, matching the current 26.2 container-block hook shape, but it remains uncompiled/unobserved here and must be explicitly tested for player break, explosion, and piston/removal edge cases before M1 is closed.

- The first Fabric-facing Generator/Cable/BatBox/Macerator chain and `MuseumEnergyWorld` lifecycle bridge are authored but uncompiled. Exact API/source patterns were checked against current Fabric 26.2 docs and Polymer source, but no runtime claim is permitted until Java 25 build/boot succeeds.
- Cable currently uses a lightweight block entity solely so Fabric block-entity load/unload events can reconstruct topology after chunk/server load without scanning whole chunks. This is acceptable for the M2 proof but should be profiled before treating it as the long-term 500+ cable architecture.
- M2 crafting recipes and balance constants are provisional technology-preview values and are not historical-fidelity claims.

## Session log

### 2026-09-27 — M2 in-world chain continuation

**Objective:** move from pure M2 groundwork to an authored first in-world Generator → Cable → BatBox → Cable → Macerator path without overstating uncompiled code.

**Changes made:** added Generator/Cable/BatBox/Macerator blocks and block entities; Fabric block-entity lifecycle + end-level-tick energy bridge; stable in-place snapshot restoration; Polymer block-entity filtering registration; provisional M2 crafting/loot/tag data; updated M2 acceptance documentation and completion estimates.

**Evidence actually run:** pure-core smoke PASS; static repository validation PASS; `javac -Xlint:all` PASS for all Minecraft-independent core classes; `git diff --check` PASS. Fabric 26.2 block-entity ticker/lifecycle patterns and Polymer block-entity registration API were source-checked. Full Minecraft-dependent compile/boot remains blocked by Java 25/dependency/Docker environment constraints.

### 2026-09-27 — M2 core/persistence continuation

**Objective:** continue as far as possible toward M2 without falsely claiming runtime verification, and give a concrete M0–M2 completion view.

**Changes made:** persistent Test Machine block entity + inventory and authored break-drop hook; `MuseumContainer`; machine recipe/snapshot/runtime core; generator runtime + versioned generator snapshot; cached/invalidation-aware bounded energy network with terminal machine/storage endpoints + pure six-neighbour grid adapter; Iron Dust smelting recipe; expanded unit tests and restart-capable pure integration smoke; M0–M2 progress estimates; roadmap/plan/agent handoff updates.

**Tests/evidence actually run:** `./scripts/test-pure-core.sh` PASS; `./scripts/validate-static.sh` PASS; `git diff --check` PASS. The pure smoke includes Generator → network/cables → BatBox buffer → Macerator runtime, terminal-port non-bridging, grid placement/removal invalidation, exact machine/generator snapshot restore, a mid-process simulated restart, topology reconstruction, and stored-energy conservation across that restart. Current Fabric 26.2 documentation was checked for block-entity registration, persistent `ValueInput`/`ValueOutput` storage, `ContainerHelper`, and `BaseEntityBlock` patterns.

**Failures/blockers:** Java 25 unavailable; system Gradle unavailable; shell DNS cannot resolve Gradle distribution/dependency hosts; Docker unavailable; direct Gradle distribution injection failed. Consequently no Gradle JUnit, Fabric boot, generated pack, or stock-client observation occurred.

**Milestone/checklist changes:** M2 moved from minimal energy-buffer groundwork to a substantial pure core plus authored persistent Minecraft adapter. M0/M1 gates remain open because verification evidence has not changed.

**Exact next actions:** Java-25 Gradle build → fix compile mismatches → Fabric boot → M1 clean-client test → Test Machine restart persistence test → in-world Generator → Fabric world Cable adapter → BatBox → Macerator → minimal ore loop → M2 restart/chunk-load gate.

### 2026-09-27 — compatibility implementation pass

**Changes:** dependency cleanup; M1 custom Polymer items/block/GUI/assets; autohost dev config; EnergyBuffer/EmcLedger foundations; canonical project plan and progress protocol.

**Evidence:** `./scripts/validate-static.sh` PASS; earlier `./scripts/test-pure-core.sh` PASS for the initial energy/EMC primitives; environment checks correctly reported Java 21 and Docker missing; `./gradlew clean test build --stacktrace` stopped before Gradle launch because `services.gradle.org` could not resolve from the shell environment.

**Blockers:** Java 25 + online Gradle dependency resolution + Docker/runtime client test unavailable inside this execution environment.

**Next:** compile in Java 25, fix API mismatches, boot server, perform clean vanilla-client M1 acceptance test.

---

## Required format for future agent updates

Append a dated entry containing:

- **Objective**
- **Changes made**
- **Tests/evidence actually run**
- **Failures/blockers**
- **Milestone/checklist changes**
- **Exact next actions**

Never write "works", "complete", "verified", or check a runtime box solely because code/config was authored.
