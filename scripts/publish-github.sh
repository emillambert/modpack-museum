#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."

REPO="${REPO:-emillambert/modpack-museum}"
VISIBILITY="${VISIBILITY:-public}"

command -v git >/dev/null || { echo "git is required" >&2; exit 1; }
command -v gh >/dev/null || { echo "GitHub CLI (gh) is required" >&2; exit 1; }
gh auth status >/dev/null

if [[ ! -d .git ]]; then
  git init -b main
fi

git add .
if ! git diff --cached --quiet; then
  git commit -m "Initialize Modpack Museum"
fi

if gh repo view "$REPO" >/dev/null 2>&1; then
  echo "$REPO already exists"
else
  gh repo create "$REPO" --"$VISIBILITY" \
    --description "Classic modpacks. Modern Minecraft. No launcher." \
    --source . --remote origin --push
fi

if ! git remote get-url origin >/dev/null 2>&1; then
  git remote add origin "https://github.com/${REPO}.git"
fi

git push -u origin main

gh repo edit "$REPO" --description "Classic modpacks. Modern Minecraft. No launcher." \
  --add-topic minecraft --add-topic fabric --add-topic polymer --add-topic preservation --add-topic server-side

echo
echo "Published: https://github.com/$REPO"
echo "To create the prepared issues, run: ./scripts/create-github-issues.sh"
