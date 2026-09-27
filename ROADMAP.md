# Roadmap

This is the concise release view. See `docs/PROJECT_PLAN.md` for the canonical detailed plan and `PROGRESS.md` for actual status/evidence.

## M0 — Baseline

- [ ] Java 25 `./gradlew clean test build` verified
- [ ] Fabric 26.2 server boot verified
- [x] Fabric/Polymer versions pinned
- [x] Java 25 CI definition
- [x] Docker development-server definition

**Exit:** reproducible build + clean server boot.

## M1 — Vanilla Client

- [x] Polymer custom Archive Token authored
- [x] Iron Dust custom material authored
- [x] project-owned item assets authored
- [x] Polymer textured Test Machine authored
- [x] project-owned machine assets authored
- [x] server-driven vanilla GUI authored
- [x] automatic resource-pack/autohost configuration authored
- [x] block loot/pickaxe data authored
- [ ] Gradle compile/test passes
- [ ] generated Polymer pack inspected
- [ ] clean stock 26.2 client join verified
- [ ] item/block models verified in stock client
- [ ] GUI interaction verified
- [ ] restart/reconnect acceptance test verified

**Exit:** a stock 26.2 client joins and interacts with server-defined content without client mods.

## M2 — Industrial Core

- [x] pure energy-buffer conservation primitive/test authored
- [x] pure deterministic processing-machine runtime + versioned snapshot authored
- [x] persistent Test Machine block entity/inventory adapter authored (uncompiled)
- [x] pure Generator fuel-to-energy runtime authored
- [x] cached/bounded cable-network topology core authored
- [x] topology invalidation + 500-conductor tests authored
- [x] in-world Generator block/entity authored (uncompiled)
- [x] in-world Cable + lifecycle/world-topology adapter authored (uncompiled)
- [x] in-world BatBox-like storage authored (uncompiled)
- [x] in-world Macerator authored (uncompiled)
- [x] provisional crafting + raw-iron → 2 dust → ingot loop authored (unverified)
- [ ] persistence/chunk unload runtime tests

**Exit:** Generator → Cable → BatBox → Macerator works reliably across restart.

## M3 — Automation

- [ ] basic inventory-to-inventory item pipe
- [ ] cached item graph
- [ ] fixed-footprint Quarry
- [ ] energy-powered mining
- [ ] Pipe → Chest output
- [ ] crash/restart/duplication tests

**Exit:** powered Quarry mines and delivers drops through Pipes without duplication/loss.

## M4 — Tekkit Identity

- [x] pure EMC ledger primitive/test authored
- [x] design-only example EMC data authored
- [ ] loaded EMC registry
- [ ] player EMC persistence
- [ ] learned-item knowledge
- [ ] Transmutation Table
- [ ] minimal survival recipes/progression
- [ ] period-inspired original asset pass

**Exit:** an old Tekkit player recognizes the interaction vocabulary without needing implementation explanation.

## M5 — Hardening

- [ ] exploit/duplication suite
- [ ] crash/restart and chunk reload suite
- [ ] deterministic resource-pack validation
- [ ] clean-install deployment test
- [ ] migration/schema tests
- [ ] debug commands
- [ ] development stress test / 20 TPS target

## M6 — Classic Tech Technology Preview v0.1

Run the complete clean-client proof playthrough in `docs/PROJECT_PLAN.md`. Only then decide whether the core compatibility approach is proven.

---

## After the proof: full museum

1. **Full Classic Tech** — industrial power/machines, quarry/pipes, EMC ecosystem, advanced automation, later nuclear/logic/tooling.
2. **The Archive network** — Velocity + central museum lobby + automatic collection/resource-pack transitions.
3. **PvE/PvP variants** — permanent co-op worlds and coherent competitive/seasonal modes where appropriate.
4. **Classic Adventure** — Hexxit-era dungeons, battle towers, bosses, loot, equipment, exploration and dimensions.
5. **Classic Warfare** — Voltz-era reactors, radiation, missiles, radar, forcefields and team warfare.
6. **Preservation/fidelity program** — historical source documentation, behavior diffs, license/asset review and attribution.
7. **Productionization** — backups, monitoring, moderation, migration, rollback, tutorials and deployment automation.
8. **Later museum wings** — classic skyblock, magic, B-Team/Yogbox-era and other historically significant experiences after feasibility/licensing review.
