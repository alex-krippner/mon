package com.akrippner.mon.feature.reader

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.data.VocabularyRepository
import com.akrippner.mon.core.dictionary.DictionaryProvider
import com.akrippner.mon.core.dictionary.MonDictionary
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.TokenAnalysis
import com.akrippner.mon.core.model.data.VocabularyGroup
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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

/** Keeps the saved words in memory; the reader only uses the words of one reading. */
class FakeVocabularyRepository : VocabularyRepository {
  val words = MutableStateFlow<List<SavedWord>>(emptyList())
  val noteWrites = mutableListOf<Pair<String, String?>>()
  private var nextId = 1

  override fun observeForReading(readingId: String): Flow<List<SavedWord>> =
    words.map { all -> all.filter { it.readingId == readingId } }

  override fun observeGroups(): Flow<List<VocabularyGroup>> = flowOf(emptyList())

  override fun observeCount(readingId: String): Flow<Int> =
    words.map { all -> all.count { it.readingId == readingId } }

  override fun observeSaved(readingId: String, dictionaryForm: String): Flow<SavedWord?> =
    words.map { all ->
      all.firstOrNull { it.readingId == readingId && it.dictionaryForm == dictionaryForm }
    }

  override fun observeWord(id: String): Flow<SavedWord?> =
    words.map { all -> all.firstOrNull { it.id == id } }

  override suspend fun save(word: NewSavedWord): SavedWord {
    words.value
      .firstOrNull { it.readingId == word.readingId && it.dictionaryForm == word.dictionaryForm }
      ?.let {
        return it
      }
    val stored =
      SavedWord(
        id = "word-${nextId++}",
        readingId = word.readingId,
        dictionaryForm = word.dictionaryForm,
        surface = word.surface,
        kana = word.kana,
        gloss = word.gloss,
        jlpt = word.jlpt,
        jmdictSeq = word.jmdictSeq,
        sentence = word.sentence,
        note = null,
        savedAt = nextId.toLong(),
      )
    words.value += stored
    return stored
  }

  override suspend fun remove(id: String): SavedWord? {
    val word = words.value.firstOrNull { it.id == id } ?: return null
    words.value -= word
    return word
  }

  override suspend fun restore(word: SavedWord) {
    words.value = words.value.filterNot { it.id == word.id } + word
  }

  override suspend fun setNote(id: String, note: String?) {
    noteWrites += id to note
    words.value =
      words.value.map { if (it.id == id) it.copy(note = note?.trim()?.ifEmpty { null }) else it }
  }
}
