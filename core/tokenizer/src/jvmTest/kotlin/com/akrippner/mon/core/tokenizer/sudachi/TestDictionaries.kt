package com.akrippner.mon.core.tokenizer.sudachi

import com.akrippner.mon.core.packs.CatalogPack
import com.worksap.nlp.sudachi.dictionary.DictionaryVersion
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Writes a file that has the header of a Sudachi dictionary and [bodySize] bytes of nothing after
 * it. Enough for the checks that only look at the header; it can't tokenize.
 */
internal fun File.writeSudachiHeader(
  version: Long = DictionaryVersion.SYSTEM_DICT_VERSION_2,
  createTime: Long = 1_700_000_000,
  description: String = "SudachiDict-core 20250825",
  bodySize: Int = 100,
): File {
  val buffer = ByteBuffer.allocate(8 + 8 + 256 + bodySize).order(ByteOrder.LITTLE_ENDIAN)
  buffer.putLong(version)
  buffer.putLong(createTime)
  buffer.put(description.toByteArray())
  writeBytes(buffer.array())
  return this
}

internal fun sudachiPack(unpackedSizeBytes: Long = 0, id: String = "sudachi-core") =
  CatalogPack(
    id = id,
    name = "Sudachi",
    sourceLanguage = "ja",
    glossLanguage = "ja",
    format = SudachiSchema.FORMAT,
    schema = SudachiSchema.VERSION,
    version = "20250825",
    url = "https://example.test/$id.dic.gz",
    sizeBytes = 0,
    unpackedSizeBytes = unpackedSizeBytes,
    sha256 = "sha-$id",
    attribution = "attribution",
  )
