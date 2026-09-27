# Core: bounded energy network graph

## Goal
Create a reusable energy network for generators, storage, machines and conductors.

## Requirements
- topology changes invalidate/rebuild networks.
- bounded per-tick work.
- transfer/storage limits.
- extension point for tier/voltage semantics.
- persistence belongs to nodes, not transient graph caches.
- benchmark/test large but plausible networks.
