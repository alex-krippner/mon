package com.akrippner.mon.core.dictionary.jmdict

import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Label
import com.akrippner.mon.core.model.data.Sense

/** One JMdict entry as the converter hands it to [JmdictWriter]. */
class JmdictRecord(
  val id: Long,
  val common: Boolean,
  val senses: List<Sense>,
  val headwords: List<Headword>,
  val labels: List<Label> = emptyList(),
)

/** FORMAT and VERSION go into `catalog.json`, so the converter needs them too. */
object JmdictSchema {
  const val FORMAT = "jmdict-sqlite"
  const val VERSION = 1

  internal val CREATE =
    listOf(
      "CREATE TABLE meta(key TEXT PRIMARY KEY NOT NULL, value TEXT NOT NULL)",
      "CREATE TABLE entry(id INTEGER PRIMARY KEY NOT NULL, common INTEGER NOT NULL, payload TEXT NOT NULL)",
      "CREATE TABLE lookup_key(key TEXT NOT NULL, entry_id INTEGER NOT NULL)",
      "CREATE INDEX lookup_key_key ON lookup_key(key)",
    )
}
