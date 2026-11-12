package com.akrippner.mon.core.dictionary.jmdict

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import com.akrippner.mon.core.dictionary.CloseableDictionary
import com.akrippner.mon.core.dictionary.pack.CatalogPack
import com.akrippner.mon.core.dictionary.pack.DictionaryFormat
import com.akrippner.mon.core.dictionary.pack.toSource
import com.akrippner.mon.core.model.data.Language
import java.io.File

internal fun isValidJmdictFile(file: File, pack: CatalogPack): Boolean =
  try {
    val connection = BundledSQLiteDriver().open(file.path, SQLITE_OPEN_READONLY)
    try {
      fun meta(key: String): String? =
        connection.prepare("SELECT value FROM meta WHERE key = ?").use { statement ->
          statement.bindText(1, key)
          if (statement.step()) statement.getText(0) else null
        }
      meta("format") == JmdictSchema.FORMAT &&
        meta("schema") == JmdictSchema.VERSION.toString() &&
        pack.format == JmdictSchema.FORMAT &&
        pack.schema == JmdictSchema.VERSION
    } finally {
      connection.close()
    }
  } catch (e: Exception) {
    false // not a SQLite file, or a damaged one
  }

internal object JmdictFormat : DictionaryFormat {
  override val format = JmdictSchema.FORMAT
  override val schema = JmdictSchema.VERSION

  override fun verify(file: File, pack: CatalogPack) = isValidJmdictFile(file, pack)

  override fun open(file: File, pack: CatalogPack): CloseableDictionary =
    JmdictDictionary(
      file.path,
      pack.toSource(),
      Language.fromTag(pack.sourceLanguage),
      Language.fromTag(pack.glossLanguage),
    )
}
