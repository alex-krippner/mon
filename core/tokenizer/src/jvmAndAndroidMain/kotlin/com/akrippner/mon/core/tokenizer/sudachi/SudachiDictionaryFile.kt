package com.akrippner.mon.core.tokenizer.sudachi

import com.worksap.nlp.sudachi.dictionary.DictionaryHeader
import com.worksap.nlp.sudachi.dictionary.DictionaryVersion
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** The format id and schema of a Sudachi system dictionary pack, as in the catalog. */
object SudachiSchema {
  const val FORMAT = "sudachi-system"
  const val VERSION = 1
}

/** What the header of a Sudachi system dictionary says about the file. */
class SudachiHeader(
  /** Seconds since the epoch at which the dictionary was built. */
  val createTime: Long,
  /** Free text chosen at build time, such as `SudachiDict-core 20250825`. */
  val description: String,
)

/**
 * Reads the header of [file]. The header is the first 272 bytes: the version and the creation time
 * (little-endian longs), then 256 bytes of description.
 *
 * @return null if [file] is too short, or its version is not one of a Sudachi *system* dictionary
 *   (a user dictionary or some other file).
 */
fun readSudachiHeader(file: File): SudachiHeader? {
  val bytes = ByteArray(HEADER_SIZE)
  try {
    RandomAccessFile(file, "r").use { it.readFully(bytes) }
  } catch (e: IOException) { // too short (EOFException) or unreadable
    return null
  }
  val header = DictionaryHeader(ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN), 0)
  if (!DictionaryVersion.isSystemDictionary(header.version)) return null
  return SudachiHeader(header.createTime, header.description)
}

private const val HEADER_SIZE = 8 + 8 + 256
