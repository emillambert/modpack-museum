# Modpack Museum

**Classic modpacks. Modern Minecraft. No launcher.**

Modpack Museum is an open-source project to preserve and recreate the gameplay of Minecraft's classic modpack era on modern Minecraft servers.

Players connect using an **unmodified current Minecraft Java client**. No Forge, Fabric, Technic Launcher, old Java installation, or client-side modpack is required.

From a central lobby — **The Archive** — players will enter modern server-side recreations inspired by classic technology, adventure, warfare, skyblock, and magic packs.

> If you remember playing an old Minecraft modpack ten or fifteen years ago, you should be able to open modern Minecraft, join one server, and experience that style of play again.

## Initial collections

| Collection | Historical target | Status |
|---|---|---|
| **Classic Tech** | Tekkit Classic-era technology | First implementation target |
| **Classic Adventure** | Hexxit-era exploration | Planned |
| **Classic Warfare** | Voltz-era warfare/energy | Planned |

These are preservation-inspired clean implementations, not bundles of the original mod binaries. Names, code, art, sounds, and assets from historical projects are only used where licensing or explicit permission permits it.

## Why this exists

Old packs remain playable, but usually require old launchers, old Minecraft versions, old Java versions, dead download links, and matching client installs. Modern recreations generally remain client-modded. Modpack Museum aims for a different experience:

```text
Current Minecraft Java
        ↓
play.modpackmuseum.net
        ↓
The Archive
        ↓
Classic Tech / Adventure / Warfare
        ↓
server-side implementation + automatic resource pack
```

The player should only need Minecraft itself.

## Technical direction

The first implementation targets **Minecraft 26.2**, **Java 25**, **Fabric**, and **Polymer**. Polymer allows custom server-side items, blocks, entities, and resource-pack-backed presentation while vanilla clients connect normally.

The network architecture is designed around:

- Fabric backend servers
- Polymer for vanilla-client translation
- Velocity as the eventual network proxy
- one shared `MuseumCore` framework
- collection-specific modules built on top of the common framework
- automatic server resource packs
- declarative recipes and machine definitions wherever possible

See [`docs/architecture.md`](docs/architecture.md).

## First proof: the Tekkit moment

We are deliberately not starting by recreating hundreds of mods. The first milestone is a small vertical slice that proves the concept:

- copper and tin
- generator
- cable
- BatBox-style storage
- macerator and ore doubling
- electric furnace
- BuildCraft-style quarry
- item pipes
- EMC
- transmutation table
- philosopher's-stone-style utility item

If a player can join with stock Minecraft and this feels recognizably like classic Tekkit, the core thesis is proven.

## Repository status

The repository now contains the first **authored but not yet runtime-verified** compatibility slice: Polymer-backed custom items, a textured custom test-machine block, a server-driven vanilla GUI, automatic resource-pack setup, and pure energy/EMC conservation primitives.

The source of truth for actual completion and test evidence is [`PROGRESS.md`](PROGRESS.md). The canonical immediate and long-term plan is [`docs/PROJECT_PLAN.md`](docs/PROJECT_PLAN.md). Do not treat authored code as proof that a stock client works until the M1 acceptance test has been run.

## Development

Requirements:

- Java 25
- Docker + Docker Compose for the local server

Check the host first:

```bash
./scripts/check-environment.sh
./scripts/validate-static.sh
```

Build:

```bash
./gradlew build
```

Run a local Fabric development server:

```bash
./scripts/dev-up.sh
```

Stop it:

```bash
./scripts/dev-down.sh
```

The local runtime is stored under `server/runtime/` and is ignored by Git except for placeholder files. Polymer autohost is preconfigured under `server/config/polymer/`. The manual M1 stock-client gate is documented in [`docs/testing/M1_VANILLA_CLIENT.md`](docs/testing/M1_VANILLA_CLIENT.md).

## Publishing this repository

If this directory is not already a GitHub repository and the GitHub CLI is authenticated:

```bash
./scripts/publish-github.sh
```

That script creates `emillambert/modpack-museum`, pushes the repository, and optionally opens the prepared backlog issues.

## Contributing

Historical knowledge is useful. Contributions can include implementation work, documentation of old behavior, recipe verification, screenshots/video references, testing, and licensing research.

See [`CONTRIBUTING.md`](CONTRIBUTING.md) and [`docs/legal-and-attribution.md`](docs/legal-and-attribution.md).

## License

Original Modpack Museum source code in this repository is licensed under the Apache License 2.0 unless a subdirectory explicitly states otherwise. Third-party names, trademarks, code, assets, and documentation remain property of their respective owners and are not relicensed by this project.
