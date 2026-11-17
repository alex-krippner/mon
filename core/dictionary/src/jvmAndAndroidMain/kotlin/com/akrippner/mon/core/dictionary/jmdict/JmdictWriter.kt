package com.akrippner.mon.core.dictionary.jmdict

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.akrippner.mon.core.model.data.Language
import kotlinx.serialization.json.Json

/**
 * Writes a `jmdict-sqlite` file. [sourceLanguage] is the language of the headwords; it decides how
 * the lookup keys are normalized, and the file records that rule for the reader.
 */
class JmdictWriter(path: String, sourceLanguage: Language) : AutoCloseable {
  private val connection = BundledSQLiteDriver().open(path)
  private val json = Json
  private val normalization = KeyNormalization.forLanguage(sourceLanguage)

  init {
    JmdictSchema.CREATE.forEach { sql -> connection.prepare(sql).use { it.step() } }
    connection.prepare("BEGIN").use { it.step() }
    putMeta("format", JmdictSchema.FORMAT)
    putMeta("schema", JmdictSchema.VERSION.toString())
    // No row for "not normalized", so a Japanese file is the same as before the rule existed.
    normalization.id?.let { putMeta(KEY_NORMALIZATION, it) }
  }

  private val insertEntry =
    connection.prepare("INSERT INTO entry(id, common, payload) VALUES (?, ?, ?)")
  private val insertKey = connection.prepare("INSERT INTO lookup_key(key, entry_id) VALUES (?, ?)")

  fun putMeta(key: String, value: String) {
    connection.prepare("INSERT OR REPLACE INTO meta(key, value) VALUES (?,?)").use { statement ->
      statement.bindText(1, key)
      statement.bindText(2, value)
      statement.step()
    }
  }

  fun insert(record: JmdictRecord) {
    insertEntry.bindLong(1, record.id)
    insertEntry.bindLong(2, if (record.common) 1 else 0)
    insertEntry.bindText(3, json.encodeToString(record.toPayload()))
    insertEntry.step()
    insertEntry.reset()

    // Normalized first, deduplicated second: "Haus" and "haus" may become the same key, and the
    // table has no unique constraint, so a repeated row would return the entry twice.
    val keys =
      record.headwords
        .flatMap { listOfNotNull(it.text, it.reading) }
        .map(normalization::apply)
        .toSet()
    for (key in keys) {
      insertKey.bindText(1, key)
      insertKey.bindLong(2, record.id)
      insertKey.step()
      insertKey.reset()
    }
  }

  fun finish() {
    connection.prepare("COMMIT").use { it.step() }
    connection.prepare("VACUUM").use { it.step() }
  }

  override fun close() {
    insertEntry.close()
    insertKey.close()
    connection.close()
  }
}
