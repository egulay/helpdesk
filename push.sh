#!/usr/bin/env bash
set -euo pipefail

printf '%s\n' \
  '══════════════════════════════════════════════' \
  '📤 Helpdesk Git Push' \
  '══════════════════════════════════════════════'

if [[ $# -lt 1 || -z "${1// /}" ]]; then
  printf '%s\n' \
    '❌ A commit message is required.' \
    'Usage: ./push.sh "commit message"' >&2
  exit 1
fi

COMMIT_MESSAGE="$*"

command -v git >/dev/null 2>&1 || {
  printf '❌ Git is required but was not found.\n' >&2
  exit 1
}

git rev-parse --is-inside-work-tree >/dev/null 2>&1 || {
  printf '❌ Not inside a Git repository.\n' >&2
  exit 1
}

cd "$(git rev-parse --show-toplevel)"

BRANCH=$(git branch --show-current)
if [[ -z "${BRANCH}" ]]; then
  printf '❌ HEAD is detached; check out a branch before pushing.\n' >&2
  exit 1
fi

REMOTE=${REMOTE:-origin}
git remote get-url "${REMOTE}" >/dev/null 2>&1 || {
  printf '❌ Remote %s is not configured.\n' "${REMOTE}" >&2
  exit 1
}

# Stage every tracked change plus new files that .gitignore does not exclude.
printf '📦 Staging changes...\n'
git add -A

if git diff --cached --quiet; then
  printf 'ℹ️ Nothing to commit; the working tree is clean.\n'
else
  git -c color.status=always status --short
  printf '📝 Committing: %s\n' "${COMMIT_MESSAGE}"
  git commit -m "${COMMIT_MESSAGE}"
fi

printf '🚀 Pushing %s to %s...\n' "${BRANCH}" "${REMOTE}"
if git rev-parse --abbrev-ref --symbolic-full-name '@{upstream}' >/dev/null 2>&1; then
  git push "${REMOTE}" "${BRANCH}"
else
  printf 'ℹ️ No upstream is configured; setting %s/%s as upstream.\n' "${REMOTE}" "${BRANCH}"
  git push --set-upstream "${REMOTE}" "${BRANCH}"
fi

printf '%s\n' \
  '══════════════════════════════════════════════' \
  '✅ Push completed successfully.' \
  '══════════════════════════════════════════════'
