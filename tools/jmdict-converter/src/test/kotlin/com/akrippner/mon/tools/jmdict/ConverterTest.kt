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
    code: String = "eng",
    languages: String = """["$code"]""",
    commonOnly: Boolean = false,
    dictDate: String = "2026-11-09",
    fileName: String = "jmdict-$code.json",
  ): File =
    File(dir, fileName).also {
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
                "gloss": [{"lang": "$code", "gender": null, "type": null, "text": "to eat"}]
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
    val result = convert(listOf(inputFile()), outDir, "owner/repo")

    assertEquals("jmdict-2026-11-09", result.releaseTag)
    val packResult = result.packs.single()
    assertEquals(1, packResult.entries)
    assertEquals(1, packResult.skipped)
    assertEquals(File(outDir, "jmdict-eng-2026-11-09.db.gz"), packResult.archive)

    val catalog = Json.decodeFromString<Catalog>(result.catalog.readText())
    val pack = catalog.packs.single()
    assertEquals(pack, packResult.pack)
    assertEquals(
      "https://github.com/owner/repo/releases/download/jmdict-2026-11-09/jmdict-eng-2026-11-09.db.gz",
      pack.url,
    )
    assertEquals("jmdict-eng", pack.id)
    assertEquals("JMdict (English)", pack.name)
    assertEquals("ja", pack.sourceLanguage)
    assertEquals("en", pack.glossLanguage)
    assertEquals(JmdictSchema.FORMAT, pack.format)
    assertEquals(JmdictSchema.VERSION, pack.schema)
    assertEquals(sha256(packResult.archive), pack.sha256)
    assertEquals(packResult.archive.length(), pack.sizeBytes)
    assertTrue(pack.attribution.contains("Electronic Dictionary Research and Development Group"))

    val unpacked = GZIPInputStream(packResult.archive.inputStream()).use { it.readBytes() }
    assertEquals(unpacked.size.toLong(), pack.unpackedSizeBytes)
    assertContentEquals("SQLite format 3\u0000".toByteArray(), unpacked.copyOf(16))
  }

  @Test
  fun leavesOnlyTheArchiveAndTheCatalogBehind() {
    convert(listOf(inputFile()), outDir, "owner/repo")

    assertEquals(
      setOf("jmdict-eng-2026-11-09.db.gz", "catalog.json"),
      outDir.listFiles().orEmpty().map { it.name }.toSet(),
    )
  }

  @Test
  fun sameInputGivesTheSameArchive() {
    val input = inputFile()
    val first = convert(listOf(input), File(dir, "a"), "owner/repo").packs.single().pack.sha256
    val second = convert(listOf(input), File(dir, "b"), "owner/repo").packs.single().pack.sha256

    assertEquals(first, second)
  }

  @Test
  fun germanFileGivesAGermanPack() {
    val result = convert(listOf(inputFile(code = "ger")), outDir, "owner/repo")

    val pack = result.packs.single().pack
    assertEquals("jmdict-ger", pack.id)
    assertEquals("JMdict (German)", pack.name)
    assertEquals("ja", pack.sourceLanguage)
    assertEquals("de", pack.glossLanguage)
    assertEquals(File(outDir, "jmdict-ger-2026-11-09.db.gz"), result.packs.single().archive)
  }

  @Test
  fun severalLanguagesShareOneCatalogAndOneRelease() {
    // Listed German first: the catalog is sorted by id, not by input order.
    val result = convert(listOf(inputFile("ger"), inputFile("eng")), outDir, "owner/repo")

    assertEquals("jmdict-2026-11-09", result.releaseTag)
    val packs = Json.decodeFromString<Catalog>(result.catalog.readText()).packs
    assertEquals(listOf("jmdict-eng", "jmdict-ger"), packs.map { it.id })
    assertEquals(listOf("en", "de"), packs.map { it.glossLanguage })
    assertEquals(
      listOf(
        "https://github.com/owner/repo/releases/download/jmdict-2026-11-09/jmdict-eng-2026-11-09.db.gz",
        "https://github.com/owner/repo/releases/download/jmdict-2026-11-09/jmdict-ger-2026-11-09.db.gz",
      ),
      packs.map { it.url },
    )
    assertEquals(
      setOf("jmdict-eng-2026-11-09.db.gz", "jmdict-ger-2026-11-09.db.gz", "catalog.json"),
      outDir.listFiles().orEmpty().map { it.name }.toSet(),
    )
  }

  @Test
  fun rejectsFilesItCannotTurnIntoOnePack() {
    // An unsupported language, a multi-language file (jmdict-all) and the common-only file.
    assertFailsWith<IllegalArgumentException> {
      convert(listOf(inputFile(code = "xxx")), outDir, "owner/repo")
    }
    assertFailsWith<IllegalArgumentException> {
      convert(listOf(inputFile(languages = """["eng","ger"]""")), outDir, "owner/repo")
    }
    assertFailsWith<IllegalArgumentException> {
      convert(listOf(inputFile(commonOnly = true)), outDir, "owner/repo")
    }
  }

  @Test
  fun rejectsInputsThatDoNotBelongInOneCatalog() {
    assertFailsWith<IllegalArgumentException> { convert(emptyList(), outDir, "owner/repo") }
    // The same language twice.
    assertFailsWith<IllegalArgumentException> {
      convert(
        listOf(inputFile(), inputFile(fileName = "jmdict-eng-again.json")),
        outDir,
        "owner/repo",
      )
    }
    // Two languages from different JMdict dates cannot share one release tag.
    assertFailsWith<IllegalArgumentException> {
      convert(
        listOf(inputFile("eng"), inputFile("ger", dictDate = "2026-12-09")),
        outDir,
        "owner/repo",
      )
    }
  }
}
