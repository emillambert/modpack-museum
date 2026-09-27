# Architecture

## Network target

```text
                     stock Minecraft client
                              |
                              v
                   play.modpackmuseum.net
                              |
                           Velocity
                              |
              +---------------+---------------+
              |               |               |
          The Archive     Classic Tech    future collections
           (lobby)          /      \
                           PvE     PvP
```

The initial repository develops a single Fabric backend first. Velocity is introduced only after the core gameplay is proven.

## Backend

- Minecraft 26.2
- Java 25
- Fabric Loader
- Fabric API
- Polymer
- automatic Polymer resource-pack hosting/delivery

Polymer is the presentation bridge between server-defined content and unmodified clients. Server state must remain authoritative.

## Module boundaries

For the first milestone the code is intentionally one Fabric module to reduce build-system complexity. Package boundaries should already follow the intended eventual modules:

```text
net.modpackmuseum
├── core
│   ├── energy
│   ├── machine
│   ├── network
│   ├── persistence
│   └── resourcepack
├── classictech
│   ├── industrial
│   ├── quarry
│   ├── transport
│   └── emc
├── archive
└── api
```

When independent release/versioning becomes useful, split these packages into Gradle subprojects without changing their public API unnecessarily.

## Machine framework

Target model:

```text
MachineDefinition
  id
  inventory schema
  energy behavior
  recipe family
  progress rules
  upgrades
  presentation

MachineRuntime
  persisted state
  inventories
  energy buffer
  current recipe
  progress
  lifecycle
```

The historical machine should mostly be a definition over reusable runtime behavior.

## Energy

The shared energy engine should model:

- producers
- consumers
- storage
- conductors
- transfer limits
- optional voltage/tier semantics
- graph rebuild on topology changes
- bounded per-tick work

Collection-specific semantics (EU-like tiers, Voltz-style rules, etc.) sit above the shared graph rather than forking it.

## Item transport

Pipes/tubes are graph-based routing systems. Visual moving items are presentation; the authoritative transfer is server state. Route computation should be cached and invalidated on topology changes rather than recomputed across the full graph every tick.

## Persistence

Every persisted object format must include a schema version. Public servers will outlive individual Minecraft versions. Migration code is a first-class feature.

## Resource pack

The project owns its visual identity. Do not copy historical textures merely for nostalgia unless the license/author explicitly permits redistribution.

The resource pack should support collection-specific visual themes but be generated/merged deterministically to prevent collisions.
