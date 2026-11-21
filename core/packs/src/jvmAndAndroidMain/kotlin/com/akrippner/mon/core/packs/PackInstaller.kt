package com.akrippner.mon.core.packs

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.REPLACE_EXISTING
import java.util.zip.GZIPInputStream
import kotlinx.serialization.json.Json

private const val DATA_NAME = "pack.dat"
private const val PACK_JSON = "pack.json"
private const val TMP_SUFFIX = ".tmp"
private val ID_PATTERN = Regex("[a-z0-9-]+")

/**
 * The file work of installing a pack: download, verify, unpack, move into place. It keeps no state.
 * Layout per pack: `<baseDir>/<packId>/pack.dat` plus `pack.json`.
 */
class PackInstaller(private val downloader: PackDownloader, private val baseDir: File) {
  private val json = Json { ignoreUnknownKeys = true }

  /**
   * The only way to build a pack's folder. Pack ids come from the downloaded catalog, so they are
   * untrusted: an id like `../x` must never leave [baseDir].
   */
  private fun packDir(packId: String): File {
    if (!ID_PATTERN.matches(packId)) throw PackException(PackError.UNSUPPORTED)
    return File(baseDir, packId)
  }

  fun dataFile(packId: String) = File(packDir(packId), DATA_NAME)

  /**
   * Downloads, verifies and unpacks [pack] into a temporary file and returns it. The installed
   * version, if any, is not touched; call [commit] to put the new file in place.
   */
  suspend fun download(
    pack: CatalogPack,
    verify: (File, CatalogPack) -> Boolean,
    onProgress: (Float) -> Unit,
  ): File {
    if (!pack.url.startsWith("https://")) throw PackException(PackError.UNSUPPORTED)
    val dir = packDir(pack.id)
    storage { if (!dir.isDirectory && !dir.mkdirs()) throw IOException("cannot create $dir") }
    // The archive and the unpacked file exist side by side until the archive is deleted.
    if (dir.usableSpace < pack.sizeBytes + pack.unpackedSizeBytes) {
      throw PackException(PackError.NOT_ENOUGH_SPACE)
    }

    val archive = File(dir, "$DATA_NAME.gz$TMP_SUFFIX")
    val data = File(dir, "$DATA_NAME$TMP_SUFFIX")
    try {
      downloader.download(pack.url, archive, pack.sizeBytes, onProgress)
      // The checks and unpacking below take a while on a phone. Reporting 100 % first lets the
      // screen show them as "Installing" even when the server's size did not match exactly.
      onProgress(1f)
      if (!storage { sha256Hex(archive) }.equals(pack.sha256, ignoreCase = true)) {
        throw PackException(PackError.CHECKSUM)
      }
      gunzip(archive, data)
      if (!verify(data, pack)) throw PackException(PackError.UNSUPPORTED)
      return data
    } catch (e: Throwable) { // also on cancellation: never leave a half-written file behind
      data.delete()
      throw e
    } finally {
      archive.delete()
    }
  }

  /**
   * Moves a verified [data] into place and records [pack] in `pack.json`. A `pack.json` on disk
   * always describes the `pack.dat` next to it: the old one is deleted before the data file is
   * replaced and the new one is put in place last. An update that dies half-way therefore leaves
   * the pack not installed (and [sweep] cleans up), never new data under the old version. Close
   * readers of the old file first.
   */
  fun commit(pack: CatalogPack, data: File) = storage {
    val dir = packDir(pack.id)
    val installed = File(dir, PACK_JSON)
    // Written before anything is touched: on a full disk this fails with the old version intact.
    val metadata = File(dir, "$PACK_JSON$TMP_SUFFIX")
    try {
      metadata.writeText(json.encodeToString(pack))
    } catch (e: IOException) {
      metadata.delete()
      throw e
    }
    Files.deleteIfExists(installed.toPath())
    Files.move(data.toPath(), dataFile(pack.id).toPath(), REPLACE_EXISTING)
    Files.move(metadata.toPath(), installed.toPath(), REPLACE_EXISTING)
  }

  /**
   * Returns false if something could not be deleted: either the pack is still installed
   * (`pack.json` is still there), or it is un-installed but files stay behind until the next
   * [sweep]. Close any open reader first.
   */
  fun remove(packId: String): Boolean {
    val dir = packDir(packId)
    val metadata = File(dir, PACK_JSON)
    // Deleted first: this un-installs the pack at once, even if the big file stays.
    if (!metadata.delete() && metadata.exists()) return false
    return dir.deleteRecursively()
  }

  /** Packs that are fully installed: both `pack.json` and `pack.dat` exist and parse. */
  fun installedPacks(): List<CatalogPack> = packDirs().mapNotNull(::installedIn)

  /**
   * Deletes what a killed install, update or removal left behind: pack folders without a
   * `pack.json`, and temporary files in the others. Without it that space (up to a whole pack) is
   * lost, because the UI offers no "remove" for a pack that is not installed. A folder whose
   * `pack.json` exists but can't be read is kept: it may be a pack this version of the app does not
   * understand. Must not run while a download is in progress; the manager calls it once, before the
   * first one.
   */
  fun sweep() {
    for (dir in packDirs()) {
      if (!File(dir, PACK_JSON).exists()) {
        dir.deleteRecursively()
      } else {
        dir.listFiles().orEmpty().filter { it.name.endsWith(TMP_SUFFIX) }.forEach { it.delete() }
      }
    }
  }

  private fun packDirs(): List<File> =
    baseDir.listFiles().orEmpty().filter { it.isDirectory && ID_PATTERN.matches(it.name) }

  private fun installedIn(dir: File): CatalogPack? =
    runCatching { json.decodeFromString<CatalogPack>(File(dir, PACK_JSON).readText()) }
      .getOrNull()
      ?.takeIf { it.id == dir.name && File(dir, DATA_NAME).isFile }

  private fun gunzip(source: File, target: File) = storage {
    GZIPInputStream(source.inputStream()).use { input ->
      target.outputStream().use { output -> input.copyTo(output) }
    }
  }
}
