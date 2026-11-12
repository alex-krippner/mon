package com.akrippner.mon.tools.jmdict

import com.akrippner.mon.core.dictionary.jmdict.JmdictSchema
import com.akrippner.mon.core.dictionary.pack.Catalog
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class ConverterTest {
  private val dir = Files.createTempDirectory("converter-test").toFile()
  private val outDir = File(dir, "out")

  @AfterTest
  fun cleanUp() {
    dir.deleteRecursively()
  }

  // A real jmdict-simplified entry has many more fields; the extra ones must be ignored.
  private fun inputFile(
    languages: String = """["eng"]""",
    commonOnly: Boolean = false,
    dictDate: String = "2026-11-09",
  ): File =
    File(dir, "jmdict-eng.json").also {
      it.writeText(
        """
        {
          "version": "3.6.2",
          "languages": $languages,
          "commonOnly": $commonOnly,
          "dictDate": "$dictDate",
          "dictRevisions": ["1.09"],
          "tags": {"v1": "Ichidan verb", "vt": "transitive verb"},
          "words": [
            {
              "id": "1358280",
              "kanji": [{"common": true, "text": "食べる", "tags": []}],
              "kana": [{"common": true, "text": "たべる", "tags": [], "appliesToKanji": ["*"]}],
              "sense": [{
                "partOfSpeech": ["v1", "vt"], "appliesToKanji": ["*"], "appliesToKana": ["*"],
                "related": [], "antonym": [], "field": [], "dialect": [], "misc": [], "info": [],
                "languageSource": [],
                "gloss": [{"lang": "eng", "gender": null, "type": null, "text": "to eat"}]
              }]
            },
            {"id": "99", "kanji": [], "kana": [], "sense": []}
          ]
        }
        """
          .trimIndent()
      )
    }

  private fun sha256(file: File) =
    MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") {
      "%02x".format(it)
    }

  @Test
  fun writesArchiveAndCatalogThatDescribeEachOther() {
    val result = convert(inputFile(), outDir, "owner/repo")

    assertEquals(1, result.entries)
    assertEquals(1, result.skipped)
    assertEquals("jmdict-2026-11-09", result.releaseTag)
    assertEquals(File(outDir, "jmdict-eng-2026-11-09.db.gz"), result.archive)

    val catalog = Json.decodeFromString<Catalog>(result.catalog.readText())
    val pack = catalog.packs.single()
    assertEquals(pack, result.pack)
    assertEquals(
      "https://github.com/owner/repo/releases/download/jmdict-2026-11-09/jmdict-eng-2026-11-09.db.gz",
      pack.url,
    )
    assertEquals("jmdict-eng", pack.id)
    assertEquals("ja", pack.sourceLanguage)
    assertEquals("en", pack.glossLanguage)
    assertEquals(JmdictSchema.FORMAT, pack.format)
    assertEquals(JmdictSchema.VERSION, pack.schema)
    assertEquals(sha256(result.archive), pack.sha256)
    assertEquals(result.archive.length(), pack.sizeBytes)
    assertTrue(pack.attribution.contains("Electronic Dictionary Research and Development Group"))

    val unpacked = GZIPInputStream(result.archive.inputStream()).use { it.readBytes() }
    assertEquals(unpacked.size.toLong(), pack.unpackedSizeBytes)
    assertContentEquals("SQLite format 3\u0000".toByteArray(), unpacked.copyOf(16))
  }

  @Test
  fun leavesOnlyTheArchiveAndTheCatalogBehind() {
    convert(inputFile(), outDir, "owner/repo")

    assertEquals(
      setOf("jmdict-eng-2026-11-09.db.gz", "catalog.json"),
      outDir.listFiles().orEmpty().map { it.name }.toSet(),
    )
  }

  @Test
  fun sameInputGivesTheSameArchive() {
    val first = convert(inputFile(), File(dir, "a"), "owner/repo").pack.sha256
    val second = convert(inputFile(), File(dir, "b"), "owner/repo").pack.sha256

    assertEquals(first, second)
  }

  @Test
  fun rejectsOtherLanguagesAndTheCommonOnlyFile() {
    assertFailsWith<IllegalArgumentException> {
      convert(inputFile(languages = """["ger"]"""), outDir, "owner/repo")
    }
    assertFailsWith<IllegalArgumentException> {
      convert(inputFile(commonOnly = true), outDir, "owner/repo")
    }
  }
}
