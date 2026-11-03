# AGENTS.md

Guidance for agents working in this repository. It summarizes what is not obvious
from reading the code alone. Prefer following the executable build configuration
over prose documentation; where `README.md` and the build scripts disagree,
trust the scripts.

## Build and commands

The project uses the Gradle wrapper (`./gradlew`, or `gradlew.bat` on Windows).
Kotlin Multiplatform with AGP 9.1.1 / Kotlin 2.3.21 / Compose Multiplatform 1.11.1.
All targets compile with JVM target 11.

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
  `./gradlew :core:database:jvmTest :core:data:jvmTest :core:domain:jvmTest :core:model:jvmTest :feature:addreading:jvmTest :feature:library:jvmTest`

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
  validates input, tokenizes it with the `Tokenizer` and stores it through a repository.
- `:core:tokenizer` — the `Tokenizer` interface, which returns `MonToken`s, and its Sudachi
  implementation. `TokenizerImpl` lives in the shared `jvmAndAndroidMain` source set; the
  dictionary loading and the Koin module are `expect`/`actual` per platform. The dictionary is
  loaded lazily on the first `tokenize` call, so call it off the main thread.
- `:core:database` — Room database (`MonDatabase`) and DAOs, with KSP codegen.
- `:core:data` — repository layer: the `ReadingRepository` interface and its Room-backed
  implementation, and `UserRepository`. It only maps between Room entities and `:core:model`.
- `:core:ui` — reusable Compose UI that knows about content, including the kanji lookup
  screens and `VocabEntryItem`.
- `:core:designsystem` — Compose design system: theme, components, modifiers, icon catalog.
  It holds no domain models, only primitives and component parameters.
- `:core:navigation` — the Navigation 3 keys (`MonNavKey`: `Library`, `AddReading`, `Reader`) and
  `MonNavConfiguration`, the one place that registers them for back-stack serialization.
- `:core:arch` — small shared building blocks: `BaseViewModel<State, Intent, Effect>`.
- `:feature:library` — the library feature: list of saved readings.
- `:feature:addreading` — the add-reading feature: a form that creates a reading and its tokens.
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
  `:feature:addreading` and `:feature:library`, plus `jvmTest` in `:core:database` for the migration test (it reads the
  exported schemas); other modules have no test source sets. Database tests run
  against an in-memory database with the bundled SQLite driver. Domain and view model
  tests use hand-written fakes of `ReadingRepository` and `Tokenizer`, and view model
  tests use `Dispatchers.setMain`. No emulator or server is needed.
- `system_core.dic` — the Sudachi system dictionary — is a git-LFS file located
  in `:shared` resources. It is loaded at runtime by the tokenizer (copied to
  the app cache dir on Android). Do not delete or regenerate it.
- The iOS targets in `:shared/build.gradle.kts` are commented out. The iOS app
  only builds via the Xcode project in `iosApp/`.

## Library and API documentation
- When you need to search docs, use Context7.
