package com.akrippner.mon.core.dictionary.pack

import com.akrippner.mon.core.dictionary.CloseableReader
import com.akrippner.mon.core.dictionary.PackReader
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

private data class State(
  val catalog: List<CatalogPack> = emptyList(),
  val installed: Map<String, CatalogPack>? = null,
  val activity: Map<String, PackState> = emptyMap(),
  val removing: Set<String> = emptySet(),
)

private fun State.toPacks(): List<Pack> {
  val onDisk = installed.orEmpty()
  fun activityOf(id: String): PackState? =
    when (val current = activity[id]) {
      is PackState.Failed -> current.copy(installed = onDisk[id])
      is PackState.Downloading -> current.copy(installed = onDisk[id])
      else -> current
    }
  val fromCatalog =
    catalog.map { info -> Pack(info, activityOf(info.id) ?: stateOf(info, onDisk[info.id])) }
  val diskOnly =
    onDisk.values
      .filter { disk -> catalog.none { it.id == disk.id } }
      .map { Pack(it, activityOf(it.id) ?: PackState.Installed) }
  return fromCatalog + diskOnly
}

private fun stateOf(info: CatalogPack, onDisk: CatalogPack?): PackState =
  when {
    onDisk == null -> PackState.NotInstalled
    onDisk.sha256 == info.sha256 -> PackState.Installed
    else -> PackState.UpdateAvailable(onDisk)
  }

internal class FileDictionaryManager(
  private val downloader: PackDownloader,
  private val installer: PackInstaller,
  private val factory: DictionaryFactory,
  private val catalogUrl: String,
  /** Runs the installations. Lives as long as the manager, i.e. the app. */
  private val scope: CoroutineScope,
) : DictionaryManager {

  @Volatile private var current = State()
  private val _packs = MutableStateFlow<List<Pack>>(emptyList())
  override val packs: StateFlow<List<Pack>> = _packs.asStateFlow()

  private val json = Json { ignoreUnknownKeys = true }

  /** Serializes "close reader, then replace or delete the file" against [readers]. */
  private val files = Mutex()

  /** Open readers by pack id. Only touched while holding [files]. */
  private val opened = mutableMapOf<String, CloseableReader>()

  private fun change(block: (State) -> State): State =
    synchronized(this) {
      current = block(current)
      _packs.value = current.toPacks()
      current
    }

  /**
   * Whether the leftovers of an earlier run have been deleted. Only touched while holding [files].
   */
  private var swept = false

  /**
   * Rescans the disk. Call it while holding [files], so a scan can't overwrite newer state. The
   * first scan also deletes what a killed install left behind. That is safe there: a download needs
   * a catalog, and the catalog is only set after the first scan.
   */
  private suspend fun reloadInstalled() {
    val onDisk =
      withContext(Dispatchers.IO) {
        if (!swept) {
          installer.sweep()
          swept = true
        }
        installer.installedPacks()
      }
    change { it.copy(installed = onDisk.associateBy { pack -> pack.id }) }
  }

  private fun setActivity(packId: String, state: PackState?) {
    change { s ->
      s.copy(activity = if (state == null) s.activity - packId else s.activity + (packId to state))
    }
  }

  /** Marks the pack as downloading and returns it, or null if unknown, busy or being removed. */
  private fun claim(packId: String): CatalogPack? {
    var claimed: CatalogPack? = null
    change { s ->
      val pack = s.catalog.firstOrNull { it.id == packId }
      if (pack == null || s.activity[packId] is PackState.Downloading || packId in s.removing) {
        s // unknown pack, or someone already downloads or removes it: change nothing
      } else {
        claimed = pack
        s.copy(activity = s.activity + (packId to PackState.Downloading(0f)))
      }
    }
    return claimed
  }

  /** Atomically marks the pack as being removed, unless it is downloading. */
  private fun beginRemoval(packId: String): Boolean {
    var allowed = false
    change { s ->
      if (s.activity[packId] is PackState.Downloading || packId in s.removing) {
        s
      } else {
        allowed = true
        s.copy(removing = s.removing + packId)
      }
    }
    return allowed
  }

  /**
   * Closes the reader first and forgets it only afterwards, so a cancelled close loses nothing. A
   * reader whose close failed is forgotten too: it counts as closed and answers nothing any more,
   * so [readers] must not hand it out again.
   */
  private suspend fun closeReader(packId: String) {
    try {
      opened[packId]?.close()
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      opened.remove(packId)
      throw e
    }
    opened.remove(packId)
  }

  override suspend fun refreshCatalog(): CatalogError? {
    files.withLock { reloadInstalled() }
    val supported =
      try {
        json
          .decodeFromString<Catalog>(downloader.fetchText(catalogUrl))
          .packs
          .filter { factory.formatOf(it) != null }
          // The catalog is downloaded, so untrusted: the id is the pack's folder and the UI's list
          // key, and a repeated one would crash the screen. The first one wins.
          .distinctBy { it.id }
      } catch (e: PackException) { // network failure
        return CatalogError.NETWORK
      } catch (e: CancellationException) {
        throw e
      } catch (e: SerializationException) { // broken JSON, or a pack with a missing field
        return CatalogError.INVALID
      } catch (e: Exception) { // an engine error that is not an IOException
        return CatalogError.NETWORK
      }
    change { it.copy(catalog = supported) }
    return null
  }

  override suspend fun install(packId: String) {
    val pack = claim(packId) ?: return
    // The download belongs to the manager, not to the caller: leaving the screen that asked for it
    // cancels the wait below, but not the download. ATOMIC makes the body start even if [scope] is
    // already cancelled, so the "Downloading" mark set by claim() is always cleared again.
    scope.launch(start = CoroutineStart.ATOMIC) { runInstall(pack) }.join()
  }

  private suspend fun runInstall(pack: CatalogPack) {
    val packId = pack.id
    val format = factory.formatOf(pack)
    if (format == null) {
      setActivity(packId, PackState.Failed(PackError.UNSUPPORTED))
      return
    }
    var unpacked: File? = null
    try {
      // The downloader reports every chunk it reads, thousands per pack. Only a new whole percent
      // is worth a state update.
      var lastPercent = 0 // claim() already showed 0 %
      val database =
        withContext(Dispatchers.IO) {
          installer.download(pack, format::verify) { progress ->
            val percent = (progress * 100).toInt()
            if (percent != lastPercent) {
              lastPercent = percent
              setActivity(packId, PackState.Downloading(percent / 100f))
            }
          }
        }
      unpacked = database
      files.withLock {
        closeReader(packId)
        // Not cancellable, and also after a failed commit: once the files have changed, the
        // installed list must follow, or readers() would work with the old pack.json.
        withContext(NonCancellable) {
          try {
            withContext(Dispatchers.IO) { installer.commit(pack, database) }
          } finally {
            reloadInstalled()
          }
        }
      }
      setActivity(packId, null)
    } catch (e: PackException) {
      setActivity(packId, PackState.Failed(e.error))
    } catch (e: CancellationException) {
      setActivity(packId, null)
      throw e
    } catch (e: Exception) {
      // Before the download finished: an engine error that is not an IOException. After it: a
      // local problem, and calling that a network error would be wrong.
      setActivity(
        packId,
        PackState.Failed(if (unpacked == null) PackError.NETWORK else PackError.STORAGE),
      )
    } catch (e: Throwable) { // an Error: never leave "Downloading" behind
      setActivity(packId, null)
      throw e
    } finally {
      unpacked?.delete() // gone after a successful move; otherwise don't leave hundreds of MB
    }
  }

  override suspend fun remove(packId: String) {
    if (!beginRemoval(packId)) return
    try {
      var removed = false
      files.withLock {
        closeReader(packId)
        withContext(NonCancellable) {
          try {
            removed = withContext(Dispatchers.IO) { installer.remove(packId) }
          } finally {
            reloadInstalled()
          }
        }
      }
      // Something could not be deleted. If the rescan above still finds the pack, the user must see
      // that the removal did not go through. If only pack.json went, the pack is uninstalled
      // already
      // and sweep() deletes the rest of its folder on the next start.
      val stillInstalled = current.installed.orEmpty().containsKey(packId)
      setActivity(
        packId,
        if (removed || !stillInstalled) null else PackState.Failed(PackError.REMOVE),
      )
    } finally {
      change { it.copy(removing = it.removing - packId) }
    }
  }

  override suspend fun readers(): List<PackReader> =
    files.withLock {
      if (current.installed == null) reloadInstalled()
      // Sorted, so results from several packs always come in the same order.
      current.installed
        .orEmpty()
        .values
        .sortedBy { it.id }
        .mapNotNull { pack ->
          val format = factory.formatOf(pack) ?: return@mapNotNull null
          opened.getOrPut(pack.id) { format.open(installer.databaseFile(pack.id), pack) }
        }
    }
}
