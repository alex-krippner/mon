# Dictionary architecture

How the dictionary feature fits into the app. Everything drawn in the class diagram exists in the
code; the only planned part is the Reader's lookup sheet, which will call `LookupWordUseCase`. The
module edges are simplified from `AGENTS.md`.

Design decisions:

- The app is tied to no dictionary and no language. `MonDictionary` is an interface; entries are
  data classes; labels are a sealed hierarchy.
- JMdict (English glosses) is the first dictionary.
- Dictionaries are downloadable packs listed in a hosted `catalog.json`, not bundled. The app works
  without any pack.
- `system_core.dic` (Sudachi) stays bundled.

## 1. Modules

Yellow dashed boxes are planned.

```mermaid
flowchart TB
  classDef planned fill:#fff3cd,stroke:#b58900,stroke-dasharray: 5 3,color:#000

  subgraph Platforms
    androidApp
    desktopApp
    iosApp
  end

  shared["<b>:shared</b><br/>App(), MonNavHost, initKoin<br/>Sudachi system_core.dic"]

  subgraph Features
    library[":feature:library"]
    addreading[":feature:addreading"]
    dictionaries[":feature:dictionaries<br/>pack list, download, licence attribution"]
  end

  subgraph Core
    nav[":core:navigation<br/>MonNavKey.Dictionaries"]
    arch[":core:arch"]
    ui[":core:ui<br/>LookupBottomSheet"]
    ds[":core:designsystem"]
    domain[":core:domain<br/>AddReadingUseCase<br/>LookupWordUseCase"]
    data[":core:data"]
    db[":core:database (Room)"]
    tok[":core:tokenizer (Sudachi)"]
    dict[":core:dictionary<br/>MonDictionary, packs, JMdict reader"]
    model[":core:model<br/>DictionaryEntry, Label, LookupQuery"]
  end

  subgraph Tools
    conv[":tools:jmdict-converter"]
  end

  mondata[("GitHub: alex-krippner/mon-data<br/>catalog.json + jmdict-eng.db.gz")]:::planned

  androidApp --> shared
  desktopApp --> shared
  iosApp --> shared
  shared --> library & addreading & dictionaries & nav
  library & addreading & dictionaries --> domain
  library & addreading & dictionaries --> ui
  library & addreading & dictionaries --> arch
  dictionaries --> dict
  ui --> ds
  domain --> data & tok & dict & model
  data --> db & model
  dict --> model
  conv --> dict
  conv -. "publishes" .-> mondata
  dict -. "HTTPS (Ktor)" .-> mondata
  shared -. "dictionaryModule (Koin)" .-> dict
```

The module rule still holds: features never depend on `:core:database` or on each other.
`:feature:dictionaries` reaches the dictionary only through `:core:domain` or the
`DictionaryManager` interface, and `:shared` wires them together.

## 2. Classes

The stereotype names the visibility and, for classes outside `:core:dictionary`, the module.

```mermaid
classDiagram
  direction LR

  %% ---------- :core:dictionary commonMain (public contract) ----------
  class MonDictionary {
    <<interface>>
    +source: DictionarySource
    +sourceLanguage: Language
    +glossLanguage: Language
    +lookup(query: LookupQuery) List~DictionaryEntry~
  }
  class DictionaryClosedException {
    <<exception>>
  }
  class CloseableDictionary {
    <<internal interface>>
    +close()
  }
  class DictionaryManager {
    <<interface>>
    +packs: StateFlow~List~Pack~~
    +refreshCatalog() CatalogError?
    +install(packId)
    +remove(packId)
    +installed() List~MonDictionary~
  }
  class Pack {
    <<data>>
    +info: CatalogPack
    +state: PackState
  }
  class PackState {
    <<sealed>>
    NotInstalled
    Downloading(progress)
    Installed
    UpdateAvailable(installed)
    Failed(error, installed)
  }
  class CatalogPack {
    <<data, serializable>>
    id, name, format, schema
    sourceLanguage, glossLanguage
    version, url, sha256
    sizeBytes, unpackedSizeBytes
    attribution
  }
  class Catalog {
    <<serializable>>
    +packs: List~CatalogPack~
  }
  class PackError {
    <<enum>>
    NETWORK
    CHECKSUM
    STORAGE
    UNSUPPORTED
    NOT_ENOUGH_SPACE
  }
  class CatalogError {
    <<enum>>
    NETWORK
    INVALID
  }

  %% ---------- :core:model ----------
  class DictionaryEntry {
    <<data :core:model>>
    id, source, headwords
    senses, labels
  }
  class DictionarySource {
    <<data :core:model>>
    id, name, attribution, version
  }
  class Headword {
    <<data :core:model>>
    text, reading
  }
  class Sense {
    <<data :core:model>>
    glosses, labels
  }
  class Label {
    <<sealed :core:model>>
    PartOfSpeech, Gender, Register
    Domain, Frequency, Other
  }
  class LookupQuery {
    <<data :core:model>>
    lemma, surface, reading
  }

  %% ---------- :core:dictionary jvmAndAndroidMain: packs ----------
  class FileDictionaryManager {
    <<internal>>
    -current: State
    -files: Mutex
    -opened: Map~String, CloseableDictionary~
    -scope: CoroutineScope
    -catalogUrl: String
  }
  class State {
    <<private data>>
    catalog: List~CatalogPack~
    installed: Map~String, CatalogPack~
    activity: Map~String, PackState~
    removing: Set~String~
    toPacks() List~Pack~
  }
  class DictionaryFactory {
    <<internal>>
    +formatOf(pack) DictionaryFormat?
  }
  class DictionaryFormat {
    <<internal interface>>
    +format: String
    +schema: Int
    +verify(file, pack) Boolean
    +open(file, pack) CloseableDictionary
  }
  class PackInstaller {
    <<internal>>
    -baseDir: File
    +download(pack, verify, onProgress) File
    +commit(pack, database)
    +remove(packId) Boolean
    +installedPacks() List~CatalogPack~
    +sweep()
    +databaseFile(packId) File
  }
  class PackDownloader {
    <<internal interface>>
    +fetchText(url) String
    +download(url, target, fallbackBytes, onProgress)
  }
  class KtorPackDownloader {
    <<internal>>
    -client: HttpClient
  }
  class PackException {
    <<internal exception>>
    +error: PackError
  }
  class Sha256 {
    <<public file>>
    +sha256Hex(file) String
  }

  %% ---------- :core:dictionary jvmAndAndroidMain: JMdict ----------
  class JmdictFormat {
    <<internal object>>
  }
  class JmdictDictionary {
    <<internal>>
    -path: String
    -mutex: Mutex
    -connection: SQLiteConnection?
    -closed: Boolean
  }
  class EntryPayload {
    <<internal serializable>>
    headwords, senses, labels
  }
  class JmdictWriter {
    <<public>>
    +putMeta(key, value)
    +insert(record)
    +finish()
    +close()
  }
  class JmdictRecord {
    <<public>>
    id, common
    headwords, senses, labels
  }
  class JmdictSchema {
    <<public object>>
    FORMAT, VERSION
    CREATE (internal)
  }

  %% ---------- :core:domain ----------
  class LookupWordUseCase {
    <<public :core:domain>>
    +execute(token, source, gloss) LookupResult
  }
  class LookupResult {
    <<sealed :core:domain>>
    Found(entries)
    NotFound
    NoDictionaryInstalled
  }

  %% ---------- :feature:dictionaries ----------
  class DictionariesViewModel {
    <<public :feature:dictionaries>>
    +onIntent(intent)
  }
  class DictionariesState {
    <<data :feature:dictionaries>>
    packs: List~Pack~
    isRefreshing: Boolean
    refreshError: CatalogError?
  }

  %% ---------- :tools:jmdict-converter ----------
  class Converter {
    <<file :tools:jmdict-converter>>
    convert(input, outDir, repo) ConversionResult
  }
  class SimplifiedDictionary {
    <<serializable :tools:jmdict-converter>>
    version, dictDate, languages
    commonOnly, tags, words
  }

  %% ---------- inheritance ----------
  CloseableDictionary --|> MonDictionary
  JmdictDictionary ..|> CloseableDictionary
  FileDictionaryManager ..|> DictionaryManager
  KtorPackDownloader ..|> PackDownloader
  JmdictFormat ..|> DictionaryFormat

  %% ---------- contract and model ----------
  DictionaryManager --> Pack
  DictionaryManager ..> CatalogError : refreshCatalog returns
  DictionaryManager ..> MonDictionary : installed returns
  Pack --> CatalogPack
  Pack --> PackState
  PackState --> PackError
  Catalog --> CatalogPack
  MonDictionary --> DictionarySource
  MonDictionary ..> LookupQuery
  MonDictionary ..> DictionaryEntry
  MonDictionary ..> DictionaryClosedException : throws
  DictionaryEntry --> DictionarySource
  DictionaryEntry --> Headword
  DictionaryEntry --> Sense
  DictionaryEntry --> Label
  Sense --> Label

  %% ---------- packs ----------
  FileDictionaryManager *-- State
  State --> CatalogPack
  State --> PackState
  FileDictionaryManager --> PackInstaller
  FileDictionaryManager --> PackDownloader : fetch catalog
  FileDictionaryManager --> DictionaryFactory
  FileDictionaryManager --> CloseableDictionary : keeps open readers
  DictionaryFactory --> DictionaryFormat
  PackInstaller --> PackDownloader
  PackInstaller ..> Sha256 : checks download
  PackInstaller ..> PackException : throws
  KtorPackDownloader ..> PackException : throws
  PackException --> PackError

  %% ---------- JMdict ----------
  JmdictFormat ..> JmdictDictionary : creates
  JmdictFormat ..> JmdictSchema
  JmdictDictionary ..> EntryPayload : reads
  JmdictWriter ..> EntryPayload : writes
  JmdictWriter ..> JmdictRecord
  JmdictWriter ..> JmdictSchema

  %% ---------- users ----------
  LookupWordUseCase --> DictionaryManager
  LookupWordUseCase ..> LookupResult
  LookupWordUseCase ..> DictionaryClosedException : retries on
  LookupResult --> DictionaryEntry
  DictionariesViewModel --> DictionaryManager
  DictionariesViewModel --> DictionariesState
  DictionariesState --> Pack
  Converter ..> SimplifiedDictionary : parses
  Converter ..> JmdictRecord : maps words to
  Converter --> JmdictWriter
  Converter ..> JmdictSchema
  Converter ..> Sha256
  Converter ..> Catalog : writes catalog.json
```

## How to read it

- **Boundary:** the public contract (`MonDictionary`, `DictionaryManager`, `Pack`, `PackState`,
  `CatalogPack`) is in `commonMain` and has no `java.*`, which keeps iOS and web open.
  `PackInstaller`, `PackDownloader`, `JmdictDictionary` and the other classes that use
  `java.io.File` stay in `jvmAndAndroidMain`.
- **Ktor seam:** only `KtorPackDownloader` knows Ktor. Replacing it (for example with WorkManager)
  touches that one class.
- **Format seam:** `DictionaryFormat` and `DictionaryFactory` are the only link between a catalog
  `format` string and a reader. A German dictionary adds one `DictionaryFormat` and no change to
  the manager.
- **State:** `FileDictionaryManager` keeps one private `State` and derives `packs` from it on every
  change. `PackInstaller` keeps no state; it only does the file work.
- **UI path:** `LookupWordUseCase` and `DictionariesViewModel` depend only on the
  `DictionaryManager` interface. They never see files, Ktor or SQLite.
- **Converter:** `JmdictWriter`, `JmdictRecord`, `JmdictSchema` and `sha256Hex` are public only
  because `:tools:jmdict-converter` uses them; the app itself only reads.
- **Not drawn:** the Reader's lookup sheet, which will call `LookupWordUseCase` (a follow-up).
