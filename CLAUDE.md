# CLAUDE.md

Guidance for Claude Code in this repository. The full agent guide is imported below — keep project facts there, not here, so they are loaded once and don't drift.

@AGENTS.md

## Token hygiene

- Run Gradle quietly: `./gradlew -q --console=plain <task>`. On failure, rerun only the failing task (add `--stacktrace` if needed) and trim output with `| tail -n 80` or `| grep -E "e: |error|FAILED"`.
- Use the narrowest task that answers the question, e.g. `./gradlew -q --console=plain :core:database:jvmTest --tests "*MonDatabaseTest*"`, instead of full builds.
- Never read `system_core.dic`, `build/` outputs, or `*.generated.kt` files; regenerate the icon catalog with `generateIconCatalog` instead of editing it.
- Delegate broad multi-file searches to the `codebase-explorer` subagent; use `code-reviewer` after non-trivial changes.
