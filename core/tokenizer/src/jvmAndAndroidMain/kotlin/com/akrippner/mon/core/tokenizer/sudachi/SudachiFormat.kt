package com.akrippner.mon.core.tokenizer.sudachi

import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.CloseableReader
import com.akrippner.mon.core.packs.PackFormat
import java.io.File

/** The Sudachi system dictionary (`system_core.dic`) as a pack. */
internal object SudachiFormat : PackFormat {
  override val format = SudachiSchema.FORMAT
  override val schema = SudachiSchema.VERSION

  override fun verify(file: File, pack: CatalogPack): Boolean =
    file.length() == pack.unpackedSizeBytes && readSudachiHeader(file) != null

  // Cheap on purpose: the dictionary is loaded on the first tokenize call.
  override fun open(file: File, pack: CatalogPack): CloseableReader = SudachiReader(file, pack)
}
