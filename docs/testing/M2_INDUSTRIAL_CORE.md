# M2 Industrial Core Acceptance Test

M2 is not complete when the classes merely exist. This gate proves a real stock-client industrial loop and the conservation/persistence invariants around it.

## Preconditions

- M0 build/server gate is green.
- M1 clean stock-client gate is green.
- `./gradlew clean test build` passes under Java 25.
- Server starts with no Modpack Museum/Fabric/Polymer errors.
- Test world is disposable or backed up.

## Required playable chain

```text
Generator
   ↓
 Cable
   ↓
 BatBox
   ↓
 Cable
   ↓
 Macerator
```

The first recipe is intentionally narrow: **raw iron → 2 × Iron Dust**, followed by a normal smelting path back to iron ingots.


## Authored preflight implementation

The repository now contains an **uncompiled** Fabric-facing first pass for all four in-world nodes:

- Generator: coal fuel commits burn time into a persistent local energy buffer;
- Cable: a lightweight block entity marks conductor positions for lifecycle registration;
- BatBox: persistent terminal storage with bounded input/output;
- Macerator: commits Raw Iron into the versioned pure processing runtime and flushes exactly two Iron Dust on completion;
- `MuseumEnergyWorld`: mirrors relevant loaded block entities into `GridEnergyTopology` and runs one bounded transfer pass per level tick;
- provisional technology-preview crafting recipes and block loot tables exist.

This moves the implementation close to a runnable M2 chain, but **does not pass this gate** until Java 25 compilation, server boot, clean-client interaction, restart, and chunk lifecycle tests succeed. Recipe/balance values are provisional and must not be described as historically exact.

## Functional test

1. Obtain/place one Generator, one BatBox, one Macerator, and enough Cable for the chain above.
2. Verify each block is represented correctly on an unmodified Minecraft 26.2 client.
3. Put the supported fuel into the Generator.
4. Put one raw iron into the Macerator input.
5. Confirm the Generator begins producing energy and no client mod is required to interact with it.
6. Confirm energy reaches the BatBox through the first Cable segment.
7. Confirm the BatBox stores energy without exceeding its capacity.
8. Confirm energy reaches the Macerator through the second Cable segment.
9. Confirm the Macerator consumes energy over time and produces exactly two Iron Dust.
10. Smelt the dust through the chosen minimal survival loop and confirm the intended ore-doubling outcome.
11. Repeat with multiple inputs and verify no extra items or energy appear.

## Topology invalidation tests

1. While the system is running, break the Cable between Generator and BatBox.
2. Confirm transfer across that path stops promptly; a stale cached path must not continue to move energy.
3. Replace the Cable and confirm the network reconnects.
4. Break the Cable between BatBox and Macerator and repeat.
5. Move one machine to a different valid position and reconnect it; confirm the rebuilt topology is the new topology.

## Blocking/full-state tests

- Fill the BatBox and confirm it never exceeds capacity.
- Fill/block the Macerator output and confirm it does not consume input or energy for progress it cannot commit.
- Empty the Generator fuel and confirm it cannot create energy afterward.
- Disconnect all consumers and confirm the system does not create negative or overflowed values.

## Restart / chunk lifecycle test

Create a recognizable partial state, for example:

- Generator has fuel/burn state remaining.
- BatBox contains a non-round amount of energy.
- Macerator has input and partial recipe progress.
- Macerator output contains a known stack.

Then:

1. record the state;
2. stop the server cleanly;
3. restart and rejoin;
4. confirm committed inventory/energy/progress state resumes without reset or replay;
5. unload/reload the relevant chunks and repeat the comparison.

No input may duplicate, disappear, or be processed twice because of reload.

## Conservation evidence

During at least one controlled run, record:

- Generator energy created from the accepted fuel model;
- energy remaining in Generator;
- energy stored in BatBox;
- energy remaining/consumed by Macerator;
- item inputs and outputs.

The exact balance constants can change later. The invariant cannot: transfers do not create energy, stores never exceed capacity, and each completed recipe consumes exactly its declared input and produces exactly its declared output.

## Development stress sanity check

Before closing M2, build at least one test with approximately 100 energy endpoints and 500 Cable blocks. This is not the final M5 stress gate; it is a sanity check that unchanged topology is cached rather than globally rediscovered every tick.

## Pass criterion

Gate M2 passes only when the stock-client chain **Generator → Cable → BatBox → Cable → Macerator** processes the minimal ore loop and remains correct across cable mutation, server restart, and chunk unload/reload.

After passing, update `PROGRESS.md` with exact commit, environment, logs/test notes, and any known limitations.
