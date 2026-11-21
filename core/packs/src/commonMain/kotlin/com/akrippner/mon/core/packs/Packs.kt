package com.akrippner.mon.core.packs

import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/** The file the converter publishes next to the pack files. */
@Serializable class Catalog(val packs: List<CatalogPack>)

/**
 * One downloadable pack of any kind (a word dictionary, the Sudachi tokenizer dictionary, ...). The
 * same JSON is stored on disk as `pack.json` once installed. Never remove or rename a field, and
 * give a new one a default value, or installed `pack.json` files stop decoding.
 */
@Serializable
data class CatalogPack(
  val id: String, // "<source id>-<…>", e.g. "jmdict-eng" or "sudachi-core"
  val name: String,
  val sourceLanguage: String, // Language tag, e.g. "ja"
  val glossLanguage: String, // e.g. "en"; a kind without one repeats sourceLanguage
  val format: String, // e.g. "jmdict-sqlite" or "sudachi-system", picks the PackFormat
  val schema: Int,
  val version: String,
  val url: String, // the gzipped pack file (.db.gz), https only
  val sizeBytes: Long,
  val unpackedSizeBytes: Long,
  val sha256: String,
  val attribution: String,
)

enum class PackError {
  NETWORK,
  CHECKSUM,
  STORAGE,
  UNSUPPORTED,
  NOT_ENOUGH_SPACE,
  /** The last removal failed and the pack is still installed. Retrying means removing again. */
  REMOVE,
}

/** Why the catalog could not be refreshed. */
enum class CatalogError {
  /** The catalog could not be fetched: offline, or the server answered with an error. */
  NETWORK,
  /** The catalog was fetched but can't be read, so updates stay hidden until it is fixed. */
  INVALID,
}

/**
 * A pack and where it stands. [info] is the newest version known: the catalog's, or the on-disk one
 * offline.
 */
data class Pack(val info: CatalogPack, val state: PackState)

sealed interface PackState {
  data object NotInstalled : PackState

  /** [installed] is the version on disk while an update downloads, or null for a first install. */
  data class Downloading(val progress: Float, val installed: CatalogPack? = null) : PackState

  /** The installed version is [Pack.info] itself. */
  data object Installed : PackState

  /** The catalog has a newer version; [installed] is the one on disk, which still works. */
  data class UpdateAvailable(val installed: CatalogPack) : PackState

  /**
   * The last download or removal failed. [installed] is the version still on disk (a failed update
   * leaves the old one usable), or null if nothing is installed.
   */
  data class Failed(val error: PackError, val installed: CatalogPack? = null) : PackState
}

/**
 * Single entry point for downloadable packs: it knows the catalog, installs, updates and removes
 * packs, and hands out readers for the installed ones. It knows no file format and does not care
 * what kind of data a pack holds; formats and capabilities (dictionary, tokenizer) live in the
 * modules that own them.
 *
 * Installs and removals are serialized, and a reader is closed before its file is replaced or
 * deleted. State is observed through [packs]; operations report problems there or in their return
 * value instead of throwing.
 */
interface PackManager {
  /** The catalog joined with what is installed. Works offline: installed packs always show. */
  val packs: StateFlow<List<Pack>>

  /**
   * Fetches the catalog. It never throws: a failure keeps the old list and is returned, and null
   * means the list is up to date.
   */
  suspend fun refreshCatalog(): CatalogError?

  /** Progress and errors show up in [packs]. */
  suspend fun install(packId: String)

  suspend fun remove(packId: String)

  /**
   * The readers of the installed packs, of every kind, sorted by pack id. An update or removal
   * closes a pack's reader, after which its calls throw [PackClosedException]; call this again to
   * get the current ones.
   */
  suspend fun readers(): List<PackReader>
}
