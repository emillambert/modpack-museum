# Contributing

Modpack Museum accepts code, testing, historical research, documentation, and compatibility contributions.

## Principles

1. **Vanilla clients first.** A required client mod is a regression unless the feature is explicitly optional.
2. **Behavior over binary compatibility.** Reimplement documented gameplay; do not attempt to ship ancient mod binaries inside the project.
3. **No copied assets without permission.** Old does not mean public domain.
4. **Evidence historical behavior.** For fidelity changes, link a wiki page, source file, archived documentation, video, or reproducible old-pack observation when possible.
5. **Keep the core generic.** Shared machine/energy/network logic belongs in MuseumCore, not a one-off collection implementation.
6. **Preserve Minecraft.** Collections should feel like their historical inspiration without turning the lobby/network into a monetized public-server UI.

## Pull requests

A good PR contains:

- a focused change
- tests where practical
- no unlicensed third-party assets
- migration notes for persistent data changes
- performance consideration for tick-time systems
- documentation updates when behavior changes

## Historical references

When submitting behavior derived from an old mod, include a note such as:

```text
Historical target: IC2 Macerator (Tekkit Classic era)
Reference: <URL / archive / source path>
Observed behavior: ...
Implementation differences: ...
```

## Clean-room rule

For closed or unclear-license projects, contributors should implement behavior from public documentation/observation using new code and project-owned assets. Do not submit decompiled proprietary code.
