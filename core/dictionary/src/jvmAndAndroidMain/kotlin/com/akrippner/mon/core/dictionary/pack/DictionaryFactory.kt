package com.akrippner.mon.core.dictionary.pack

import com.akrippner.mon.core.dictionary.CloseableReader
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import java.io.File

/**
 * One file format the app can read. A German dictionary format later adds one implementation. Not
 * in `commonMain`, because it works on `java.io.File`.
 */
internal interface DictionaryFormat {
  val format: String
  val schema: Int

  fun verify(file: File, pack: CatalogPack): Boolean

  /**
   * Must be cheap and must not touch [file] yet: the manager opens a reader for every installed
   * pack whenever the readers are asked for. Open the file on the first real call instead.
   */
  fun open(file: File, pack: CatalogPack): CloseableReader
}

internal class DictionaryFactory(private val formats: List<DictionaryFormat>) {
  /** Null if the app can't read [pack]: unknown format, schema or language. Never offered. */
  fun formatOf(pack: CatalogPack): DictionaryFormat? =
    formats
      .firstOrNull { it.format == pack.format && it.schema == pack.schema }
      ?.takeIf {
        Language.fromTagOrNull(pack.sourceLanguage) != null &&
          Language.fromTagOrNull(pack.glossLanguage) != null
      }
}

internal fun CatalogPack.toSource() = DictionarySource(id, name, attribution, version)
