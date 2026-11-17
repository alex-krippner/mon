package com.akrippner.mon.tools.pack

import com.akrippner.mon.core.dictionary.pack.Catalog
import com.akrippner.mon.core.dictionary.pack.CatalogPack
import java.io.File
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * A source that is not JMdict and not SQLite: each input text file becomes one pack `<id>-<file
 * name>` with the same content. It shows that packaging and the catalog work for any source.
 */
private class FakeSource(
  override val id: String = "other",
  override val title: String = "Other",
  private val version: String = "2026.1",
) : PackSource {
  override val inputHelp = "any text files"

  override fun build(inputs: List<File>, workDir: File): List<BuiltPack> =
    inputs.map { input ->
      builtPack(workDir, "$id-${input.nameWithoutExtension}", version, input.readText())
    }
}

/** A pack as a source hands it to [publish]: [content] in a file in [workDir]. */
private fun builtPack(
  workDir: File,
  id: String,
  version: String = "2026.1",
  content: String = "content of $id",
  fileName: String = "$id.txt",
): BuiltPack {
  workDir.mkdirs()
  val file = File(workDir, fileName).also { it.writeText(content) }
  return BuiltPack(
    id = id,
    name = "Name of $id",
    sourceLanguage = "ja",
    glossLanguage = "de",
    format = "notes-text",
    schema = 3,
    version = version,
    attribution = "Attribution of $id",
    file = file,
    entries = 7,
    skipped = 2,
  )
}

/** A pack as an earlier release's catalog lists it. */
private fun catalogPack(id: String, version: String = "old") =
  CatalogPack(
    id = id,
    name = "Previous $id",
    sourceLanguage = "ja",
    glossLanguage = "en",
    format = "jmdict-sqlite",
    schema = 1,
    version = version,
    url = "https://github.com/owner/repo/releases/download/earlier/$id-$version.db.gz",
    sizeBytes = 10,
    unpackedSizeBytes = 20,
    sha256 = "sha-of-$id",
    attribution = "Previous attribution",
  )

class PublishTest {
  private val dir = Files.createTempDirectory("publish-test").toFile()
  private val workDir = File(dir, "work")
  private val outDir = File(dir, "out")
  private val source = FakeSource()

  @AfterTest
  fun cleanUp() {
    dir.deleteRecursively()
  }

  private fun input(name: String, content: String = "content of $name"): File =
    File(dir, "$name.txt").also { it.writeText(content) }

  private fun pack(id: String, version: String = "2026.1") = builtPack(workDir, id, version)

  private fun filesIn(directory: File) = directory.listFiles().orEmpty().map { it.name }.toSet()

  private fun sha256(file: File) =
    MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") {
      "%02x".format(it)
    }

  private fun catalogIn(release: Release) = readCatalog(release.catalog.readText()).packs

  // ---- packaging ----

  @Test
  fun archiveAndCatalogDescribeEachOther() {
    val built = source.build(listOf(input("x", content = "first line\nsecond line\n")), workDir)
    val release = publish(source, built, outDir, "owner/repo", previous = null)

    assertEquals("other-2026.1", release.tag)
    assertEquals("Other 2026.1", release.title)
    assertEquals(File(outDir, "catalog.json"), release.catalog)

    val result = release.packs.single()
    assertEquals(File(outDir, "other-x-2026.1.txt.gz"), result.archive)
    assertEquals(7, result.entries)
    assertEquals(2, result.skipped)

    val pack = catalogIn(release).single()
    assertEquals(result.pack, pack)
    // What the source said is carried over unchanged ...
    assertEquals("other-x", pack.id)
    assertEquals("Name of other-x", pack.name)
    assertEquals("ja", pack.sourceLanguage)
    assertEquals("de", pack.glossLanguage)
    assertEquals("notes-text", pack.format)
    assertEquals(3, pack.schema)
    assertEquals("2026.1", pack.version)
    assertEquals("Attribution of other-x", pack.attribution)
    // ... and what describes the archive is measured.
    assertEquals(
      "https://github.com/owner/repo/releases/download/other-2026.1/other-x-2026.1.txt.gz",
      pack.url,
    )
    assertEquals(sha256(result.archive), pack.sha256)
    assertEquals(result.archive.length(), pack.sizeBytes)
    val unpacked = GZIPInputStream(result.archive.inputStream()).use { it.readBytes() }
    assertEquals("first line\nsecond line\n", unpacked.decodeToString())
    assertEquals(unpacked.size.toLong(), pack.unpackedSizeBytes)
  }

  @Test
  fun leavesOnlyTheArchivesAndTheCatalogBehind() {
    val built = source.build(listOf(input("x"), input("y")), workDir)
    publish(source, built, outDir, "owner/repo", previous = null)

    assertEquals(
      setOf("other-x-2026.1.txt.gz", "other-y-2026.1.txt.gz", "catalog.json"),
      filesIn(outDir),
    )
    // The uncompressed files are not published, so they are gone.
    assertTrue(filesIn(workDir).isEmpty())
  }

  @Test
  fun sameFileGivesTheSameArchive() {
    fun sha256Of(name: String): String {
      val built = listOf(builtPack(File(dir, "work-$name"), "other-x", content = "same content"))
      return publish(source, built, File(dir, "out-$name"), "owner/repo", null)
        .packs
        .single()
        .pack
        .sha256
    }

    assertEquals(sha256Of("a"), sha256Of("b"))
  }

  @Test
  fun archiveIsNamedAfterThePackFilesExtension() {
    val built =
      listOf(
        builtPack(workDir, "other-db", fileName = "anything.db"),
        builtPack(workDir, "other-plain", fileName = "no-extension"),
      )
    val release = publish(source, built, outDir, "owner/repo", previous = null)

    assertEquals(
      listOf("other-db-2026.1.db.gz", "other-plain-2026.1.gz"),
      release.packs.map { it.archive.name },
    )
    assertEquals(
      listOf("other-db-2026.1.db.gz", "other-plain-2026.1.gz"),
      catalogIn(release).map { it.url.substringAfterLast('/') },
    )
  }

  // ---- what may not be published ----

  @Test
  fun rejectsPacksThatDoNotBelongInOneRelease() {
    fun assertRejected(vararg built: BuiltPack) {
      assertFailsWith<IllegalArgumentException>(built.joinToString { "${it.id}@${it.version}" }) {
        publish(source, built.toList(), outDir, "owner/repo", previous = null)
      }
    }

    assertRejected() // nothing to publish
    assertRejected(pack("other-x"), pack("other-x")) // the same id twice
    assertRejected(pack("other-x", version = "1"), pack("other-y", version = "2"))

    // Rejected before anything is written: a failed run leaves no half-made release behind.
    assertTrue(filesIn(outDir).isEmpty())
  }

  @Test
  fun rejectsPackIdsOutsideTheSourcesPrefix() {
    // Another source's id, the bare source id, a prefix without the dash, and characters the app
    // does not accept in an id (it is the pack's folder name).
    for (id in listOf("jmdict-eng", "other", "otherwise-x", "other-X", "other-x y", "other-ä")) {
      assertFailsWith<IllegalArgumentException>(id) {
        publish(source, listOf(pack(id)), outDir, "owner/repo", previous = null)
      }
    }
    assertTrue(filesIn(outDir).isEmpty())
  }

  @Test
  fun rejectsVersionsThatDoNotFitIntoATagAndAUrl() {
    for (version in listOf("", "1 2", "1/2", "v1'", "2026:11", "ä")) {
      assertFailsWith<IllegalArgumentException>(version) {
        publish(source, listOf(pack("other-x", version)), outDir, "owner/repo", previous = null)
      }
    }
    assertTrue(filesIn(outDir).isEmpty())

    // Dates, dotted versions and underscores are fine.
    for (version in listOf("2026-11-09", "3.6.2", "v1_rc2")) {
      val release =
        publish(source, listOf(pack("other-x", version)), File(dir, version), "owner/repo", null)
      assertEquals("other-$version", release.tag)
    }
  }

  // ---- the catalog of the latest release must list every pack ----

  @Test
  fun withoutAPreviousCatalogTheCatalogListsOnlyThisRun() {
    val release = publish(source, listOf(pack("other-x")), outDir, "owner/repo", previous = null)

    assertEquals(listOf("other-x"), catalogIn(release).map { it.id })
  }

  @Test
  fun mergeKeepsOtherSourcesAndReplacesItsOwnPacks() {
    val jmdictEng = catalogPack("jmdict-eng")
    val zeta = catalogPack("zeta-1")
    val previous = Catalog(listOf(zeta, catalogPack("other-x"), jmdictEng))

    val release = publish(source, listOf(pack("other-x")), outDir, "owner/repo", previous)

    val packs = catalogIn(release)
    assertEquals(listOf("jmdict-eng", "other-x", "zeta-1"), packs.map { it.id }) // sorted by id
    assertEquals(listOf("jmdict-eng", "zeta-1"), release.kept)
    assertTrue(release.dropped.isEmpty())
    // The other sources' packs are untouched, down to the URL in their own earlier release.
    assertEquals(jmdictEng, packs[0])
    assertEquals(zeta, packs[2])
    // Its own pack is this run's, not the previous one.
    assertEquals(release.packs.single().pack, packs[1])
    assertEquals("2026.1", packs[1].version)
    // Only this run's packs are uploaded.
    assertEquals(listOf("other-x"), release.packs.map { it.pack.id })
    assertEquals(setOf("other-x-2026.1.txt.gz", "catalog.json"), filesIn(outDir))
  }

  @Test
  fun sourceOwnsOnlyIdsWithItsWholePrefix() {
    // "other2-x" and "otherwise-y" start with "other" but belong to other sources.
    val previous = Catalog(listOf(catalogPack("other2-x"), catalogPack("otherwise-y")))

    val release = publish(source, listOf(pack("other-x")), outDir, "owner/repo", previous)

    assertEquals(listOf("other-x", "other2-x", "otherwise-y"), catalogIn(release).map { it.id })
  }

  @Test
  fun repeatedRunOfTheSameSourceReplacesAllItsPacks() {
    val first =
      publish(source, listOf(pack("other-x"), pack("other-y")), outDir, "owner/repo", null)
    val previous = Catalog(catalogIn(first) + catalogPack("jmdict-eng"))

    val rebuilt = listOf(pack("other-y", "2026.2"), pack("other-x", "2026.2"))
    val second = publish(source, rebuilt, File(dir, "out2"), "owner/repo", previous)

    val packs = catalogIn(second)
    assertEquals(listOf("jmdict-eng", "other-x", "other-y"), packs.map { it.id })
    assertEquals(listOf("old", "2026.2", "2026.2"), packs.map { it.version })
    assertTrue(second.dropped.isEmpty())
  }

  @Test
  fun packMissingFromThisRunStopsTheRelease() {
    // "other-old" is in the published catalog but not in this run, e.g. its input file was
    // forgotten. Publishing would silently remove it from the app.
    val previous =
      Catalog(listOf(catalogPack("other-x"), catalogPack("other-old"), catalogPack("jmdict-eng")))

    val error =
      assertFailsWith<IllegalArgumentException> {
        publish(source, listOf(pack("other-x")), outDir, "owner/repo", previous)
      }

    assertTrue(error.message.orEmpty().contains("other-old"), error.message)
    // Other sources' packs and its own rebuilt pack are not what is missing.
    assertFalse(error.message.orEmpty().contains("jmdict-eng"), error.message)
    assertFalse(error.message.orEmpty().contains("other-x"), error.message)
    // Stopped before anything was written.
    assertTrue(filesIn(outDir).isEmpty())
  }

  @Test
  fun allowDropRemovesAPackOnPurpose() {
    val jmdictEng = catalogPack("jmdict-eng")
    val previous = Catalog(listOf(catalogPack("other-x"), catalogPack("other-old"), jmdictEng))

    val release =
      publish(source, listOf(pack("other-x")), outDir, "owner/repo", previous, allowDrop = true)

    assertEquals(listOf("jmdict-eng", "other-x"), catalogIn(release).map { it.id })
    assertEquals(listOf("other-old"), release.dropped)
    assertEquals(listOf("jmdict-eng"), release.kept)
  }

  @Test
  fun withoutAPreviousCatalogNothingCanBeDropped() {
    val release = publish(source, listOf(pack("other-x")), outDir, "owner/repo", previous = null)

    assertTrue(release.dropped.isEmpty())
    assertTrue(release.kept.isEmpty())
  }

  // ---- reading the previous catalog ----

  @Test
  fun readsTheCatalogItWrote() {
    val release = publish(source, listOf(pack("other-x")), outDir, "owner/repo", previous = null)

    assertEquals(release.packs.map { it.pack }, readCatalog(release.catalog.readText()).packs)
  }

  @Test
  fun previousCatalogWithAnUnknownFieldIsRejectedNotTrimmed() {
    val known = Json.encodeToString(Catalog(listOf(catalogPack("jmdict-eng"))))
    assertEquals(listOf("jmdict-eng"), readCatalog(known).packs.map { it.id })

    // Written by a newer converter. Reading it leniently would drop the field from the merged
    // catalog without anyone noticing.
    val newer = known.replace("\"attribution\":", "\"fromTheFuture\":true,\"attribution\":")
    assertFalse(newer == known)
    assertFailsWith<SerializationException> { readCatalog(newer) }
  }

  @Test
  fun previousCatalogThatIsNotACatalogIsRejected() {
    assertFailsWith<SerializationException> { readCatalog("<html>Not Found</html>") }
    assertFailsWith<SerializationException> { readCatalog("{}") }
    // A pack with a missing field.
    assertFailsWith<SerializationException> { readCatalog("""{"packs":[{"id":"jmdict-eng"}]}""") }
  }

  // ---- source ids ----

  @Test
  fun sourceIdsMustNotOverlap() {
    requireSeparateIds(listOf(FakeSource("jmdict"), FakeSource("other"), FakeSource("jmnedict")))
    requireSeparateIds(emptyList())

    // The same id twice, and an id that starts with another one plus "-": a run of "jmdict" would
    // drop the packs of "jmdict-names".
    assertFailsWith<IllegalArgumentException> {
      requireSeparateIds(listOf(FakeSource("jmdict"), FakeSource("jmdict")))
    }
    assertFailsWith<IllegalArgumentException> {
      requireSeparateIds(listOf(FakeSource("jmdict"), FakeSource("jmdict-names")))
    }
    assertFailsWith<IllegalArgumentException> {
      requireSeparateIds(listOf(FakeSource("jmdict-names"), FakeSource("jmdict")))
    }
  }

  @Test
  fun sourceIdMustBeUsableAsAPackIdPrefix() {
    for (id in listOf("", "JMdict", "jm dict", "-jmdict", "jmdict-", "jm--dict")) {
      assertFailsWith<IllegalArgumentException>(id) { requireSeparateIds(listOf(FakeSource(id))) }
    }
  }
}
