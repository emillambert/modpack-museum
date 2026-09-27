# Core: machine definition/runtime framework

## Goal
Separate reusable machine mechanics from individual Classic Tech machines.

Create concepts equivalent to `MachineDefinition`, `MachineRuntime`, inventory schema, progress, recipe family, energy buffer and persistence schema version.

## Acceptance criteria
- no historical machine names embedded in generic core types.
- deterministic tick behavior.
- explicit persistence schema version.
- unit/game tests for state transition and restart serialization.
