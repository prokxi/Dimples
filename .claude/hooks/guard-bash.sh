#!/usr/bin/env bash
# PreToolUse hook for Bash. Exit code 2 blocks the command and shows stderr to the agent.
set -u

if ! command -v jq >/dev/null 2>&1; then
	echo "guard-bash: jq not found, guardrail skipped" >&2
	exit 1
fi

command=$(jq -r '.tool_input.command // empty')
[ -z "$command" ] && exit 0

block() {
	echo "Blocked: $1 (see AGENTS.md). Ask the user if this is really needed." >&2
	exit 2
}

# Only look at the git invocation itself, up to the next ; & | or newline.
while IFS= read -r push; do
	[[ "$push" =~ [[:space:]](-f|--force|--force-with-lease|--force-if-includes)([[:space:]=]|$) ]] && block "force push"
	[[ "$push" =~ [[:space:]:+](main|refs/heads/main)([[:space:]]|$) ]] && block "push to main"
	[[ "$push" =~ --no-verify ]] && block "skipping git hooks"
done < <(grep -oE 'git[[:space:]]+push[^;&|]*' <<<"$command")

while IFS= read -r commit; do
	branch=$(git -C "${CLAUDE_PROJECT_DIR:-.}" branch --show-current 2>/dev/null)
	[ "$branch" = "main" ] && block "committing on main; work on a feature branch off develop"
	[[ "$commit" =~ --no-verify ]] && block "skipping git hooks"
done < <(grep -oE 'git[[:space:]]+commit[^;&|]*' <<<"$command")

while IFS= read -r hooks_path; do
	[[ "$hooks_path" =~ --get ]] && continue
	[[ "$hooks_path" =~ core\.hooksPath[[:space:]=]+\.githooks([[:space:]]|$) ]] && continue
	block "changing core.hooksPath (the commit-msg hook in .githooks runs gitlint)"
done < <(grep -oE 'git[[:space:]]+[^;&|]*core\.hooksPath[^;&|]*' <<<"$command")

[[ "$command" =~ -D(enforcer\.skip|spring-javaformat\.skip|archunit|maven\.test\.failure\.ignore|surefire\.failIfNoSpecifiedTests) ]] \
	&& block "skipping build guardrails"

exit 0
