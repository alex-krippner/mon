package com.akrippner.mon.feature.reader

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.dictionary.DictionaryProvider
import com.akrippner.mon.core.dictionary.MonDictionary
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import com.akrippner.mon.core.model.data.TokenAnalysis
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/** Only [observeReading] matters to the reader; the rest is never called. */
class FakeReadingRepository : ReadingRepository {
  /** The reading the reader observes; null means it does not exist. */
  val reading = MutableStateFlow<Reading?>(null)
  var observeFailure: Throwable? = null

  override fun observeReading(readingId: String): Flow<Reading?> {
    val failure = observeFailure ?: return reading
    return flow { throw failure }
  }

  override fun observeReadingSummaries(): Flow<List<ReadingSummary>> = flowOf(emptyList())

  override suspend fun getReading(readingId: String): Reading? = reading.value

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    analysis: TokenAnalysis?,
  ): String = error("Not used by the reader")

  override suspend fun getPendingReadings(): List<PendingReading> = emptyList()

  override suspend fun saveAnalysis(readingId: String, analysis: TokenAnalysis) = false

  override suspend fun deleteReading(readingId: String) = Unit

  override suspend fun recordReadingOpened(readingId: String) = Unit
}

class FakeDictionaryProvider(private val dictionaries: List<MonDictionary> = emptyList()) :
  DictionaryProvider {
  override suspend fun dictionariesFor(source: Language, gloss: Language): List<MonDictionary> =
    dictionaries.filter { it.sourceLanguage == source && it.glossLanguage == gloss }
}

/**
 * Answers every query with [entries]. A query for a lemma in [gates] waits until its gate is
 * completed, so a test can decide when a lookup finishes.
 */
class FakeDictionary(
  var entries: List<DictionaryEntry> = emptyList(),
  var failure: Throwable? = null,
  val gates: MutableMap<String, CompletableDeferred<Unit>> = mutableMapOf(),
) : MonDictionary {
  override val source = DictionarySource("fake", "Fake", "attribution", "1")
  override val sourceLanguage = Language.JAPANESE
  override val glossLanguage = Language.ENGLISH
  val queries = mutableListOf<LookupQuery>()

  override suspend fun lookup(query: LookupQuery): List<DictionaryEntry> {
    queries += query
    gates[query.lemma]?.await()
    failure?.let { throw it }
    return entries
  }
}
