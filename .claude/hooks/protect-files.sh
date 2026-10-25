#!/bin/bash
f=$(jq -r '.tool_input.file_path // empty')
case "$f" in
  *.generated.kt|*/core/database/schemas/*|*system_core.dic|*/local.properties)
  echo "Blocked: $f is generated or protected. Use the Gradle task (e.g. generateIconCatalog) or ask the user." >&2
  exit 2;;
esac
exit 0