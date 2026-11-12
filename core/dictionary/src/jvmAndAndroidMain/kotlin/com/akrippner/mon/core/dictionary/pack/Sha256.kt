package com.akrippner.mon.core.dictionary.pack

import java.io.File
import java.security.MessageDigest

private const val BUFFER_SIZE = 64 * 1024
private const val HEX_BYTE = "%02x" // one byte as two lowercase hex digits

/**
 * Hex SHA-256 of [file], streamed so a large file is never held in memory. The converter writes
 * this value into the catalog and the app checks every download against it, so both must use this
 * one function.
 */
fun sha256Hex(file: File): String {
  val digest = MessageDigest.getInstance("SHA-256")
  file.inputStream().use { input ->
    val buffer = ByteArray(BUFFER_SIZE)
    while (true) {
      val read = input.read(buffer)
      if (read == -1) break
      digest.update(buffer, 0, read)
    }
  }
  return digest.digest().joinToString("") { HEX_BYTE.format(it) }
}
