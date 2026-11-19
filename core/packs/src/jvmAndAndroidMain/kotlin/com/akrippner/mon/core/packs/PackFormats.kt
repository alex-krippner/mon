package com.akrippner.mon.core.packs

import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import java.io.File

/**
 * One file format the app can read. A module that owns a format registers one in Koin with `bind
 * PackFormat::class`; the manager collects them all. Not in `commonMain`, because it works on
 * `java.io.File`.
 */
interface PackFormat {
  val format: String
  val schema: Int

  fun verify(file: File, pack: CatalogPack): Boolean

  /**
   * Must be cheap and must not touch [file] yet: the manager opens a reader for every installed
   * pack whenever the readers are asked for. Open the file on the first real call instead.
   */
  fun open(file: File, pack: CatalogPack): CloseableReader
}

class PackFormats(private val formats: List<PackFormat>) {
  /** Null if the app can't read [pack]: unknown format, schema or language. Never offered. */
  fun formatOf(pack: CatalogPack): PackFormat? =
    formats
      .firstOrNull { it.format == pack.format && it.schema == pack.schema }
      ?.takeIf {
        Language.fromTagOrNull(pack.sourceLanguage) != null &&
          Language.fromTagOrNull(pack.glossLanguage) != null
      }
}

fun CatalogPack.toSource() = DictionarySource(id, name, attribution, version)
