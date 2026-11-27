package com.akrippner.mon.feature.addreading

import com.akrippner.mon.feature.addreading.file.MAX_FILE_BYTES
import com.akrippner.mon.feature.addreading.file.TextFileResult
import com.akrippner.mon.feature.addreading.file.checkBeforeReading
import com.akrippner.mon.feature.addreading.file.decodeTextFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TextFileDecoderTest {
  private fun rejected(error: AddReadingError) = TextFileResult.Rejected(error)

  @Test
  fun decodesUtf8_andTakesTheTitleFromTheName() {
    val result = decodeTextFile("吾輩は猫である.txt", "吾輩は猫である".encodeToByteArray())

    assertEquals(TextFileResult.Text(title = "吾輩は猫である", text = "吾輩は猫である"), result)
  }

  @Test
  fun stripsAUtf8Bom() {
    val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())

    val result = decodeTextFile("a.txt", bom + "こんにちは".encodeToByteArray())

    assertEquals(TextFileResult.Text(title = "a", text = "こんにちは"), result)
  }

  @Test
  fun keepsTheWholeNameWhenTheExtensionIsInCapitals() {
    val result = decodeTextFile("Chapter 1.TXT", "text".encodeToByteArray())

    assertEquals(TextFileResult.Text(title = "Chapter 1", text = "text"), result)
  }

  @Test
  fun normalisesLineEndings() {
    val result = decodeTextFile("a.txt", "one\r\ntwo\rthree\n".encodeToByteArray())

    assertEquals(TextFileResult.Text(title = "a", text = "one\ntwo\nthree\n"), result)
  }

  @Test
  fun invalidUtf8_isUnreadable() {
    // Shift_JIS for こんにちは is not valid UTF-8.
    val shiftJis =
      byteArrayOf(
        0x82.toByte(),
        0xB1.toByte(),
        0x82.toByte(),
        0xF1.toByte(),
        0x82.toByte(),
        0xC9.toByte(),
      )

    assertEquals(rejected(AddReadingError.FileUnreadable), decodeTextFile("a.txt", shiftJis))
  }

  @Test
  fun blankFile_isEmpty() {
    assertEquals(rejected(AddReadingError.FileEmpty), decodeTextFile("a.txt", byteArrayOf()))
    assertEquals(
      rejected(AddReadingError.FileEmpty),
      decodeTextFile("a.txt", " \n\t".encodeToByteArray()),
    )
  }

  @Test
  fun tooManyBytes_isTooLarge_evenWhenTheSizeWasUnknown() {
    val bytes = ByteArray((MAX_FILE_BYTES + 1).toInt()) { 'a'.code.toByte() }

    assertEquals(rejected(AddReadingError.FileTooLarge), decodeTextFile("a.txt", bytes))
  }

  @Test
  fun exactlyTheLimit_isAccepted() {
    val bytes = ByteArray(MAX_FILE_BYTES.toInt()) { 'a'.code.toByte() }

    assertEquals(TextFileResult.Text::class, decodeTextFile("a.txt", bytes)::class)
  }

  @Test
  fun checkBeforeReading_refusesOtherExtensions_andLargeFiles() {
    assertEquals(AddReadingError.FileNotText, checkBeforeReading("a.md", size = 10))
    assertEquals(AddReadingError.FileNotText, checkBeforeReading("txt", size = 10))
    assertEquals(AddReadingError.FileTooLarge, checkBeforeReading("a.txt", MAX_FILE_BYTES + 1))
    assertNull(checkBeforeReading("a.TXT", size = MAX_FILE_BYTES))
    assertNull(checkBeforeReading("a.txt", size = null))
  }
}
