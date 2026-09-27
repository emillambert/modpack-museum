# v0.1: Custom test machine block and server-driven GUI

## Goal
Implement a minimal custom machine block visible to stock clients and prove that server-side interaction can open a vanilla-compatible GUI.

## Acceptance criteria
- custom block registration via Polymer textured-block path.
- project-owned model/texture.
- safe placement/breaking with correct block-item drop.
- server GUI opens without client mod.
- automatic resource pack renders the intended placed-block model.
- reconnect/server restart preserves the placed custom block identity.

## Scope note
Persistent inventory was originally deferred to M2, but M2 groundwork has now replaced the transient demo container with a versioned persistent block entity. M1 still exists primarily to prove stock-client presentation/interaction; persistence must now also be checked because the implementation is present and will become the basis for real machines.
