# Codex bootstrap prompt

Work directly in this repository and treat `AGENTS.md`, `README.md`, `ROADMAP.md`, and `docs/architecture.md` as authoritative project context.

Your job is to move Modpack Museum from scaffold to a tested v0.1 implementation, not to write another planning document.

## Execute now

1. Inspect the host and repository.
2. Ensure Java 25, Docker and GitHub CLI are available; install/configure only what is safe and appropriate on this host.
3. Run the build and repair any dependency/API/version problems against the current stable Minecraft 26.2 Fabric/Polymer ecosystem.
4. Implement the v0.1 milestone in `ROADMAP.md`:
   - Polymer-backed custom Archive Token item
   - project-owned resource-pack model/texture
   - custom test machine block
   - server-driven machine GUI
   - automatic Polymer resource-pack delivery to an unmodified client
   - persistence/restart tests
5. Start the Docker dev server, inspect logs, and fix all startup errors.
6. Verify the server using automated checks wherever possible. Do not claim a vanilla-client visual test was performed unless a real client/browser-capable test environment is available; document the exact remaining smoke test if not.
7. Keep the repo clean: generated worlds, secrets, logs and large runtime data must remain ignored.
8. Commit the v0.1 implementation in logical commits.
9. If GitHub CLI is authenticated and the remote repository does not yet exist, run `./scripts/publish-github.sh` to create and push `emillambert/modpack-museum`.
10. Run `./scripts/create-github-issues.sh` after publishing so the prepared engineering backlog exists as GitHub issues.
11. Update README/ROADMAP to reflect only what is actually working.

## Engineering constraints

- Stock Minecraft Java clients remain the primary target.
- Do not introduce required client mods.
- Do not reuse historical proprietary assets or decompiled code.
- Keep Polymer/server presentation separate from authoritative gameplay state.
- Persisted data must be versioned/migratable.
- Test restart/chunk unload behavior for machine state.
- Avoid per-tick whole-network scans in designs that will later become energy/pipes.

When v0.1 works, stop and produce a concise engineering report: build status, runtime status, exact dependencies/versions, vanilla-client test status, resource-pack status, known issues, and the next highest-priority issue. Do not start Hexxit/Voltz work.
