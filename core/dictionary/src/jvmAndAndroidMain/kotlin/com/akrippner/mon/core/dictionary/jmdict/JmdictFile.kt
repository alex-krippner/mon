package com.akrippner.mon.core.dictionary.jmdict

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import com.akrippner.mon.core.packs.CatalogPack
import com.akrippner.mon.core.packs.CloseableReader
import com.akrippner.mon.core.packs.PackFormat
import java.io.File

/** The value of the meta row [key], or null if the file has no such row. */
internal fun SQLiteConnection.meta(key: String): String? =
  prepare("SELECT value FROM meta WHERE key = ?").use { statement ->
    statement.bindText(1, key)
    if (statement.step()) statement.getText(0) else null
  }

internal fun isValidJmdictFile(file: File, pack: CatalogPack): Boolean =
  try {
    val connection = BundledSQLiteDriver().open(file.path, SQLITE_OPEN_READONLY)
    try {
      connection.meta("format") == JmdictSchema.FORMAT &&
        connection.meta("schema") == JmdictSchema.VERSION.toString() &&
        pack.format == JmdictSchema.FORMAT &&
        pack.schema == JmdictSchema.VERSION &&
        // A rule this app does not know: installing it would make every lookup miss.
        KeyNormalization.fromMeta(connection.meta(KEY_NORMALIZATION)) != null
    } finally {
      connection.close()
    }
  } catch (e: Exception) {
    false // not a SQLite file, or a damaged one
  }

internal object JmdictFormat : PackFormat {
  override val format = JmdictSchema.FORMAT
  override val schema = JmdictSchema.VERSION

  override fun verify(file: File, pack: CatalogPack) = isValidJmdictFile(file, pack)

  override fun open(file: File, pack: CatalogPack): CloseableReader =
    JmdictDictionary(file.path, pack)
}
