# AGENTS.md

Guidance for agents working in this repository. It summarizes what is not obvious
from reading the code alone. Prefer following the executable build configuration
over prose documentation; where `README.md` and the build scripts disagree,
trust the scripts.

## Build and commands

The project uses the Gradle wrapper (`./gradlew`, or `gradlew.bat` on Windows).
Kotlin Multiplatform with AGP 9.1.1 / Kotlin 2.3.21 / Compose Multiplatform 1.11.1.
All targets compile with JVM target 17.

- Build and run the Android app:
  `./gradlew :androidApp:assembleDebug`
- Run the desktop app:
  `./gradlew :desktopApp:run`
- Run the iOS app: open the `iosApp/` directory in Xcode (it is an Xcode project,
  not built by the Gradle build).
- Check formatting without changing files:
  `./gradlew spotlessCheck`
- Fix formatting:
  `./gradlew spotlessApply`
- Run the JVM tests:
  `./gradlew :core:database:jvmTest :core:data:jvmTest :core:domain:jvmTest :core:model:jvmTest :core:dictionary:jvmTest :feature:addreading:jvmTest :feature:dictionaries:jvmTest :feature:library:jvmTest`
- Test the pack converter (a plain JVM module, so the task is `test`):
  `./gradlew :tools:pack-converter:test`

Notes:

- `spotless` (ktfmt, google style) is the only code-quality check configured.
  There is no `detekt` task despite what `README.md` suggests — do not run it.
- Each library module has both `android` and `jvm` targets, so a module task is
  usually named after the target and the task, for example
  `:core:database:jvmTest`.
- Gradle is configured with the configuration cache and build caching, so the
  first build after changing configuration re-runs configuration.
- `local.properties` points at the local Android SDK and must not be committed
  (it is gitignored).

## Project layout

The app is a reading/learning app ("Mon") that shows readings with furigana and
dictionary lookups, using a Japanese tokenizer.

- `:shared` — the shared/common code used by all platforms. Contains the app's
  single `App()` composable, the navigation host (`MonNavHost`), the Koin
  bootstrap (`initKoin`), Compose resources, and the Sudachi system dictionary.
- `:core:model` — data models (`Reading`, `ReadingSummary`, `MonToken`, ...).
- `:core:domain` — business logic: use cases such as `AddReadingUseCase`, which
  validates input, tokenizes it with the `Tokenizer` that `TokenizerProvider` offers for the
  reading's language (none, or it can't answer: it stores the reading without tokens, as
  *pending*) and stores it through a repository, `TokenizePendingReadingsUseCase`, which gives the
  pending readings their tokens as far as a tokenizer is available, `PendingTokenization`, which
  runs that once at start (from `initKoin`) and again on every `TokenizerProvider.changes`, one run
  at a time, and `LookupWordUseCase`, which looks a `MonToken` up in every dictionary
  `DictionaryProvider` offers for a language pair and returns `LookupResult` (`Found`, `NotFound` or `NoDictionaryInstalled`, which the UI
  turns into a "download a dictionary" prompt).
- `:core:tokenizer` — the `Tokenizer` capability (`language`, `id`, suspending `tokenize`, which
  returns `MonToken`s), `TokenizerProvider` (`tokenizerFor(language)`, and `changes`, which emits
  when the available tokenizers may have changed) and the Sudachi implementation.
  `TokenizerUnavailableException` means "can't answer right now", not "no tokens".
  Code above this module asks the provider and never assumes where a tokenizer comes from.
  Today `BundledTokenizerProvider` offers the one bundled Sudachi tokenizer. `TokenizerImpl` lives in the shared `jvmAndAndroidMain` source set; the
  dictionary loading and the Koin module are `expect`/`actual` per platform. The dictionary is
  loaded lazily on the first `tokenize` call, so call it off the main thread.
- `:core:packs` — the generic pack system, which knows no file format: `PackManager` (catalog, download,
  install, remove, `packs: StateFlow<List<Pack>>`), `PackReader`/`CloseableReader` (what an installed
  pack opens into) and the `PackFormat` interface that other modules implement. The contract
  (`PackManager`, `Pack`, `PackState`, `CatalogPack`, `PackReader`) is in `commonMain` and free of
  `java.*`; the file, SQLite-free installer and Ktor code is in the shared `jvmAndAndroidMain` source
  set, and only the Koin module (`packsModule`) is `expect`/`actual`. It collects every `PackFormat`
  registered in Koin with `getAll<PackFormat>()`, so a module that owns a format only registers it
  (`single { JmdictFormat } bind PackFormat::class`). Installed packs live in a folder called
  `dictionaries` (see "Dictionary packs"); the name is historical and must not change.
- `:core:dictionary` — the dictionary: the `MonDictionary` capability (word lookup in some language
  pair; it does not extend `PackReader`, so an online dictionary can implement it too),
  `DictionaryProvider` (`dictionariesFor(source, gloss)`), the JMdict reader and file format and
  `PackDictionaryProvider`, which offers the `MonDictionary` readers of the installed packs and
  repeats a lookup with the pack's current reader when it was closed by an update or removal. A
  lookup that can't be answered throws `DictionaryUnavailableException`. It plugs `JmdictFormat`
  into `:core:packs` (`dictionaryModule`). See "Dictionary packs" below.
  The architecture diagrams are in `docs/dictionary-architecture.md`.
- `:tools:pack-converter` — command-line tool (not part of the app) that turns dictionary source
  files into packs and writes the catalog. One subcommand per source (`PackSource`); today the only
  one is `jmdict` (jmdict-simplified JSON files). The packaging and the catalog merge (`publish`)
  are shared by all sources. It is the only user of `JmdictWriter`.
- `:core:database` — Room database (`MonDatabase`) and DAOs, with KSP codegen.
- `:core:data` — repository layer: the `ReadingRepository` interface and its Room-backed
  implementation, and `UserRepository`. A reading stores the id of the tokenizer that produced its
  tokens (`reading.tokenizedWith`, in `TokenAnalysis`); null means it is pending, shown as
  `isTokenized = false` on `Reading` and `ReadingSummary`. `saveAnalysis` only writes to a pending
  reading, so tokenizing the same reading twice is harmless. It only maps between Room entities and `:core:model`.
- `:core:ui` — reusable Compose UI that knows about content, including the kanji lookup
  screens and `VocabEntryItem`.
- `:core:designsystem` — Compose design system: theme, components, modifiers, icon catalog.
  It holds no domain models, only primitives and component parameters.
- `:core:navigation` — the Navigation 3 keys (`MonNavKey`: `Library`, `AddReading`, `Dictionaries`, `Reader`) and
  `MonNavConfiguration`, the one place that registers them for back-stack serialization.
- `:core:arch` — small shared building blocks: `BaseViewModel<State, Intent, Effect>`.
- `:feature:library` — the library feature: list of saved readings.
- `:feature:addreading` — the add-reading feature: a form that creates a reading and its tokens.
- `:feature:dictionaries` — the Dictionaries screen (a bottom-bar tab): lists the
  packs of `PackManager` with download, progress, update, retry and remove, and shows each
  pack's licence attribution. It talks to the `PackManager` interface directly; downloads run
  in the manager's own scope, so leaving the screen does not cancel them.
- `:androidApp`, `:desktopApp`, `:iosApp` — platform entrypoints. All three embed
  `:shared`; the iOS app is an Xcode project that bridges into it.

Dependency direction: `:shared` depends on all `core:*` and `feature:*`
modules, which depend on lower layers. Platform-specific implementations live in
the `*Main` source sets (`androidMain`, `jvmMain`).

Layering rule: a feature may read through a repository directly for a simple query, for
example the library list uses `ReadingRepository.observeReadingSummaries()`. Writes, and logic
that combines repositories or the tokenizer, go through a use case in `:core:domain`, for
example `AddReadingUseCase`. Screens and view models never touch `:core:database` or
`:core:tokenizer` implementations. Lists use light summary models (`ReadingSummary`), and
the full `Reading` with its tokens is loaded only for one reading (`getReading`).

Dependency injection is Koin. All modules register their Koin modules in
`:shared` (`Koin.kt`, `initKoin`); new modules must be added there.

Presentation and navigation:

- A feature has a `*Contract.kt` (state, sealed intents, effects), a view model extending
  `BaseViewModel<State, Intent, Effect>`, and a stateless screen `Screen(state, onIntent)`.
  Intents are input from the UI through `onIntent`. State is what is rendered. Effects are
  one-off events (navigation, snackbars) sent with `sendEffect` and delivered once through
  `effects`; never signal them through state. Use `NoEffect` when a screen has none.
- View models expose typed errors (an enum or sealed type such as `LibraryError`), never
  exception messages. The screen maps them to string resources.
- Features do not depend on `:core:navigation`. They emit their own effects (for example
  `LibraryEffect.OpenReader`), and `MonNavHost` in `:shared` collects them and maps them to nav
  keys. To add a destination: add the key to `MonNavKey`, register it in `MonNavConfiguration`
  (the only place; forgetting it crashes on back-stack restore), and add an `entry<…>` to
  `MonNavHost`. `Reader` is still a stub entry.
- The bottom bar is rendered by `MonNavHost`, not by the features, and only while a tab's screen is
  on top. Its tabs (`Library`, `Dictionaries`) are the `tabs` list there; `MonAppNavigator.switchTab`
  keeps `Library` at the bottom of the back stack. The visual spec is `docs/design/mon-handoff-spec.md`.
- Strings live in each feature's `src/commonMain/composeResources/values/strings.xml`. The
  `mon.feature` convention plugin gives each feature its own resource package, derived from the
  module path (`:feature:library` → `com.akrippner.mon.feature.library.generated.resources`).
  Do not hardcode UI strings.

## Quirks and gotchas

- Module builds use the convention plugins in the included `build-logic` build: `mon.kmp.library`
  (targets, Android library setup), `mon.kmp.compose` (Compose) and `mon.feature` (both, plus the
  shared feature dependencies and per-feature resource package). A module's `build.gradle.kts`
  declares only its own dependencies, using the `projects.*` accessors. `mon.feature` also fails
  configuration when a feature directly depends on `:core:database` or on another feature; reach
  persistence through `:core:data`, and wire features together in `:shared`.

- Icon catalog is generated code. `core:designsystem` has a manual
  `generateIconCatalog` task that produces `IconIds.generated.kt` and
  `IconCatalog.generated.kt`, and those generated files are checked in.
  The task is **not** wired into the build, so run it manually after changing
  icon vector XMLs and commit the regenerated files.
- Room schema exports live in `core/database/schemas/` and are regenerated by
  KSP. If you change the database entities, update the schema files and the
  `MonDatabase` version accordingly. Every version after 2 needs a `Migration` in
  `Migrations.kt` (or an `AutoMigration`) and a test in `MonDatabaseMigrationTest`; only version 1
  may still be dropped, so a missing migration fails instead of wiping user data.
- Tests live in `commonTest` of `:core:database`, `:core:data`, `:core:domain`, `:core:model`,
  `:feature:addreading`, `:feature:dictionaries` and `:feature:library`, plus `jvmTest` in `:core:database` for the migration test (it reads the
  exported schemas), `jvmTest` in `:core:dictionary` and `test` in `:tools:pack-converter`; other
  modules have no test source sets. Database tests run
  against an in-memory database with the bundled SQLite driver. Domain and view model
  tests use hand-written fakes of `ReadingRepository`, `TokenizerProvider` and `DictionaryProvider`, and view model
  tests use `Dispatchers.setMain`. The dictionary tests build tiny real packs with `JmdictWriter`
  and use a hand-written `FakePackDownloader` plus Ktor's `MockEngine`; `NotesFormat` is a test-only
  pack kind that is not a word dictionary. The converter tests use a fake `PackSource`, and
  `PreviousCatalogTest` talks to an HTTP server it starts on the loopback address. No emulator or
  outside server is needed.
- `system_core.dic` — the Sudachi system dictionary — is a git-LFS file located
  in `:shared` resources. It is loaded at runtime by the tokenizer (copied to
  the app cache dir on Android). Do not delete or regenerate it.
- The iOS targets in `:shared/build.gradle.kts` are commented out. The iOS app
  only builds via the Xcode project in `iosApp/`.
- `compileSdk` is 37 because Ktor's OkHttp engine pulls in OkHttp 5.5, which requires it.
  `targetSdk` stays 36. AGP downloads the missing platform on first build.

## Dictionary packs

The app is tied to no dictionary or language: `MonDictionary` is an interface, and dictionaries are
downloadable packs, not bundled (only the Sudachi `system_core.dic` is). The app works without any
pack. Do not read `*.db` or `jmdict-*.json` files; they are large.

- **Readers and capabilities.** Every installed pack opens into a `PackReader` (only `pack: CatalogPack`).
  What a reader can answer is a capability interface that the reader implements next to it:
  `MonDictionary` (`lookup`) is the only one today. Capabilities do not extend `PackReader`.
  `PackManager.readers()` returns the readers of all kinds, sorted by pack id; the `internal`
  extension `installed()` in `:core:dictionary` keeps the `MonDictionary` ones. The manager, the installer and the Dictionaries screen do not care what
  kind a pack is. `PackFormat.open` must stay cheap (no file access), because `readers()`
  opens every installed pack; a reader opens its file on the first real call.
- **Where it lives.** A pack is `<base>/<packId>/dictionary.db` plus `pack.json` (the installed
  `CatalogPack`). `pack.json` is written last, and an update deletes the old one before it replaces
  the database, so a half-finished install or update never counts as installed. `<base>` is
  `noBackupFilesDir/dictionaries` on Android and `~/.mon/dictionaries` on desktop. On its first
  scan the manager calls `PackInstaller.sweep()`, which deletes what a killed install left behind:
  temporary files and pack folders without a `pack.json`.
- **Catalog.** The app hard-codes one URL, `CATALOG_URL` in `PackManagers.kt` (`:core:packs`): the
  `catalog.json` of the latest GitHub release of the public repo `alex-krippner/mon-data`. The
  catalog points to the `.db.gz` files and carries size, `unpackedSizeBytes`, sha256 and the licence
  attribution. The app offers only packs whose `format`, `schema` and languages it supports, and
  each id once. `refreshCatalog()` never throws; it returns a `CatalogError` (or null), which the
  Dictionaries screen shows.
- **Installing.** `FilePackManager` keeps the state (`packs: StateFlow<List<Pack>>`);
  `PackInstaller` does the file work (https only, ids must match `[a-z0-9-]+`, free-space check,
  sha256, gunzip, validate with `PackFormat.verify`, then commit). Ktor is used only in
  `KtorPackDownloader`, behind the `PackDownloader` interface. A reader must be closed before its
  file is replaced or deleted; the manager does that under its `files` lock. A closed reader throws
  `PackClosedException`, and `PackDictionaryProvider` then fetches the readers again and repeats
  the lookup (or throws `DictionaryUnavailableException` if the pack is gone). The sha256 of a pack is computed by `sha256Hex`, shared by the app and the converter.
- **The word-list format.** `jmdict-sqlite` is Mon's format for any word list in any language pair,
  not only JMdict: every source that maps to `JmdictRecord` (headwords, senses, labels, `common`)
  uses it. The wire id and the Kotlin names keep "jmdict"; renaming the id would hide installed
  packs.
  - `lookup_key.key` is matched exactly, so the file itself says how its keys are normalized, in
    the meta row `keyNormalization` (`KeyNormalization` in `LookupKeys.kt`). `JmdictWriter(path,
    sourceLanguage)` picks the rule from the language (the only place a language decides it),
    normalizes each key and writes the row. `JmdictDictionary` reads the row and applies the same
    rule to each query; it never derives the rule from the pack's language.
  - No row means `NONE`: Japanese, and every pack published so far. Other languages get
    `nfc-lower` (Unicode NFC, then lower case). That is not case folding: "STRASSE" does not find
    "Straße".
  - **Never change what an existing `KeyNormalization` value does;** a new rule is a new value.
    `verify` rejects a file with a value the app does not know. Moving a language that already has
    published packs to a new value also needs a new `schema`, so that older apps hide the pack
    instead of failing after the download.
  - Normalization is not lemmatization: "Häuser" → "Haus" is the tokenizer's job. Today the only
    tokenizer is Sudachi, which is Japanese-only, so a reading in another language is stored
    without tokens, and a pack whose source language is not Japanese can be installed but is not
    reached from a reading yet.
- **Adding a source or a format.**
  - *A new word source, any language pair* (e.g. JMnedict, Wadoku): a package in
    `:tools:pack-converter` with a parser, a mapper to `JmdictRecord` and a `PackSource` that passes
    its source language to `JmdictWriter`; add it to `SOURCES` in `Main.kt`; add any new language
    to `Language` (`:core:model`). No other app change, unless the language needs its own key rule:
    then add a `KeyNormalization` value and return it from `forLanguage`, before the language's
    first pack is published.
  - *A new storage format for words:* in `:core:dictionary/<format>/`, a schema with
    `FORMAT`/`VERSION`, a writer (used by the converter) and a reader that is
    `MonDictionary, CloseableReader`; a `PackFormat`, added to the list in
    `createPackManager`; then the converter source. The manager does not change.
  - *A new kind of data* (kanji, example sentences, pitch accent): as a new storage format, but the
    reader implements a new capability interface `: PackReader` in `commonMain` instead of
    `MonDictionary`. Add its models to `:core:model`, a provider like
    `PackDictionaryProvider` that retries on `PackClosedException`, a use case in `:core:domain`
    that asks that provider, and the UI.
  - *Another JMdict gloss language:* add it to `GlossLanguage` (converter) and to `Language`
    (`:core:model`), or the app hides the pack. `JmdictSourceTest` fails if the second is missing.
- **Publishing a data update** (the licence requires keeping the data current, so do this regularly,
  at least monthly):
  1. From the same release at https://github.com/scriptin/jmdict-simplified/releases, download the
     full single-language files you publish (`jmdict-eng-<version>.json.tgz`,
     `jmdict-ger-<version>.json.tgz`; not the `common` or `all` variants) and unpack them. The
     languages the converter knows are the entries of `GlossLanguage`. Every file must come from
     the same release, because all packs of a run share one `dictDate` and release tag.
  2. `./gradlew :tools:pack-converter:run --args="jmdict <outDir> <path>/jmdict-eng-<version>.json <path>/jmdict-ger-<version>.json"`
     The first argument is the source. The language is read from each file. It writes one
     `jmdict-<lang>-<dictDate>.db.gz` per file plus a single `catalog.json`, and prints the release
     command. Options go between the source and `<outDir>`:
     - `--repo <owner/repo>` names the repository that will hold the release.
     - By default the converter first downloads the `catalog.json` of that repository's latest
       release and carries the other sources' packs over. If that fails, it stops before
       converting. `--previous-catalog <file>` reads a local file instead.
     - `--no-previous` starts a catalog with only this run's packs. Use it only for a repository's
       very first release: it drops every other source's packs.
     - If the previous catalog has a pack of this source that the run does not build (a forgotten
       language file, for example), the run fails before it writes anything. `--allow-drop`
       publishes anyway and removes that pack from the catalog on purpose.
  3. Create the release in `mon-data` with the printed command (`gh release create jmdict-<dictDate>
     … --latest`), which uploads every `.db.gz` and the `catalog.json`. Do this before converting
     another source: the merge reads the *published* catalog, so two conversions without a release
     in between lose the first one's packs.
- **Catalog and release rules.** The app reads only the latest release's `catalog.json`, so that
  one file must list every pack of every source.
  - A pack id is `<source id>-<…>`, and a source owns that prefix: its run replaces all of its packs
    in the catalog and keeps everything else. So always convert all of a source's packs in one run
    (for JMdict: all languages); the converter refuses a run that would drop one, unless it gets
    `--allow-drop`. No source id may be `<another id>-…`; the converter checks this.
  - Never delete an old `mon-data` release or its files. Pack URLs are absolute, and the current
    catalog points to other sources' archives in earlier releases.
  - Every release marked latest must contain `catalog.json`, or the app gets a 404.
  - Never remove or rename a `CatalogPack` field, and every pack carries every field with a valid
    value. The app decodes the whole catalog before it filters, so one pack with a missing field
    makes the whole catalog `CatalogError.INVALID` for older apps. A kind without a gloss language
    repeats its source language.
  - A new `CatalogPack` field must have a default value. Otherwise the installed `pack.json` files
    stop decoding and the packs vanish, while `sweep()` keeps their folders. The converter reads
    the previous catalog strictly, so an older converter stops instead of dropping the new field.
  - New formats, and new fields old apps can ignore, are fine: an app hides packs whose format,
    schema or language it does not know.
  - A change that old readers can't read needs a new `schema`. Known limit: older apps then get no
    more updates for that pack, because `publish` lists each id once.
- **Licence.** JMdict is © the EDRDG, CC BY-SA 4.0. The app must show the pack's `attribution` text
  on a separate screen (`:feature:dictionaries`, done), and `mon-data` carries the
  licence in its README. The `common` flag of an entry is a column; the reader adds the
  `Frequency("common")` label, so the converter must not.

## Library and API documentation
- When you need to search docs, use Context7.
