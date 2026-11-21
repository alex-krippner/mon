package com.akrippner.mon.core.tokenizer.sudachi

import com.worksap.nlp.sudachi.dictionary.DictionaryVersion
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SudachiFormatTest {
  private val dir = Files.createTempDirectory("sudachi-format").toFile()

  @AfterTest
  fun cleanUp() {
    dir.deleteRecursively()
  }

  private fun file(name: String = "pack.dat") = dir.resolve(name)

  @Test
  fun acceptsASystemDictionaryOfTheDeclaredSize() {
    val dictionary = file().writeSudachiHeader()

    assertTrue(SudachiFormat.verify(dictionary, sudachiPack(dictionary.length())))
  }

  @Test
  fun acceptsBothSystemDictionaryVersions() {
    listOf(DictionaryVersion.SYSTEM_DICT_VERSION_1, DictionaryVersion.SYSTEM_DICT_VERSION_2)
      .forEach { version ->
        val dictionary = file().writeSudachiHeader(version = version)
        assertTrue(SudachiFormat.verify(dictionary, sudachiPack(dictionary.length())))
      }
  }

  @Test
  fun rejectsAUserDictionary() {
    val dictionary = file().writeSudachiHeader(version = DictionaryVersion.USER_DICT_VERSION_3)

    assertFalse(SudachiFormat.verify(dictionary, sudachiPack(dictionary.length())))
  }

  @Test
  fun rejectsAFileThatIsNotADictionary() {
    val text = file().apply { writeText("not a dictionary ".repeat(30)) }

    assertFalse(SudachiFormat.verify(text, sudachiPack(text.length())))
  }

  @Test
  fun rejectsAFileThatIsTooShortForAHeader() {
    val short = file().apply { writeBytes(ByteArray(20)) }

    assertFalse(SudachiFormat.verify(short, sudachiPack(short.length())))
  }

  @Test
  fun rejectsAFileOfTheWrongSize() {
    val dictionary = file().writeSudachiHeader()

    assertFalse(SudachiFormat.verify(dictionary, sudachiPack(dictionary.length() + 1)))
  }

  @Test
  fun readsTheHeader() {
    val header =
      readSudachiHeader(
        file().writeSudachiHeader(createTime = 42, description = "SudachiDict-core 20250825")
      )

    assertNotNull(header)
    assertEquals(42, header.createTime)
    assertEquals("SudachiDict-core 20250825", header.description)
  }

  @Test
  fun noHeaderForAMissingFile() {
    assertNull(readSudachiHeader(file("missing")))
  }

  @Test
  fun openingDoesNotTouchTheFile() {
    // open() must be cheap: the manager opens a reader for every installed pack on every scan.
    val reader = SudachiFormat.open(file("missing"), sudachiPack())

    assertEquals("sudachi-core@20250825", (reader as SudachiReader).id)
  }
}
