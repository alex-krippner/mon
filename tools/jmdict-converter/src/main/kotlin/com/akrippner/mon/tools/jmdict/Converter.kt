package com.akrippner.mon.tools.jmdict

import com.akrippner.mon.core.dictionary.jmdict.JmdictSchema
import com.akrippner.mon.core.dictionary.jmdict.JmdictWriter
import com.akrippner.mon.core.dictionary.pack.Catalog
import com.akrippner.mon.core.dictionary.pack.CatalogPack
import com.akrippner.mon.core.dictionary.pack.sha256Hex
import java.io.File
import java.util.zip.GZIPOutputStream
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

private const val BUFFER_SIZE = 64 * 1024

/** The standard acknowledgement the EDRDG licence asks for. Shown on the Dictionaries screen. */
internal const val ATTRIBUTION =
  "This app uses the JMdict dictionary files. These files are the property of the Electronic " +
    "Dictionary Research and Development Group, and are used in conformance with the Group's " +
    "licence (https://www.edrdg.org/edrdg/licence.html), CC BY-SA 4.0. " +
    "Converted with jmdict-simplified (https://github.com/scriptin/jmdict-simplified)."

/** One converted input file: its catalog entry, the archive to publish, and entry counts. */
internal class PackResult(
  val pack: CatalogPack,
  val archive: File,
  val entries: Int,
  val skipped: Int,
)

internal class ConversionResult(
  val packs: List<PackResult>,
  val catalog: File,
  val releaseTag: String,
)

private val inputJson = Json { ignoreUnknownKeys = true }
private val catalogJson = Json { prettyPrint = true }

private fun releaseTagFor(version: String) = "jmdict-$version"

/**
 * Converts several jmdict-simplified files ([inputs], one gloss language each) into one
 * `jmdict-<code>-<dictDate>.db.gz` per file plus a single `catalog.json` that lists every pack, all
 * in [outDir]. [repo] (`owner/name`) is the GitHub repository whose release will hold the files;
 * the catalog points at the archives there. All inputs must come from the same JMdict date, since
 * they share one release tag.
 *
 * The files are converted one at a time, so only one parsed dictionary is in memory.
 */
internal fun convert(inputs: List<File>, outDir: File, repo: String): ConversionResult {
  require(inputs.isNotEmpty()) { "No input files" }
  val results = inputs.map { convertPack(it, outDir, repo) }
  val packs = results.map { it.pack }

  require(packs.map { it.id }.toSet().size == packs.size) {
    "Two input files have the same language: ${packs.map { it.id }}"
  }
  val versions = packs.map { it.version }.toSet()
  require(versions.size == 1) {
    "The files come from different JMdict dates $versions; use one jmdict-simplified release"
  }

  val catalog = File(outDir, "catalog.json")
  catalog.writeText(catalogJson.encodeToString(Catalog(packs.sortedBy { it.id })))
  return ConversionResult(results, catalog, releaseTagFor(versions.single()))
}

/**
 * Converts one jmdict-simplified file ([input]) into `jmdict-<code>-<dictDate>.db.gz` in [outDir]
 * and describes it as a [CatalogPack]. The gloss language comes from the file itself.
 */
@OptIn(ExperimentalSerializationApi::class)
internal fun convertPack(input: File, outDir: File, repo: String): PackResult {
  val dictionary =
    input.inputStream().buffered().use { inputJson.decodeFromStream<SimplifiedDictionary>(it) }
  val language =
    requireNotNull(dictionary.languages.singleOrNull()?.let(GlossLanguage::fromCode)) {
      "Expected a file with one supported gloss language " +
        "(${GlossLanguage.entries.joinToString { it.code }}), got ${dictionary.languages}"
    }
  require(!dictionary.commonOnly) {
    "That is the 'common words only' file; use the full jmdict-${language.code}-*.json"
  }

  val version = dictionary.dictDate
  val releaseTag = releaseTagFor(version)
  val archiveName = "${language.packId}-$version.db.gz"
  outDir.mkdirs()

  val database = File(outDir, "${language.packId}-$version.db").also { it.delete() }
  var entries = 0
  var skipped = 0
  try {
    JmdictWriter(database.path).use { writer ->
      writer.putMeta("version", version)
      writer.putMeta("dictDate", dictionary.dictDate)
      writer.putMeta("sourceVersion", dictionary.version)
      for (word in dictionary.words) {
        val record = word.toRecord(dictionary.tags)
        if (record == null) {
          skipped++
        } else {
          writer.insert(record)
          entries++
        }
      }
      writer.finish()
    }

    val archive = File(outDir, archiveName)
    GZIPOutputStream(archive.outputStream().buffered()).use { out ->
      database.inputStream().use { it.copyTo(out, BUFFER_SIZE) }
    }

    val pack =
      CatalogPack(
        id = language.packId,
        name = "JMdict (${language.displayName})",
        sourceLanguage = "ja",
        glossLanguage = language.tag,
        format = JmdictSchema.FORMAT,
        schema = JmdictSchema.VERSION,
        version = version,
        url = "https://github.com/$repo/releases/download/$releaseTag/$archiveName",
        sizeBytes = archive.length(),
        unpackedSizeBytes = database.length(),
        sha256 = sha256Hex(archive),
        attribution = ATTRIBUTION,
      )
    return PackResult(pack, archive, entries, skipped)
  } finally {
    database.delete() // only the compressed archive is published
  }
}
