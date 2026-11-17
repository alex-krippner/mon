package com.akrippner.mon.tools.pack

import com.akrippner.mon.core.dictionary.pack.Catalog
import com.akrippner.mon.core.dictionary.pack.CatalogPack
import com.akrippner.mon.core.dictionary.pack.sha256Hex
import java.io.File
import java.util.zip.GZIPOutputStream
import kotlinx.serialization.json.Json

private const val BUFFER_SIZE = 64 * 1024

/** The app accepts only such pack ids; the id is also the pack's folder name. */
private val ID_PATTERN = Regex("[a-z0-9-]+")

private val VERSION_PATTERN = Regex("[A-Za-z0-9._-]+")

private val catalogJson = Json { prettyPrint = true }

/** One published pack: its catalog entry, the archive to upload, and entry counts. */
internal class PackResult(
  val pack: CatalogPack,
  val archive: File,
  val entries: Int,
  val skipped: Int,
)

/**
 * What one run publishes: the [packs]' archives and the [catalog], as the release [tag].
 *
 * @property kept ids of the previous catalog's packs that were carried over (other sources).
 * @property dropped ids of this source's previous packs that this run did not build, so they are no
 *   longer in the catalog.
 */
internal class Release(
  val tag: String,
  val title: String,
  val packs: List<PackResult>,
  val catalog: File,
  val kept: List<String>,
  val dropped: List<String>,
)

/**
 * A version goes into the release tag, the archive's file name and its URL, and sources use it in
 * the names of the files they build. Check it before building any path from it.
 *
 * @throws IllegalArgumentException if [version] has other characters than letters, digits, '.', '_'
 *   and '-'.
 */
internal fun requireValidVersion(version: String) {
  require(VERSION_PATTERN.matches(version)) {
    "Version '$version' must consist of letters, digits, '.', '_' and '-'"
  }
}

/**
 * Reads the catalog of the previous release. It is strict on purpose: an unknown field fails
 * instead of being dropped. Otherwise a pack of another source would lose that field without an
 * error when [publish] writes the merged catalog.
 *
 * @throws kotlinx.serialization.SerializationException if [text] is not a catalog this version of
 *   the converter fully understands.
 */
internal fun readCatalog(text: String): Catalog = Json.decodeFromString<Catalog>(text)

/**
 * Packages what [source] built: gzips each pack file into [outDir] as `<id>-<version>.<ext>.gz`,
 * describes it as a [CatalogPack] and writes `catalog.json` there. The uncompressed files are
 * deleted. [repo] (`owner/name`) is the GitHub repository whose release will hold the files; the
 * catalog points at the archives there.
 *
 * The app only reads the catalog of the latest release, so the new catalog must list every pack,
 * not just this run's. A source owns the pack ids that start with `<source id>-`: the new catalog
 * is the packs of [previous] that belong to other sources, plus this run's packs. Pass null for
 * [previous] only for the very first release.
 *
 * A pack of [source] that is in [previous] but not in [built] would vanish from the app with this
 * release, for example when one language's input file was forgotten. That stops the run, unless
 * [allowDrop] says it is meant.
 *
 * Everything is checked before the first file is written.
 *
 * @throws IllegalArgumentException if the packs don't belong in one release of [source], or if the
 *   release would drop a pack and [allowDrop] is false.
 */
internal fun publish(
  source: PackSource,
  built: List<BuiltPack>,
  outDir: File,
  repo: String,
  previous: Catalog?,
  allowDrop: Boolean = false,
): Release {
  val prefix = "${source.id}-"
  require(built.isNotEmpty()) { "No packs to publish" }
  val ids = built.map { it.id }
  require(ids.toSet().size == ids.size) { "Two packs have the same id: $ids" }
  ids.forEach { id ->
    require(id.startsWith(prefix) && ID_PATTERN.matches(id)) {
      "Pack id '$id' must start with '$prefix' and consist of a-z, 0-9 and '-'"
    }
  }
  val versions = built.map { it.version }.toSet()
  require(versions.size == 1) {
    "The packs have different versions $versions; one release has one version"
  }
  val version = versions.single()
  requireValidVersion(version)

  val (own, kept) = previous?.packs.orEmpty().partition { it.id.startsWith(prefix) }
  val dropped = own.map { it.id }.filterNot { it in ids }.distinct().sorted()
  require(dropped.isEmpty() || allowDrop) {
    "The previous catalog has ${source.title} packs that this run does not build: " +
      "${dropped.joinToString()}.\nPublishing this release would remove them from the app. " +
      "Build them in the same run, or allow the removal explicitly (--allow-drop)."
  }

  val tag = "$prefix$version"
  outDir.mkdirs()
  val results = built.map { packageUp(it, outDir, repo, tag) }

  val merged = (kept + results.map { it.pack }).sortedBy { it.id }
  val catalog = File(outDir, "catalog.json")
  catalog.writeText(catalogJson.encodeToString(Catalog(merged)))
  return Release(
    tag = tag,
    title = "${source.title} $version",
    packs = results,
    catalog = catalog,
    kept = kept.map { it.id }.sorted(),
    dropped = dropped,
  )
}

/** Gzips one pack file into [outDir], describes the archive and deletes the uncompressed file. */
private fun packageUp(built: BuiltPack, outDir: File, repo: String, tag: String): PackResult {
  val extension = built.file.extension.let { if (it.isEmpty()) "" else ".$it" }
  val archive = File(outDir, "${built.id}-${built.version}$extension.gz")
  try {
    GZIPOutputStream(archive.outputStream().buffered()).use { out ->
      built.file.inputStream().use { it.copyTo(out, BUFFER_SIZE) }
    }
    val pack =
      CatalogPack(
        id = built.id,
        name = built.name,
        sourceLanguage = built.sourceLanguage,
        glossLanguage = built.glossLanguage,
        format = built.format,
        schema = built.schema,
        version = built.version,
        url = "https://github.com/$repo/releases/download/$tag/${archive.name}",
        sizeBytes = archive.length(),
        unpackedSizeBytes = built.file.length(),
        sha256 = sha256Hex(archive),
        attribution = built.attribution,
      )
    return PackResult(pack, archive, built.entries, built.skipped)
  } finally {
    built.file.delete() // only the compressed archive is published
  }
}
