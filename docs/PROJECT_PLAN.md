# Modpack Museum — Canonical Project Plan

**Last major planning revision:** 2026-09-27

This file is the canonical high-level plan for Modpack Museum. `AGENTS.md` defines how agents work; `PROGRESS.md` records what is actually complete. Never infer completion from this plan.

## Mission

Build a preservation-oriented Minecraft network where somebody who remembers classic modpacks can open a **current, unmodified Minecraft Java client**, join one server, accept an automatic resource pack, and play modern server-side recreations of those experiences.

Long-term player path:

```text
Current Minecraft Java
        ↓
play.modpackmuseum.net
        ↓
accept automatic resource pack
        ↓
The Archive
        ↓
choose a collection
        ↓
Classic Tech / Classic Adventure / Classic Warfare / later wings
        ↓
PvE or PvP where appropriate
        ↓
play — no Forge/Fabric client, no old Java, no old launcher
```

The project reimplements gameplay on modern infrastructure. It does **not** attempt to run ancient Forge binaries unchanged, and it must not redistribute code/assets without permission or a compatible license.

---

# Part I — Current Focus: Core Compatibility + Tiny Classic Tech Slice

The only question this phase needs to answer is:

> Can a completely stock modern Minecraft client play a small but convincing Tekkit-style technology loop, with an architecture we can safely extend later?

## Scope cap

Until the proof release is complete, do **not** start Hexxit/Voltz, the public proxy/lobby, PvP systems, nuclear power, ComputerCraft, RedPower, Railcraft, fluids, custom dimensions, elaborate worldgen, hundreds of recipes, or a general-purpose compatibility engine.

## Technical baseline

- Minecraft Java 26.2
- Java 25
- Fabric Loader 0.19.3
- Fabric API 0.156.0+26.2
- Polymer 0.17.4+26.2
- Server-authoritative behavior
- Automatic Polymer resource pack
- Unmodified vanilla client as the primary acceptance target

Versions are centralized in `gradle.properties`; verify before changing them.

## Architecture

```text
Stock Minecraft client
        │
        │ vanilla protocol + server resource pack
        ▼
Fabric 26.2 server
        │
        ├── Polymer presentation layer
        │     ├── custom items
        │     ├── custom blocks
        │     ├── models/textures
        │     └── later virtual entities
        │
        ├── Museum core
        │     ├── persistence/versioning
        │     ├── machine runtime
        │     ├── energy
        │     ├── item transport
        │     └── EMC
        │
        └── Classic Tech content
              ├── Generator
              ├── Cable
              ├── BatBox
              ├── Macerator
              ├── Quarry
              ├── Pipe
              └── Transmutation Table
```

Keep this as one codebase initially. Split modules only when real boundaries justify it.

## Foundational design rules

1. **Server authoritative.** The client never supplies trusted item quantities, EMC values, machine state, or energy values.
2. **Conservation invariants first.** Energy, items, and EMC cannot appear from nowhere or become negative.
3. **Persistence is a feature.** Restarts/chunk unloads must not duplicate or reset state.
4. **Dirty/cached networks.** Cable/pipe graph topology is recalculated when it changes, not rediscovered from scratch every tick.
5. **Logic and visuals are separate.** A broken animation may not corrupt item transport or quarry state.
6. **Use normal Minecraft storage where appropriate.** Machine state belongs in world/block-entity data; global player data is reserved for things such as EMC knowledge.
7. **Schema-version persistent data.** Public worlds must be migratable.
8. **Prefer Fabric/Minecraft/Polymer APIs before mixins.** Any future mixin must have a documented reason.
9. **Build abstractions after real repetition.** Do not construct a giant universal machine DSL before two machines demonstrate the common shape.
10. **Own the art we ship.** Early assets can be original approximations of the era; historical assets require license/permission review.

## Core interfaces expected to emerge

Only introduce them when needed, but the anticipated stable seams are:

- `EnergyNode` / `EnergyBuffer`
- `MachineDefinition`
- `MachineRuntime`
- `MachineRecipe`
- `ItemTransportNode` / network
- `EmcValueProvider` / registry
- `PlayerKnowledgeStore`
- `MuseumContent`

Fluids and multiblocks are deliberately later.

---

## Milestone M0 — Baseline

Goal: a reproducible modern Fabric development environment.

- [ ] Java 25 build verified locally or in CI
- [ ] `./gradlew build` succeeds
- [ ] Fabric 26.2 server boots
- [x] current Fabric/Polymer versions pinned centrally
- [x] Java 25 CI workflow present
- [x] Docker development-server definition present
- [ ] Docker development server runtime verified
- [ ] clean startup log archived/checked

**Gate:** do not claim the runtime works until a real Java 25 build and server boot have been observed.

## Milestone M1 — Vanilla-client presentation

Goal: prove the primary product promise before machine engineering.

Required:

- [x] custom `modpack_museum:archive_token` registration implemented
- [x] custom `modpack_museum:iron_dust` registration implemented
- [x] project-owned item textures/models included
- [x] custom `modpack_museum:test_machine` Polymer textured block implemented
- [x] project-owned machine textures/model included
- [x] server-driven vanilla chest GUI interaction implemented
- [x] Polymer resource pack marked required and project assets registered
- [x] autohost config supplied for development server
- [ ] actual pack generation verified
- [ ] actual vanilla 26.2 join verified
- [ ] item visual verified in vanilla client
- [ ] placed block visual verified in vanilla client
- [ ] GUI verified in vanilla client
- [ ] disconnect/reconnect smoke test

**Gate M1:** a completely unmodified 26.2 client joins, receives the pack automatically, and interacts with custom items/block/GUI.

## Milestone M2 — Industrial core

Goal: create the smallest real IC2-like loop.

Build in this order:

1. Persistent machine block entity/runtime.
2. Generic machine inventory and simple vanilla-compatible GUI.
3. Generator: fuel → energy.
4. Energy network and cable.
5. BatBox storage.
6. Macerator: item + energy + time → output.
7. Minimal survival recipes.
8. Ore/dust/smelting loop.

Current preparatory work:

- [x] pure `EnergyBuffer` primitive implemented
- [x] conservation/clamping tests authored
- [x] deterministic single-recipe `ProcessingMachineRuntime` authored
- [x] versioned `MachineSnapshot` persistence DTO + restart round-trip tests authored
- [x] persistent Test Machine block entity/inventory adapter authored (compile/runtime verification pending)
- [x] pure `GeneratorRuntime` fuel-to-energy state machine authored
- [x] versioned `GeneratorSnapshot` + restore validation authored
- [x] cached, invalidating, bounded `EnergyNetwork` topology core authored
- [x] machine/storage ports are terminal endpoints and cannot accidentally bridge separate cable components
- [x] pure six-neighbour `GridEnergyTopology` world-position adapter authored with placement/removal invalidation tests
- [x] 500-conductor cache/conservation unit test authored
- [x] pure Generator → Cable → BatBox → Cable → Macerator restart simulation passes
- [x] Iron Dust → Iron Ingot vanilla smelting recipe authored for the downstream half of the ore loop
- [x] in-world Generator block/entity authored (compile/runtime verification pending)
- [x] in-world Cable block entity + Fabric lifecycle/world-topology bridge authored (compile/runtime verification pending)
- [x] in-world BatBox block/entity authored (compile/runtime verification pending)
- [x] in-world Macerator block/entity authored (compile/runtime verification pending)
- [x] provisional M2 survival recipes + raw-iron → dust → ingot data path authored (runtime verification pending; historical balance not frozen)
- [ ] restart/chunk-unload runtime verification

Do not implement historical voltage explosions, transformers, cable loss, or multiple tiers yet. Design the interfaces so those rules can be layered later.

**Gate M2:** Generator → Cable → BatBox → Cable → Macerator works, including restart/chunk reload.

## Milestone M3 — Automation

Goal: prove networked logistics and authoritative world automation.

### First pipe

Support only inventory → pipe graph → inventory. Separate authoritative item state from travelling-item visuals. Defer filters, diamond pipes, gates, priorities, and advanced extraction.

### First quarry

Start with a fixed small footprint (for example 5×5 or 9×9):

- needs energy
- scans deterministically downward
- breaks blocks authoritatively
- captures drops
- outputs to connected inventory/pipe
- handles bedrock, liquids, containers, full outputs, unloads and restart
- never duplicates drops during crashes/retries

Transaction order must be designed to prevent "block broken + drops duplicated" failure modes.

- [ ] pipe graph
- [ ] chest-to-chest transport
- [ ] quarry state machine
- [ ] quarry energy use
- [ ] quarry output to pipe/chest
- [ ] restart/crash tests

**Gate M3:** Generator powers Quarry; Quarry mines; Pipe reliably delivers its drops to a chest across restart.

## Milestone M4 — Tekkit identity

Goal: add the interaction that makes the slice recognizably Tekkit-like rather than generic automation.

Build:

- data-driven EMC registry
- per-player EMC balance
- learned-item knowledge
- Transmutation Table using vanilla-compatible GUI semantics
- a small curated list of EMC-enabled vanilla items
- original period-inspired art

Current preparatory work:

- [x] pure `EmcLedger` primitive implemented
- [x] negative-balance/overflow tests authored
- [x] design-only example EMC data file added
- [ ] loaded EMC registry
- [ ] persistent player ledger
- [ ] knowledge store
- [ ] Transmutation Table

EMC transactions must be atomic and server-authoritative. Shift-clicking, full inventories, disconnects and rapid clicks are explicit exploit cases.

**Gate M4 — “the Tekkit moment”:** give an old Tekkit player the vanilla client and server IP; the Generator/Cable/BatBox/Macerator/Quarry/Pipe/Transmutation vocabulary should be immediately recognizable.

## Milestone M5 — Hardening

- [ ] duplication/exploit suite
- [ ] crash/restart suite
- [ ] chunk unload/reload suite
- [ ] deterministic resource-pack build/checksum
- [ ] clean-install deployment test
- [ ] migration/schema tests
- [ ] diagnostics (`/museum debug ...`)
- [ ] stress test: 100 machines / 500 cables / 200 pipes / 5 quarries / 2 players
- [ ] target 20 TPS under that development stress case
- [ ] documented known limitations

## Milestone M6 — Proof release

**Modpack Museum — Classic Tech Technology Preview v0.1**

This is the point where we decide whether to expand into full Classic Tech or revise the compatibility architecture.

### Final proof playthrough

1. Start with a clean stock Minecraft 26.2 client.
2. Add server IP and join.
3. Accept the server resource pack.
4. Gather ordinary materials.
5. Craft Generator.
6. Craft Cable.
7. Craft BatBox.
8. Craft Macerator.
9. Generate energy.
10. Macerate ore/dust.
11. Smelt the processed output.
12. Craft/build Quarry.
13. Power it.
14. Observe it mine blocks.
15. Connect Pipe.
16. Observe items reach Chest.
17. Build Transmutation Table.
18. Sacrifice items for EMC.
19. Recreate learned items.
20. Disconnect.
21. Restart the entire server.
22. Rejoin.
23. Verify machine inventories, energy, quarry progress, EMC and learned items persisted.
24. Verify no duplication/loss occurred.
25. Confirm zero client mods were installed at any point.

---

# Part II — Full Modpack Museum Roadmap

The vertical slice is not the final product. Once the compatibility layer is proven, proceed broadly in this order.

## 1. Full Classic Tech

Expand the proven slice into a substantial Tekkit Classic-era recreation:

- IC2-inspired machines and power progression
- BuildCraft-inspired quarries, pipes and automation
- EE2-inspired EMC ecosystem
- RedPower-inspired logic/transport where practical
- powered tools and armor
- advanced generators/storage
- nuclear-reactor gameplay
- broader recipes/progression
- recipe/discoverability system usable by vanilla clients

Historical behavior is a specification, not a source-code shortcut. Document fidelity and intentional deviations.

## 2. The Archive network

Create the public network once at least one collection is genuinely worth entering:

```text
play.modpackmuseum.net
        ↓
Velocity
        ↓
The Archive lobby
        ↓
collection selector
        ↓
collection backend/world
```

Resource-pack transitions must be automatic. The lobby should be clean and museum-like rather than a monetized-server hub.

## 3. PvE and PvP modes

Collections may offer distinct modes where historically appropriate.

**PvE/co-op:** permanent world, long-lived factories/builds, protection/rollback, optional-consent PvP.

**PvP/seasonal:** teams, raiding/destructible infrastructure, season resets where they improve play, and weapon systems enabled only when appropriate.

Do not merely duplicate identical worlds with `pvp=true`.

## 4. Classic Adventure

Hexxit-era preservation target:

- large dungeons and battle towers
- exploration-biased world generation
- custom enemies and bosses
- unusual/rare equipment
- loot-heavy progression
- dimension-like adventure spaces
- co-op first, PvP variant only if it produces a coherent experience

## 5. Classic Warfare

Voltz-era preservation target:

- industrial electricity
- reactors
- radiation
- missiles/launch systems
- radar
- forcefields
- large-scale industrial warfare
- strong PvP/teams mode

Core energy/network/multiblock systems should be reused rather than rewritten.

## 6. Preservation and fidelity program

For every recreated historical system:

- identify the original version/pack target
- document recipes, behavior and progression from legitimate sources
- record implementation differences and reasons
- audit code license separately from textures/sounds/names
- request permission where necessary
- create project-owned replacement assets when redistribution is not allowed
- avoid implying official affiliation

Planned fidelity docs live under `docs/fidelity/`.

## 7. Productionization

Before broad public launch:

- reproducible deployment
- monitoring/alerting
- automated backups and restore drills
- moderation and anti-exploit controls
- rollback/admin tooling
- player tutorials
- website/status information
- migration policy
- load/performance testing
- contribution/review process

## 8. Later museum wings

After the first three collections are healthy, evaluate historically significant experiences such as:

- classic skyblock / Agrarian Skies-style progression
- old magic-pack experiences
- Attack of the B-Team-style collections
- Yogbox-era adventure
- other community-requested packs

Each wing needs technical feasibility, licensing and demand review before implementation.

---

# Development workflow

Prefer small milestone-owned changes. A useful sequence for the current slice is:

1. vanilla client + pack + custom item
2. custom block + GUI
3. persistent machine primitive
4. Generator
5. energy network + Cable
6. BatBox
7. Macerator
8. Pipe
9. Quarry
10. EMC registry
11. Transmutation Table
12. vertical-slice integration/fidelity pass

Every meaningful PR/change should state:

- objective
- requirements
- non-goals
- tests/evidence
- acceptance criteria

A feature is not complete because it compiles. At minimum consider build, startup, vanilla representation, persistence, duplication, logs and documentation.
