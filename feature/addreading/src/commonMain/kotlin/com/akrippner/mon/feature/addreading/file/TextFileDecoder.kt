package com.akrippner.mon.feature.addreading.file

import com.akrippner.mon.feature.addreading.AddReadingError

/** Files above this many bytes are refused, so a wrong pick cannot freeze the screen. */
const val MAX_FILE_BYTES: Long = 2L * 1024 * 1024

sealed interface TextFileResult {
  /** [title] is the file name without its extension. */
  data class Text(val title: String, val text: String) : TextFileResult

  data class Rejected(val error: AddReadingError) : TextFileResult
}

/**
 * Checks what can be checked before reading: the extension and, when the platform reports it, the
 * size. Returns the error, or null when the file may be read.
 */
fun checkBeforeReading(name: String, size: Long?): AddReadingError? =
  when {
    !name.endsWith(TXT_EXTENSION, ignoreCase = true) -> AddReadingError.FileNotText
    size != null && size > MAX_FILE_BYTES -> AddReadingError.FileTooLarge
    else -> null
  }

/**
 * Turns the bytes of a `.txt` file into text. Only UTF-8 is read: a file in another encoding is
 * reported as unreadable rather than shown as garbage.
 */
fun decodeTextFile(name: String, bytes: ByteArray): TextFileResult {
  if (bytes.size > MAX_FILE_BYTES) return TextFileResult.Rejected(AddReadingError.FileTooLarge)

  val start = if (bytes.hasUtf8Bom()) UTF8_BOM_SIZE else 0
  val decoded =
    try {
      bytes.decodeToString(startIndex = start, endIndex = bytes.size, throwOnInvalidSequence = true)
    } catch (_: CharacterCodingException) {
      return TextFileResult.Rejected(AddReadingError.FileUnreadable)
    }

  // Windows files use CRLF; the text area and the tokenizer should see plain line feeds.
  val text = decoded.replace("\r\n", "\n").replace('\r', '\n')
  if (text.isBlank()) return TextFileResult.Rejected(AddReadingError.FileEmpty)
  return TextFileResult.Text(title = name.dropLast(TXT_EXTENSION.length).trim(), text = text)
}

private const val TXT_EXTENSION = ".txt"
private const val UTF8_BOM_SIZE = 3

private fun ByteArray.hasUtf8Bom(): Boolean =
  size >= UTF8_BOM_SIZE &&
    this[0] == 0xEF.toByte() &&
    this[1] == 0xBB.toByte() &&
    this[2] == 0xBF.toByte()
