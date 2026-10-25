#!/bin/bash
f=$(jq -r '.tool_input.file_path // empty')
[[ "$f" == *.kt && "$f" != *.generated.kt ]]  || exit 0
cd "$CLAUDE_PROJECT_DIR" && ./gradlew -q --console=plain --no-configuration-cache spotlessApply -PspotlessIdeHook="$f" >/dev/null 2>&1
exit 0