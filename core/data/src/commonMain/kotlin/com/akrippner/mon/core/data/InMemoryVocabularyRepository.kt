package com.akrippner.mon.core.data

import com.akrippner.mon.core.data.internal.newId
import com.akrippner.mon.core.data.internal.nowMillis
import com.akrippner.mon.core.data.internal.toStoredNote
import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.VocabularyGroup
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Keeps the saved words in memory, so they are gone when the app closes. The browser build uses it,
 * because Room has no browser target. It takes the readings from [readings] for the group titles,
 * so the groups come in the order of the Library (newest reading first); it does not track which
 * reading was opened last.
 */
class InMemoryVocabularyRepository(private val readings: ReadingRepository) : VocabularyRepository {
  // In the order the words were saved, oldest first.
  private val words = MutableStateFlow<List<SavedWord>>(emptyList())

  override fun observeForReading(readingId: String): Flow<List<SavedWord>> =
    words.map { list ->
      list.filter { it.readingId == readingId }.sortedBy { it.sentence.wordOffset }
    }

  override fun observeGroups(): Flow<List<VocabularyGroup>> =
    combine(words, readings.observeReadingSummaries()) { saved, summaries ->
      summaries.mapNotNull { summary ->
        // Newest first; words saved at the same millisecond keep the order they were saved in.
        val ofReading =
          saved
            .withIndex()
            .filter { it.value.readingId == summary.id }
            .sortedWith(
              compareByDescending<IndexedValue<SavedWord>> { it.value.savedAt }
                .thenByDescending { it.index }
            )
            .map { it.value }
        if (ofReading.isEmpty()) null else VocabularyGroup(summary.id, summary.title, ofReading)
      }
    }

  override fun observeCount(readingId: String): Flow<Int> =
    words.map { list -> list.count { it.readingId == readingId } }

  override fun observeSaved(readingId: String, dictionaryForm: String): Flow<SavedWord?> =
    words.map { list ->
      list.firstOrNull { it.readingId == readingId && it.dictionaryForm == dictionaryForm }
    }

  override fun observeWord(id: String): Flow<SavedWord?> =
    words.map { list -> list.firstOrNull { it.id == id } }

  override suspend fun save(word: NewSavedWord): SavedWord {
    var result: SavedWord? = null
    words.update { list ->
      val existing =
        list.firstOrNull {
          it.readingId == word.readingId && it.dictionaryForm == word.dictionaryForm
        }
      if (existing != null) {
        result = existing
        list
      } else {
        val saved =
          SavedWord(
            id = newId(),
            readingId = word.readingId,
            dictionaryForm = word.dictionaryForm,
            surface = word.surface,
            kana = word.kana,
            gloss = word.gloss,
            jlpt = word.jlpt,
            jmdictSeq = word.jmdictSeq,
            sentence = word.sentence,
            note = null,
            savedAt = nowMillis(),
          )
        result = saved
        list + saved
      }
    }
    return result!!
  }

  override suspend fun remove(id: String): SavedWord? {
    var removed: SavedWord? = null
    words.update { list ->
      removed = list.firstOrNull { it.id == id }
      list.filterNot { it.id == id }
    }
    return removed
  }

  override suspend fun restore(word: SavedWord) {
    words.update { list -> list.filterNot { it.id == word.id } + word }
  }

  override suspend fun setNote(id: String, note: String?) {
    val stored = note.toStoredNote()
    words.update { list -> list.map { if (it.id == id) it.copy(note = stored) else it } }
  }
}
