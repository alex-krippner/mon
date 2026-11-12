package com.akrippner.mon.core.dictionary.pack

import com.akrippner.mon.core.dictionary.DictionaryClosedException
import com.akrippner.mon.core.dictionary.MonDictionary
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

/** The file the converter publishes next to the dictionary files. */
@Serializable class Catalog(val packs: List<CatalogPack>)

/** One downloadable dictionary. The same JSON is stored on disk as `pack.json` once installed. */
@Serializable
data class CatalogPack(
  val id: String, // e.g. "jmdict-eng"
  val name: String,
  val sourceLanguage: String, // Language tag, e.g. "ja"
  val glossLanguage: String, // e.g. "en"
  val format: String, // e.g. "jmdict-sqlite", picks the reader
  val schema: Int,
  val version: String,
  val url: String, // the .db.gz file
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

  data class Downloading(val progress: Float) : PackState

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

interface DictionaryManager {
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
   * The readers of the installed packs. An update or removal closes a pack's reader, after which
   * its lookups throw [DictionaryClosedException]; call this again to get the current ones.
   */
  suspend fun installed(): List<MonDictionary>
}
