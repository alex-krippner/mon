# AGENTS.md

Guidance for agents working in this repository. It summarizes what is not obvious
from reading the code alone. Prefer following the executable build configuration
over prose documentation; where `README.md` and the build scripts disagree,
trust the scripts.

## Build and commands

The project uses the Gradle wrapper (`./gradlew`, or `gradlew.bat` on Windows).
Kotlin Multiplatform with AGP 9.1.1 / Kotlin 2.3.21 / Compose Multiplatform 1.11.1.
All targets compile with JVM target 17.

- Build and run the Android app: `./gradlew :androidApp:assembleDebug`
- Run the desktop app: `./gradlew :desktopApp:run`
- Run the iOS app: open `iosApp/` in Xcode (an Xcode project, not built by Gradle).
- Check formatting: `./gradlew spotlessCheck`; fix it: `./gradlew spotlessApply`
- Run all JVM tests: `./gradlew jvmTest :tools:pack-converter:test`. The `jvmTest` selector
  runs every module that has one, so a new module needs no change here. The pack converter is a
  plain JVM module, so its task is `test`.

Notes:

- `spotless` (ktfmt, google style) is the only code-quality check configured.
  There is no `detekt` task despite what `README.md` suggests — do not run it.
- Each library module has `android` and `jvm` targets, so tasks are named after the target, for
  example `:core:database:jvmTest`.
- Gradle uses the configuration cache and build caching, so the first build after changing
  configuration re-runs configuration.
- `local.properties` points at the local Android SDK and must not be committed (it is gitignored).

## Project layout

A reading/learning app ("Mon"): readings with furigana and dictionary lookups, using a Japanese
tokenizer. Diagrams (module graph, classes, runtime flows) are in `docs/architecture.md`; the pack
and dictionary system is in `docs/dictionary-architecture.md`. Read them when a task spans modules.

- `:shared` — the single `App()` composable, `MonNavHost`, the Koin bootstrap (`initKoin`),
  Compose resources.
- `:core:model` — data models.
- `:core:domain` — use cases (`AddReadingUseCase`, `TokenizePendingReadingsUseCase`,
  `LookupWordUseCase`) and `PendingTokenization`.
- `:core:data` — repositories; only maps between Room entities and `:core:model`.
- `:core:database` — Room database and DAOs, with KSP codegen.
- `:core:tokenizer` — `Tokenizer`, `TokenizerProvider`, and the Sudachi pack.
- `:core:dictionary` — `MonDictionary`, `DictionaryProvider`, the JMdict reader and file format.
- `:core:packs` — the generic pack system (catalog, download, install, remove). Knows no file format.
- `:core:ui` — reusable Compose UI that knows about content.
- `:core:designsystem` — theme, components, icon catalog. No domain models.
- `:core:navigation` — Navigation 3 keys (`MonNavKey`) and `MonNavConfiguration`.
- `:core:arch` — `BaseViewModel<State, Intent, Effect>`.
- `:feature:library`, `:feature:addreading`, `:feature:dictionaries` — one screen each.
- `:tools:pack-converter` — command-line tool (not in the app) that turns dictionary source files
  into packs and writes the catalog.
- `:androidApp`, `:desktopApp`, `:iosApp` — platform entrypoints; all embed `:shared`.

Dependency direction: `:shared` depends on all `core:*` and `feature:*` modules, which depend on
lower layers. Platform-specific code lives in the `*Main` source sets.

Layering rules:

- A feature may read through a repository directly for a simple query (for example
  `ReadingRepository.observeReadingSummaries()`). Writes, and logic that combines repositories or
  the tokenizer, go through a use case in `:core:domain`.
- Screens and view models never touch `:core:database` or `:core:tokenizer` implementations.
- Lists use light summary models (`ReadingSummary`); the full `Reading` with tokens is loaded only
  for one reading (`getReading`).
- Code above `:core:tokenizer` and `:core:dictionary` asks a provider and never assumes where a
  tokenizer or dictionary comes from. A provider that can't answer throws
  `TokenizerUnavailableException` / `DictionaryUnavailableException`; that means "can't answer right
  now", not "no tokens" or "not found".

Tokenization:

- `AddReadingUseCase` stores a reading without tokens (*pending*) when no tokenizer is available,
  it can't answer, or it fails, and calls `PendingTokenization.request()`.
- `PendingTokenization` runs `TokenizePendingReadingsUseCase` at start (from `initKoin`) and on
  every `TokenizerProvider.changes`, one run at a time.
- A reading stores the id of the tokenizer that produced its tokens (`tokenizedWith`); null means
  pending. `saveAnalysis` only writes to a pending reading, so tokenizing twice is harmless.
- Only a Japanese Sudachi pack exists today, so readings in other languages stay pending.

Dependency injection is Koin. Every module registers its Koin module in `:shared` (`Koin.kt`,
`initKoin`); new modules must be added there.

## Presentation and navigation

- A feature has a `*Contract.kt` (state, sealed intents, effects), a view model extending
  `BaseViewModel<State, Intent, Effect>`, and a stateless screen `Screen(state, onIntent)`.
- Intents are UI input through `onIntent`; state is what is rendered; effects are one-off events
  (navigation, snackbars) sent with `sendEffect`. Never signal effects through state. Use
  `NoEffect` when a screen has none.
- View models expose typed errors (an enum or sealed type such as `LibraryError`), never exception
  messages. The screen maps them to string resources.
- Features do not depend on `:core:navigation`. They emit their own effects and `MonNavHost` in
  `:shared` maps them to nav keys. To add a destination: add the key to `MonNavKey`, register it in
  `MonNavConfiguration` (the only place; forgetting it crashes on back-stack restore), and add an
  `entry<…>` to `MonNavHost`. `Reader` is still a stub entry.
- The bottom bar is rendered by `MonNavHost`, only while a tab's screen is on top. Its tabs are the
  `tabs` list there. The visual spec is `docs/design/mon-handoff-spec.md`.
- Strings live in each feature's `src/commonMain/composeResources/values/strings.xml`; the
  `mon.feature` plugin gives each feature its own resource package. Do not hardcode UI strings.

## Quirks and gotchas

- Module builds use the convention plugins in `build-logic`: `mon.kmp.library`, `mon.kmp.compose`
  and `mon.feature`. A module's `build.gradle.kts` declares only its own dependencies, using the
  `projects.*` accessors. `mon.feature` fails configuration when a feature depends directly on
  `:core:database` or on another feature; reach persistence through `:core:data` and wire features
  together in `:shared`.
- The icon catalog is generated code. Run `generateIconCatalog` in `:core:designsystem` manually
  after changing icon vector XMLs and commit the regenerated `*.generated.kt` files. The task is
  not wired into the build.
- Room schema exports live in `core/database/schemas/` and are regenerated by KSP. The app is not
  released yet, so a schema change edits version 1 in place: keep `version = 1`, let KSP
  regenerate `1.json`, and delete your local database (desktop `~/.mon`, or clear Android app
  data). No migrations are written before the first release. Once the app ships, bump the
  version for every schema change and add a `Migration` in `Migrations.kt` (or an
  `AutoMigration`) with a test in `MonDatabaseMigrationTest`.
- Tests: `commonTest` in most modules, plus `jvmTest` for the migration test (it reads the exported
  schemas) and the dictionary and tokenizer modules. Database tests use an in-memory database with
  the bundled SQLite driver. Domain and view model tests use hand-written fakes (`ReadingRepository`,
  `TokenizerProvider`, `DictionaryProvider`) and `Dispatchers.setMain`. The pack tests build tiny
  real packs and use a hand-written `FakePackDownloader` plus Ktor's `MockEngine`. The converter
  tests use a fake `PackSource`. No emulator or outside server is needed.
- `system_core.dic` (the Sudachi dictionary) is not in the repository; it is the downloadable pack
  `sudachi-core`. An old copy is in git history (`272df57`, git-LFS). Do not read it. `*.dic` stays
  in `.gitattributes` as an LFS pattern.
- The iOS targets in `:shared/build.gradle.kts` are commented out. The iOS app builds only via the
  Xcode project in `iosApp/`.
- `compileSdk` is 37 because Ktor's OkHttp engine pulls in OkHttp 5.5, which requires it.
  `targetSdk` stays 36. AGP downloads the missing platform on first build.

## Dictionary packs

The app is tied to no dictionary or language. Dictionaries and the Sudachi tokenizer dictionary are
downloadable packs, not bundled. The app works without any pack: a Japanese reading is stored
without tokens until `sudachi-core` is installed. Do not read `*.db` or `jmdict-*.json` files.
Publishing packs is a release task: see `docs/publishing.md`.

- **Readers and capabilities.** Every installed pack opens into a `PackReader` (only
  `pack: CatalogPack`). What a reader can answer is a capability interface it implements next to it:
  `MonDictionary` and `Tokenizer` today. Capabilities do not extend `PackReader`.
  `PackManager.readers()` returns the readers of all kinds. The manager, installer and Dictionaries
  screen do not care what kind a pack is. `PackFormat.open` must stay cheap (no file access),
  because `readers()` opens every installed pack; a reader opens its file on the first real call.
- **Closed readers.** A reader must be closed before its file is replaced or deleted; the manager
  does that under its `files` lock. A closed reader throws `PackClosedException`, and the provider
  (`PackDictionaryProvider`, `PackTokenizerProvider`) fetches the readers again and repeats the
  call, or throws the module's `*UnavailableException` if the pack is gone.
- **Where it lives.** A pack is `<base>/<packId>/pack.dat` plus `pack.json` (the installed
  `CatalogPack`). `pack.json` is written last, and an update deletes the old one before it replaces
  the database, so a half-finished install never counts as installed. `<base>` is
  `noBackupFilesDir/dictionaries` on Android and `~/.mon/dictionaries` on desktop. The folder name
  is historical and must not change. On its first scan the manager calls `PackInstaller.sweep()`.
- **Catalog.** The app hard-codes one URL, `CATALOG_URL` in `PackManagers.kt`: the `catalog.json` of
  the latest release of `alex-krippner/mon-data`. The app offers only packs whose `format`,
  `schema` and languages it supports, each id once. `refreshCatalog()` never throws; it returns a
  `CatalogError` (or null).
- **Catalog compatibility.** Never remove or rename a `CatalogPack` field, and every pack carries
  every field with a valid value. A new field must have a default value, or installed `pack.json`
  files stop decoding. A change that old readers can't read needs a new `schema`.
- **Installing.** `PackInstaller` does the file work: https only, ids must match `[a-z0-9-]+`,
  free-space check, sha256, gunzip, `PackFormat.verify`, then commit. Ktor is used only in
  `KtorPackDownloader`, behind `PackDownloader`. `sha256Hex` is shared by the app and the converter.
- **The word-list format.** `jmdict-sqlite` is Mon's format for any word list in any language pair,
  not only JMdict: every source that maps to `JmdictRecord` uses it. The wire id and the Kotlin
  names keep "jmdict"; renaming the id would hide installed packs.
  - `lookup_key.key` is matched exactly, so the file says how its keys are normalized, in the meta
    row `keyNormalization` (`KeyNormalization` in `LookupKeys.kt`). `JmdictWriter(path,
    sourceLanguage)` picks the rule from the language and writes the row. `JmdictDictionary` reads
    the row and applies the same rule to each query; it never derives it from the pack's language.
  - No row means `NONE`: Japanese, and every pack published so far. Other languages get `nfc-lower`
    (Unicode NFC, then lower case). That is not case folding: "STRASSE" does not find "Straße".
  - **Never change what an existing `KeyNormalization` value does;** a new rule is a new value.
    `verify` rejects a file with a value the app does not know. Moving a language that already has
    published packs to a new value also needs a new `schema`.
  - Normalization is not lemmatization: "Häuser" → "Haus" is the tokenizer's job.
- **Adding a source or a format.**
  - *A new word source, any language pair* (e.g. JMnedict, Wadoku): a package in
    `:tools:pack-converter` with a parser, a mapper to `JmdictRecord` and a `PackSource` that passes
    its source language to `JmdictWriter`; add it to `SOURCES` in `Main.kt`; add any new language to
    `Language` (`:core:model`). If the language needs its own key rule, add a `KeyNormalization`
    value and return it from `forLanguage`, before the language's first pack is published.
  - *A new storage format for words:* in `:core:dictionary/<format>/`, a schema with
    `FORMAT`/`VERSION`, a writer and a reader that is `MonDictionary, CloseableReader`; a
    `PackFormat`, registered in its module's Koin module with `bind PackFormat::class`; then the
    converter source. The manager does not change.
  - *A new kind of data* (kanji, example sentences, pitch accent): as a new storage format, but the
    reader implements a new capability interface in `commonMain` instead of `MonDictionary`. Add its
    models to `:core:model`, a provider that retries on `PackClosedException`, a use case in
    `:core:domain`, and the UI.
  - *Another JMdict gloss language:* add it to `GlossLanguage` (converter) and to `Language`
    (`:core:model`), or the app hides the pack. `JmdictSourceTest` fails if the second is missing.

## Library and API documentation

- When you need to search docs, use Context7.
