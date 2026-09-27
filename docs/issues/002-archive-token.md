# v0.1: Polymer Archive Token + automatic resource pack

## Goal
Implement the first genuinely custom server-defined item and prove automatic resource-pack delivery to an unmodified client.

## Acceptance criteria
- register `modpack_museum:archive_token`.
- implement it with Polymer rather than a disguised Bukkit-style item.
- project-owned model/texture.
- Polymer builds the pack reproducibly.
- joining vanilla client is offered/required the pack automatically.
- item survives save/restart and creative/inventory round-trips without conversion.
