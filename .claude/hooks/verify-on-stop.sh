#!/usr/bin/env bash
# Stop hook: if Java sources or the pom changed, format the code and run the fast
# architecture tests. Exit code 2 keeps the agent working and shows it the failures.
set -u

if command -v jq >/dev/null 2>&1; then
	[ "$(jq -r '.stop_hook_active // false')" = "true" ] && exit 0
fi

cd "${CLAUDE_PROJECT_DIR:-.}" || exit 0
[ -z "$(git status --porcelain -- '*.java' pom.xml)" ] && exit 0

if ! output=$(./mvnw -q -B spring-javaformat:apply 2>&1); then
	echo "spring-javaformat:apply failed:" >&2
	echo "$output" | tail -30 >&2
	exit 2
fi

if ! output=$(./mvnw -q -B test -Dtest='*ArchitectureTest' -Dsurefire.failIfNoSpecifiedTests=false 2>&1); then
	echo "Architecture tests failed. Fix the code (not the rules), then finish:" >&2
	echo "$output" | grep -E '^\[ERROR\]   [A-Za-z]+Test\.|^(Class|Method|Field|Constructor|Layer) ' | head -60 >&2
	exit 2
fi

exit 0
