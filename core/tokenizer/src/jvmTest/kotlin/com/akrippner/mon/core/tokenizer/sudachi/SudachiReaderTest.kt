package com.akrippner.mon.core.tokenizer.sudachi

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.packs.PackClosedException
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.runTest

class SudachiReaderTest {
  // The file does not exist: these tests must never load a dictionary.
  private fun reader() = SudachiReader(File("does-not-exist"), sudachiPack())

  @Test
  fun describesItselfFromThePack() {
    val reader = reader()

    assertEquals(Language.JAPANESE, reader.language)
    assertEquals("sudachi-core@20250825", reader.id)
  }

  @Test
  fun aClosedReaderThrowsInsteadOfReopeningTheFile() = runTest {
    val reader = reader()
    reader.close()

    assertFailsWith<PackClosedException> { reader.tokenize("私は学生です") }
  }

  @Test
  fun closingTwiceIsHarmless() = runTest {
    val reader = reader()
    reader.close()
    reader.close()
  }
}
