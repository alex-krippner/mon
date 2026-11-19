package com.akrippner.mon.tools.sudachi

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.tokenizer.sudachi.SudachiHeader
import com.akrippner.mon.core.tokenizer.sudachi.SudachiSchema
import com.akrippner.mon.core.tokenizer.sudachi.readSudachiHeader
import com.akrippner.mon.tools.pack.BuiltPack
import com.akrippner.mon.tools.pack.PackSource
import com.akrippner.mon.tools.pack.requireValidVersion
import java.io.File
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Names the licence and where its notices are. Shown on the Dictionaries screen. SudachiDict is
 * built on UniDic and NEologd data, whose notices are in the LEGAL file of the SudachiDict
 * repository.
 */
internal const val ATTRIBUTION =
  "Japanese text analysis uses SudachiDict, © Works Applications Co., Ltd., licensed under the " +
    "Apache License 2.0 (https://www.apache.org/licenses/LICENSE-2.0). SudachiDict includes " +
    "data derived from UniDic and NEologd; see " +
    "https://github.com/WorksApplications/SudachiDict/blob/develop/LEGAL for their notices."

internal const val PACK_ID = "sudachi-core"

private val DATE_IN_DESCRIPTION = Regex("""(?<!\d)(20\d{6})(?!\d)""")
private val DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC)

/**
 * The Sudachi system dictionary (`system_core.dic` from a SudachiDict release), which becomes the
 * one pack `sudachi-core`. The file is published as it is; the converter only checks that it is a
 * system dictionary and names the version.
 */
internal object SudachiSource : PackSource {
  override val id = "sudachi"
  override val title = "SudachiDict"
  override val inputHelp =
    """
    one system_core.dic, the 'core' dictionary from https://github.com/WorksApplications/SudachiDict
    (unpack the release zip first; not the 'small' or 'full' ones, the app expects 'core').
    """
      .trimIndent()

  override fun build(inputs: List<File>, workDir: File): List<BuiltPack> {
    require(inputs.size == 1) { "Expected exactly one system_core.dic, got ${inputs.size} files" }
    val input = inputs.single()
    val header =
      requireNotNull(readSudachiHeader(input)) {
        "${input.name} is not a Sudachi system dictionary (wrong header)"
      }
    // The version becomes part of a file name: checked before any path is built from it.
    val version = versionOf(header)
    requireValidVersion(version)

    workDir.mkdirs()
    val file = File(workDir, "$PACK_ID-$version.dic").also { it.delete() }
    input.copyTo(file)
    return listOf(
      BuiltPack(
        id = PACK_ID,
        name = "Sudachi core (Japanese)",
        sourceLanguage = Language.JAPANESE.tag,
        glossLanguage = Language.JAPANESE.tag, // not a word list: repeats its source language
        format = SudachiSchema.FORMAT,
        schema = SudachiSchema.VERSION,
        version = version,
        attribution = ATTRIBUTION,
        file = file,
        entries = 0, // the file is not converted, so there is nothing to count
        skipped = 0,
      )
    )
  }
}

/**
 * The release date of the dictionary: the `yyyyMMdd` date in the header's description (SudachiDict
 * names its builds like `SudachiDict-core 20250825`), or else the day the file was built.
 */
internal fun versionOf(header: SudachiHeader): String =
  DATE_IN_DESCRIPTION.find(header.description)?.value
    ?: DATE_FORMAT.format(Instant.ofEpochSecond(header.createTime))
