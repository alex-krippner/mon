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

internal const val PACK_ID = "jmdict-eng"
private const val BUFFER_SIZE = 64 * 1024

/** The standard acknowledgement the EDRDG licence asks for. Shown on the Dictionaries screen. */
internal const val ATTRIBUTION =
  "This app uses the JMdict dictionary files. These files are the property of the Electronic " +
    "Dictionary Research and Development Group, and are used in conformance with the Group's " +
    "licence (https://www.edrdg.org/edrdg/licence.html), CC BY-SA 4.0. " +
    "Converted with jmdict-simplified (https://github.com/scriptin/jmdict-simplified)."

internal class ConversionResult(
  val pack: CatalogPack,
  val archive: File,
  val catalog: File,
  val releaseTag: String,
  val entries: Int,
  val skipped: Int,
)

private val inputJson = Json { ignoreUnknownKeys = true }
private val catalogJson = Json { prettyPrint = true }

/**
 * Converts a jmdict-simplified English JSON file ([input]) into `jmdict-eng-<dictDate>.db.gz` and
 * `catalog.json` in [outDir]. [repo] (`owner/name`) is the GitHub repository whose release will
 * hold both files; the catalog points at the archive there.
 */
@OptIn(ExperimentalSerializationApi::class)
internal fun convert(input: File, outDir: File, repo: String): ConversionResult {
  val dictionary =
    input.inputStream().buffered().use { inputJson.decodeFromStream<SimplifiedDictionary>(it) }
  require(dictionary.languages == listOf("eng")) {
    "Expected the English-only file (jmdict-eng-*.json), got languages ${dictionary.languages}"
  }
  require(!dictionary.commonOnly) {
    "That is the 'common words only' file; use the full jmdict-eng-*.json"
  }

  val version = dictionary.dictDate
  val releaseTag = "jmdict-$version"
  val archiveName = "$PACK_ID-$version.db.gz"
  outDir.mkdirs()

  val database = File(outDir, "$PACK_ID-$version.db").also { it.delete() }
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
        id = PACK_ID,
        name = "JMdict (English)",
        sourceLanguage = "ja",
        glossLanguage = "en",
        format = JmdictSchema.FORMAT,
        schema = JmdictSchema.VERSION,
        version = version,
        url = "https://github.com/$repo/releases/download/$releaseTag/$archiveName",
        sizeBytes = archive.length(),
        unpackedSizeBytes = database.length(),
        sha256 = sha256Hex(archive),
        attribution = ATTRIBUTION,
      )
    val catalog = File(outDir, "catalog.json")
    catalog.writeText(catalogJson.encodeToString(Catalog(listOf(pack))))
    return ConversionResult(pack, archive, catalog, releaseTag, entries, skipped)
  } finally {
    database.delete() // only the compressed archive is published
  }
}
