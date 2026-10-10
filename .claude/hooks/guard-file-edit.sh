#!/usr/bin/env bash
# PreToolUse hook for file edits. Exit code 2 blocks the edit and shows stderr to the agent.
set -u

if ! command -v jq >/dev/null 2>&1; then
	echo "guard-file-edit: jq not found, guardrail skipped" >&2
	exit 1
fi

file_path=$(jq -r '.tool_input.file_path // .tool_input.notebook_path // empty')
[ -z "$file_path" ] && exit 0
file_path=${file_path//\\//}
cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0

case "$file_path" in
	*/src/main/resources/db/migration/*.sql)
		if git ls-files --error-unmatch -- "$file_path" >/dev/null 2>&1; then
			echo "Blocked: $file_path is a committed Flyway migration. Never edit committed migrations; add a new V<n>__<description>.sql instead." >&2
			exit 2
		fi
		name=$(basename "$file_path")
		if ! [[ "$name" =~ ^V[0-9]+__[a-z0-9_]+\.sql$ ]]; then
			echo "Blocked: migration '$name' must be named V<n>__<snake_case_description>.sql (e.g. V1__create_golfer.sql)." >&2
			exit 2
		fi
		;;
esac

exit 0
