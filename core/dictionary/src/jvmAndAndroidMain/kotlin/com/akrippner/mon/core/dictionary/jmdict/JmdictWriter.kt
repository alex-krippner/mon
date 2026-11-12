package com.akrippner.mon.core.dictionary.jmdict

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.serialization.json.Json

class JmdictWriter(path: String) : AutoCloseable {
  private val connection = BundledSQLiteDriver().open(path)
  private val json = Json

  init {
    JmdictSchema.CREATE.forEach { sql -> connection.prepare(sql).use { it.step() } }
    connection.prepare("BEGIN").use { it.step() }
    putMeta("format", JmdictSchema.FORMAT)
    putMeta("schema", JmdictSchema.VERSION.toString())
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

    val keys = record.headwords.flatMap { listOfNotNull(it.text, it.reading) }.toSet()
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
