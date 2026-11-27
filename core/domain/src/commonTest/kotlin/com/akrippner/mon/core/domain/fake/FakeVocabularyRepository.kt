package com.akrippner.mon.core.domain.fake

import com.akrippner.mon.core.data.VocabularyRepository
import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.VocabularyGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Keeps the saved words in memory, with the same rules as the real repository. */
class FakeVocabularyRepository : VocabularyRepository {
  val words = MutableStateFlow<List<SavedWord>>(emptyList())
  val saved = mutableListOf<NewSavedWord>()
  var failure: Throwable? = null
  private var nextId = 1

  override fun observeForReading(readingId: String): Flow<List<SavedWord>> =
    words.map { all ->
      all.filter { it.readingId == readingId }.sortedBy { it.sentence.wordOffset }
    }

  override fun observeGroups(): Flow<List<VocabularyGroup>> =
    words.map { all ->
      all
        .groupBy { it.readingId }
        .map { (readingId, words) ->
          VocabularyGroup(readingId, "Title $readingId", words.sortedByDescending { it.savedAt })
        }
    }

  override fun observeCount(readingId: String): Flow<Int> =
    words.map { all -> all.count { it.readingId == readingId } }

  override fun observeSaved(readingId: String, dictionaryForm: String): Flow<SavedWord?> =
    words.map { all ->
      all.firstOrNull { it.readingId == readingId && it.dictionaryForm == dictionaryForm }
    }

  override fun observeWord(id: String): Flow<SavedWord?> =
    words.map { all -> all.firstOrNull { it.id == id } }

  override suspend fun save(word: NewSavedWord): SavedWord {
    failure?.let { throw it }
    saved += word
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
    failure?.let { throw it }
    val word = words.value.firstOrNull { it.id == id } ?: return null
    words.value -= word
    return word
  }

  override suspend fun restore(word: SavedWord) {
    failure?.let { throw it }
    words.value = words.value.filterNot { it.id == word.id } + word
  }

  override suspend fun setNote(id: String, note: String?) {
    failure?.let { throw it }
    words.value =
      words.value.map { if (it.id == id) it.copy(note = note?.trim()?.ifEmpty { null }) else it }
  }
}
