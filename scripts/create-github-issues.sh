#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
REPO="${REPO:-emillambert/modpack-museum}"

command -v gh >/dev/null || { echo "GitHub CLI (gh) is required" >&2; exit 1; }
gh auth status >/dev/null

for f in docs/issues/*.md; do
  title=$(sed -n '1s/^# //p' "$f")
  if [[ -z "$title" ]]; then
    echo "Skipping $f: first line must be '# title'" >&2
    continue
  fi
  body=$(tail -n +2 "$f")
  if gh issue list --repo "$REPO" --state all --search "\"$title\" in:title" --json title --jq '.[].title' | grep -Fxq "$title"; then
    echo "Exists: $title"
  else
    gh issue create --repo "$REPO" --title "$title" --body "$body"
  fi
done
