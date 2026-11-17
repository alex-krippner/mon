package com.akrippner.mon.tools.jmdict

import com.akrippner.mon.core.dictionary.jmdict.JmdictSchema
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.tools.pack.publish
import com.akrippner.mon.tools.pack.readCatalog
import java.io.File
import java.nio.file.Files
import java.util.zip.GZIPInputStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

private val SQLITE_HEADER = "SQLite format 3\u0000".toByteArray()

class JmdictSourceTest {
  private val dir = Files.createTempDirectory("jmdict-source-test").toFile()
  private val workDir = File(dir, "work")
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

  private fun filesIn(directory: File) = directory.listFiles().orEmpty().map { it.name }.toSet()

  private fun gunzip(archive: File): ByteArray =
    GZIPInputStream(archive.inputStream()).use { it.readBytes() }

  // ---- build: what the source alone decides ----

  @Test
  fun buildsOnePackFromAnEnglishFile() {
    val pack = JmdictSource.build(listOf(inputFile()), workDir).single()

    assertEquals("jmdict-eng", pack.id)
    assertEquals("JMdict (English)", pack.name)
    assertEquals("ja", pack.sourceLanguage)
    assertEquals("en", pack.glossLanguage)
    assertEquals(JmdictSchema.FORMAT, pack.format)
    assertEquals(JmdictSchema.VERSION, pack.schema)
    assertEquals("2026-11-09", pack.version)
    assertTrue(pack.attribution.contains("Electronic Dictionary Research and Development Group"))
    // The word without spelling and gloss has nothing to show.
    assertEquals(1, pack.entries)
    assertEquals(1, pack.skipped)

    assertEquals(File(workDir, "jmdict-eng-2026-11-09.db"), pack.file)
    assertEquals(setOf("jmdict-eng-2026-11-09.db"), filesIn(workDir))
    assertContentEquals(SQLITE_HEADER, pack.file.readBytes().copyOf(SQLITE_HEADER.size))
  }

  @Test
  fun germanFileGivesAGermanPack() {
    val pack = JmdictSource.build(listOf(inputFile(code = "ger")), workDir).single()

    assertEquals("jmdict-ger", pack.id)
    assertEquals("JMdict (German)", pack.name)
    assertEquals("ja", pack.sourceLanguage)
    assertEquals("de", pack.glossLanguage)
    assertEquals(File(workDir, "jmdict-ger-2026-11-09.db"), pack.file)
  }

  @Test
  fun everyGlossLanguageGivesAPackTheAppOffers() {
    val packs = JmdictSource.build(GlossLanguage.entries.map { inputFile(it.code) }, workDir)

    // One pack per file, in input order.
    assertEquals(GlossLanguage.entries.map { it.packId }, packs.map { it.id })
    for (pack in packs) {
      // The source owns exactly the ids with its prefix; publish() rejects any other.
      assertTrue(pack.id.startsWith("${JmdictSource.id}-"), pack.id)
      // The app hides a pack whose languages are not in its Language enum.
      assertNotNull(Language.fromTagOrNull(pack.sourceLanguage), pack.sourceLanguage)
      assertNotNull(Language.fromTagOrNull(pack.glossLanguage), pack.glossLanguage)
    }
  }

  @Test
  fun rejectsFilesItCannotTurnIntoOnePack() {
    // An unsupported language, a multi-language file (jmdict-all) and the common-only file.
    assertFailsWith<IllegalArgumentException> {
      JmdictSource.build(listOf(inputFile(code = "xxx")), workDir)
    }
    assertFailsWith<IllegalArgumentException> {
      JmdictSource.build(listOf(inputFile(languages = """["eng","ger"]""")), workDir)
    }
    assertFailsWith<IllegalArgumentException> {
      JmdictSource.build(listOf(inputFile(commonOnly = true)), workDir)
    }
    // Each one was rejected before anything was written.
    assertTrue(filesIn(workDir).isEmpty())
  }

  @Test
  fun rejectsTheSameLanguageTwice() {
    val error =
      assertFailsWith<IllegalArgumentException> {
        JmdictSource.build(
          listOf(inputFile(), inputFile(fileName = "jmdict-eng-again.json")),
          workDir,
        )
      }
    assertTrue(error.message.orEmpty().contains("same language"), error.message)
  }

  @Test
  fun rejectsADateThatDoesNotFitIntoAFileName() {
    // The date becomes part of the pack file's name, so it is checked before any path is built.
    for (dictDate in listOf("../../escaped", "2026/11/09", "", "2026 11 09")) {
      assertFailsWith<IllegalArgumentException>(dictDate) {
        JmdictSource.build(listOf(inputFile(dictDate = dictDate)), workDir)
      }
    }
    assertTrue(filesIn(workDir).isEmpty())
    // Nothing was written next to the work directory either: only the input file is there.
    assertEquals(setOf("jmdict-eng.json", "work"), filesIn(dir))
  }

  @Test
  fun noInputBuildsNothing() {
    assertTrue(JmdictSource.build(emptyList(), workDir).isEmpty())
  }

  // ---- build + publish: the whole run, as the command line does it ----

  @Test
  fun publishesOneReleaseForSeveralLanguages() {
    // Listed German first: the catalog is sorted by id, not by input order.
    val built = JmdictSource.build(listOf(inputFile("ger"), inputFile("eng")), workDir)
    val release = publish(JmdictSource, built, outDir, "owner/repo", previous = null)

    assertEquals("jmdict-2026-11-09", release.tag)
    assertEquals("JMdict 2026-11-09", release.title)
    val packs = readCatalog(release.catalog.readText()).packs
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
      filesIn(outDir),
    )
    assertTrue(filesIn(workDir).isEmpty())

    for (result in release.packs) {
      val unpacked = gunzip(result.archive)
      assertEquals(unpacked.size.toLong(), result.pack.unpackedSizeBytes)
      assertContentEquals(SQLITE_HEADER, unpacked.copyOf(SQLITE_HEADER.size))
    }
  }

  @Test
  fun sameInputGivesTheSameArchive() {
    // The published sha256 identifies a pack's content, so writer and gzip must be deterministic.
    val input = inputFile()
    fun sha256Of(name: String): String {
      val built = JmdictSource.build(listOf(input), File(dir, "work-$name"))
      return publish(JmdictSource, built, File(dir, "out-$name"), "owner/repo", null)
        .packs
        .single()
        .pack
        .sha256
    }

    assertEquals(sha256Of("a"), sha256Of("b"))
  }

  @Test
  fun filesFromDifferentJmdictDatesCannotShareARelease() {
    val built =
      JmdictSource.build(
        listOf(inputFile("eng"), inputFile("ger", dictDate = "2026-12-09")),
        workDir,
      )

    assertFailsWith<IllegalArgumentException> {
      publish(JmdictSource, built, outDir, "owner/repo", previous = null)
    }
  }

  @Test
  fun forgottenLanguageStopsTheRelease() {
    // The published catalog has English and German.
    val both = JmdictSource.build(listOf(inputFile("eng"), inputFile("ger")), workDir)
    val published = publish(JmdictSource, both, File(dir, "published"), "owner/repo", null)
    val previous = readCatalog(published.catalog.readText())

    // The next update is run with the English file only.
    val onlyEnglish = JmdictSource.build(listOf(inputFile("eng", dictDate = "2026-12-09")), workDir)
    val error =
      assertFailsWith<IllegalArgumentException> {
        publish(JmdictSource, onlyEnglish, outDir, "owner/repo", previous)
      }

    assertTrue(error.message.orEmpty().contains("jmdict-ger"), error.message)
    assertTrue(filesIn(outDir).isEmpty())
  }
}
