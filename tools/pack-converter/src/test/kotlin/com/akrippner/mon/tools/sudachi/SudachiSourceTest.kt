package com.akrippner.mon.tools.sudachi

import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.sha256Hex
import com.akrippner.mon.core.tokenizer.sudachi.SudachiHeader
import com.akrippner.mon.core.tokenizer.sudachi.SudachiSchema
import com.akrippner.mon.tools.pack.publish
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

private const val SYSTEM_DICT_VERSION_2 = 0xce9f011a92394434uL
private const val USER_DICT_VERSION_3 = 0xca9811756ff64fb0uL

class SudachiSourceTest {
  private val dir = Files.createTempDirectory("sudachi-source-test").toFile()
  private val workDir = File(dir, "work")
  private val outDir = File(dir, "out")

  @AfterTest
  fun cleanUp() {
    dir.deleteRecursively()
  }

  /** A file with the header of a Sudachi dictionary and nothing else. */
  private fun dictionary(
    version: ULong = SYSTEM_DICT_VERSION_2,
    createTime: Long = 1_756_080_000, // 2025-08-25
    description: String = "SudachiDict-core 20250825",
    name: String = "system_core.dic",
  ): File {
    val buffer = ByteBuffer.allocate(8 + 8 + 256 + 64).order(ByteOrder.LITTLE_ENDIAN)
    buffer.putLong(version.toLong())
    buffer.putLong(createTime)
    buffer.put(description.toByteArray())
    return File(dir, name).also { it.writeBytes(buffer.array()) }
  }

  @Test
  fun buildsOnePackFromASystemDictionary() {
    val input = dictionary()

    val pack = SudachiSource.build(listOf(input), workDir).single()

    assertEquals("sudachi-core", pack.id)
    assertEquals("20250825", pack.version)
    assertEquals(SudachiSchema.FORMAT, pack.format)
    assertEquals(SudachiSchema.VERSION, pack.schema)
    assertEquals("ja", pack.sourceLanguage)
    assertEquals("ja", pack.glossLanguage)
    assertEquals(File(workDir, "sudachi-core-20250825.dic"), pack.file)
    assertEquals(input.readBytes().toList(), pack.file.readBytes().toList())
    assertTrue(pack.attribution.contains("Apache License"))
  }

  @Test
  fun theVersionFallsBackToTheBuildDateWhenTheDescriptionHasNone() {
    val input = dictionary(description = "my own build", createTime = 1_756_080_000)

    assertEquals("20250825", SudachiSource.build(listOf(input), workDir).single().version)
  }

  @Test
  fun versionOfPrefersTheDateInTheDescription() {
    assertEquals("20240101", versionOf(SudachiHeader(1_756_080_000, "core 20240101 build")))
    assertEquals("20250825", versionOf(SudachiHeader(1_756_080_000, "no date here")))
  }

  @Test
  fun rejectsAUserDictionary() {
    val input = dictionary(version = USER_DICT_VERSION_3)

    assertFailsWith<IllegalArgumentException> { SudachiSource.build(listOf(input), workDir) }
  }

  @Test
  fun rejectsAFileThatIsNotADictionary() {
    val input = File(dir, "other.dic").also { it.writeText("hello") }

    assertFailsWith<IllegalArgumentException> { SudachiSource.build(listOf(input), workDir) }
  }

  @Test
  fun rejectsMoreOrLessThanOneFile() {
    assertFailsWith<IllegalArgumentException> { SudachiSource.build(emptyList(), workDir) }
    assertFailsWith<IllegalArgumentException> {
      SudachiSource.build(listOf(dictionary(), dictionary(name = "second.dic")), workDir)
    }
  }

  @Test
  fun publishesTheDictionaryAsAGzippedPackAndKeepsTheOtherSourcesPacks() {
    val jmdict =
      CatalogPack(
        id = "jmdict-eng",
        name = "JMdict (English)",
        sourceLanguage = "ja",
        glossLanguage = "en",
        format = "jmdict-sqlite",
        schema = 1,
        version = "2026-11-09",
        url = "https://example.test/jmdict-eng.db.gz",
        sizeBytes = 1,
        unpackedSizeBytes = 2,
        sha256 = "abc",
        attribution = "attribution",
      )
    val built = SudachiSource.build(listOf(dictionary()), workDir)

    val release =
      publish(
        SudachiSource,
        built,
        outDir,
        "owner/repo",
        previous = com.akrippner.mon.core.packs.Catalog(listOf(jmdict)),
      )

    assertEquals("sudachi-20250825", release.tag)
    assertEquals(listOf("jmdict-eng"), release.kept)
    val pack = release.packs.single().pack
    assertEquals(
      "https://github.com/owner/repo/releases/download/sudachi-20250825/sudachi-core-20250825.dic.gz",
      pack.url,
    )
    assertEquals(sha256Hex(release.packs.single().archive), pack.sha256)
    assertEquals(336, pack.unpackedSizeBytes) // 272-byte header plus 64 bytes, as in dictionary()
    assertTrue(release.catalog.readText().contains("jmdict-eng"))
  }
}
