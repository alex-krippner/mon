package com.akrippner.mon.core.dictionary.jmdict

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import com.akrippner.mon.core.dictionary.CloseableReader
import com.akrippner.mon.core.dictionary.DictionaryClosedException
import com.akrippner.mon.core.dictionary.MonDictionary
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Label
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

private const val MAX_RESULTS = 10
private const val FIND_SQL =
  "SELECT e.id, e.common, e.payload FROM lookup_key k JOIN entry e ON e.id = k.entry_id WHERE k.key = ?"

internal class JmdictDictionary(
  private val path: String,
  override val source: DictionarySource,
  override val sourceLanguage: Language,
  override val glossLanguage: Language,
) : MonDictionary, CloseableReader {

  private val mutex = Mutex()
  private val json = Json { ignoreUnknownKeys = true }
  private var connection: SQLiteConnection? = null
  /** Read from the file when [connection] is opened. */
  private var normalization = KeyNormalization.NONE
  private var closed = false

  override suspend fun lookup(query: LookupQuery): List<DictionaryEntry> =
    withContext(Dispatchers.IO) {
      mutex.withLock {
        // A closed reader must not reopen the file behind the manager's back: nobody would close
        // it again, and it would block replacing or deleting the file. It must not answer "nothing
        // found" either: the word may well be in the pack, so the caller has to ask a new reader.
        if (closed) throw DictionaryClosedException()
        val connection = openConnection()
        val lemma = normalization.apply(query.lemma)
        val surface = query.surface?.let(normalization::apply)
        var candidates = find(connection, lemma)
        if (candidates.isEmpty() && surface != null && surface != lemma) {
          candidates = find(connection, surface)
        }
        val reading = query.reading?.toHiragana()
        candidates
          .sortedWith(compareBy({ !it.hasReading(reading) }, { !it.common }, { it.id }))
          .take(MAX_RESULTS)
          .map { it.toEntry() }
      }
    }

  /**
   * Opens the file on the first lookup and reads how its keys are normalized. The rule comes from
   * the file, not from [sourceLanguage], so it is always the one the writer used.
   */
  private fun openConnection(): SQLiteConnection {
    connection?.let {
      return it
    }
    val opened = BundledSQLiteDriver().open(path, SQLITE_OPEN_READONLY)
    try {
      val rule = opened.meta(KEY_NORMALIZATION)
      // Unreachable for an installed pack: verify() rejects such a file.
      normalization = checkNotNull(KeyNormalization.fromMeta(rule)) { "Unknown key rule: $rule" }
    } catch (e: Exception) {
      opened.close()
      throw e
    }
    connection = opened
    return opened
  }

  /** [key] must already be normalized with [normalization]. */
  private fun find(connection: SQLiteConnection, key: String): List<Candidate> =
    connection.prepare(FIND_SQL).use { statement ->
      statement.bindText(1, key)
      buildList {
        while (statement.step()) {
          add(
            Candidate(
              id = statement.getLong(0),
              common = statement.getLong(1) == 1L,
              payload = json.decodeFromString<EntryPayload>(statement.getText(2)),
            )
          )
        }
      }
    }

  /**
   * Closes the file for good, e.g. before a pack is replaced or removed. Later lookups throw
   * [DictionaryClosedException]; the manager hands out a new instance for the new file.
   */
  override suspend fun close() {
    mutex.withLock {
      closed = true
      try {
        connection?.close()
      } finally {
        connection = null
      }
    }
  }

  private fun Candidate.toEntry() =
    DictionaryEntry(
      id = "${source.id}:$id",
      source = source,
      headwords = payload.headwords.map { it.toHeadword() },
      senses = payload.senses.map { it.toSense() },
      labels =
        buildList {
          if (common) add(Label.Frequency("common"))
          addAll(payload.labels.map { it.toLabel() })
        },
    )
}

private class Candidate(val id: Long, val common: Boolean, val payload: EntryPayload) {
  fun hasReading(reading: String?): Boolean =
    reading != null && payload.headwords.any { (it.reading ?: it.text).toHiragana() == reading }
}

/**
 * Katakana to hiragana; everything else passes through unchanged. In Unicode the katakana block has
 * the same order as the hiragana block, 0x60 (96) code points later: ァ is U+30A1 and ぁ is U+3041.
 * `'ァ'..'ヶ'` are the characters that have a hiragana twin, so ー and kanji are left alone.
 */
internal fun String.toHiragana(): String =
  map { if (it in 'ァ'..'ヶ') it - 0x60 else it }.joinToString("")
