package com.akrippner.mon.tools.jmdict

import com.akrippner.mon.core.dictionary.jmdict.JmdictSchema
import com.akrippner.mon.core.dictionary.jmdict.JmdictWriter
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.tools.pack.BuiltPack
import com.akrippner.mon.tools.pack.PackSource
import com.akrippner.mon.tools.pack.requireValidVersion
import java.io.File
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/** The standard acknowledgement the EDRDG licence asks for. Shown on the Dictionaries screen. */
internal const val ATTRIBUTION =
  "This app uses the JMdict dictionary files. These files are the property of the Electronic " +
    "Dictionary Research and Development Group, and are used in conformance with the Group's " +
    "licence (https://www.edrdg.org/edrdg/licence.html), CC BY-SA 4.0. " +
    "Converted with jmdict-simplified (https://github.com/scriptin/jmdict-simplified)."

/** JMdict's headwords are Japanese. Given to the writer and to the catalog, so they agree. */
private val SOURCE_LANGUAGE = Language.JAPANESE

private val inputJson = Json { ignoreUnknownKeys = true }

/**
 * JMdict, from jmdict-simplified JSON files: one file per gloss language, each becoming one pack
 * `jmdict-<code>`. All files of a run must come from the same JMdict date, because they share one
 * release.
 */
internal object JmdictSource : PackSource {
  override val id = "jmdict"
  override val title = "JMdict"
  override val inputHelp =
    """
    full single-language files from https://github.com/scriptin/jmdict-simplified/releases
    (unpack the .json.tgz or .json.zip first; not the 'common' or 'all' variants).
    Supported: ${GlossLanguage.entries.joinToString(", ") { "jmdict-${it.code}-X.json" }}.
    All files must come from the same release, and every language must be converted in one
    run: a run replaces all JMdict packs in the catalog.
    """
      .trimIndent()

  override fun build(inputs: List<File>, workDir: File): List<BuiltPack> {
    workDir.mkdirs()
    val seen = mutableSetOf<GlossLanguage>()
    return inputs.map { input ->
      buildPack(input, workDir) { language ->
        // Checked before the file is written: the second one would overwrite the first.
        require(seen.add(language)) { "Two input files have the same language: ${language.code}" }
      }
    }
  }

  /**
   * Converts one jmdict-simplified file into `jmdict-<code>-<dictDate>.db` in [workDir]. The gloss
   * language comes from the file itself; [check] sees it before anything is written.
   */
  @OptIn(ExperimentalSerializationApi::class)
  private fun buildPack(input: File, workDir: File, check: (GlossLanguage) -> Unit): BuiltPack {
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
    // The date comes from the file and becomes part of a file name: checked before any path is
    // built from it, so a value like "../x" can't reach outside [workDir].
    val version = dictionary.dictDate
    requireValidVersion(version)
    check(language)

    val database = File(workDir, "${language.packId}-$version.db").also { it.delete() }
    var entries = 0
    var skipped = 0
    JmdictWriter(database.path, SOURCE_LANGUAGE).use { writer ->
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

    return BuiltPack(
      id = language.packId,
      name = "JMdict (${language.displayName})",
      sourceLanguage = SOURCE_LANGUAGE.tag,
      glossLanguage = language.tag,
      format = JmdictSchema.FORMAT,
      schema = JmdictSchema.VERSION,
      version = version,
      attribution = ATTRIBUTION,
      file = database,
      entries = entries,
      skipped = skipped,
    )
  }
}
