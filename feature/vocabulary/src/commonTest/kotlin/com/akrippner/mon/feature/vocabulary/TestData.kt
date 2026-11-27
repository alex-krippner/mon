package com.akrippner.mon.feature.vocabulary

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.data.VocabularyRepository
import com.akrippner.mon.core.model.data.JlptLevel
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.SentenceRef
import com.akrippner.mon.core.model.data.TokenAnalysis
import com.akrippner.mon.core.model.data.VocabularyGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

fun savedWord(
  id: String,
  form: String = id,
  readingId: String = "r1",
  kana: String = "かな",
  gloss: String = "gloss",
  note: String? = null,
  jlpt: JlptLevel? = null,
  savedAt: Long = 0,
) =
  SavedWord(
    id = id,
    readingId = readingId,
    dictionaryForm = form,
    surface = form,
    kana = kana,
    gloss = gloss,
    jlpt = jlpt,
    jmdictSeq = null,
    sentence = SentenceRef("$form。", 0, form.length, 0),
    note = note,
    savedAt = savedAt,
  )

/** Groups are what the view model observes; titles come from [titles]. */
class FakeVocabularyRepository : VocabularyRepository {
  val words = MutableStateFlow<List<SavedWord>>(emptyList())
  val titles = mutableMapOf("r1" to "First", "r2" to "Second")
  val noteWrites = mutableListOf<Pair<String, String?>>()

  override fun observeGroups(): Flow<List<VocabularyGroup>> =
    words.map { all ->
      all
        .groupBy { it.readingId }
        .map { (readingId, list) ->
          VocabularyGroup(
            readingId,
            titles.getValue(readingId),
            list.sortedByDescending { it.savedAt },
          )
        }
    }

  override fun observeForReading(readingId: String): Flow<List<SavedWord>> =
    words.map { all -> all.filter { it.readingId == readingId } }

  override fun observeCount(readingId: String): Flow<Int> = words.map { it.size }

  override fun observeSaved(readingId: String, dictionaryForm: String): Flow<SavedWord?> =
    words.map { all -> all.firstOrNull { it.dictionaryForm == dictionaryForm } }

  override fun observeWord(id: String): Flow<SavedWord?> =
    words.map { all -> all.firstOrNull { it.id == id } }

  override suspend fun save(word: NewSavedWord): SavedWord = error("Not used here")

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

/** Only the summaries and recordReadingOpened matter to the vocabulary screens. */
class FakeReadingRepository : ReadingRepository {
  val summaries = MutableStateFlow<List<ReadingSummary>>(emptyList())
  val opened = mutableListOf<String>()

  override fun observeReadingSummaries(): Flow<List<ReadingSummary>> = summaries

  override fun observeReading(readingId: String): Flow<Reading?> = error("Not used here")

  override suspend fun getReading(readingId: String): Reading? = null

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    analysis: TokenAnalysis?,
  ): String = error("Not used here")

  override suspend fun getPendingReadings(): List<PendingReading> = emptyList()

  override suspend fun saveAnalysis(readingId: String, analysis: TokenAnalysis) = false

  override suspend fun deleteReading(readingId: String) = Unit

  override suspend fun recordReadingOpened(readingId: String) {
    opened += readingId
  }
}
