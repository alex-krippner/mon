package com.akrippner.mon.core.data

import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.VocabularyGroup
import kotlinx.coroutines.flow.Flow

/** The words the user saved from their readings. */
interface VocabularyRepository {
  /** Emits the reading's saved words in text order, whenever they change. */
  fun observeForReading(readingId: String): Flow<List<SavedWord>>

  /**
   * Emits the current user's saved words grouped by reading, the most recently opened reading
   * first. Each group lists its newest word first. Readings without saved words are not listed.
   */
  fun observeGroups(): Flow<List<VocabularyGroup>>

  fun observeCount(readingId: String): Flow<Int>

  /** Emits the word saved from the reading with this dictionary form, or null if there is none. */
  fun observeSaved(readingId: String, dictionaryForm: String): Flow<SavedWord?>

  /** Emits the word, and null once it is removed. */
  fun observeWord(id: String): Flow<SavedWord?>

  /**
   * Saves the word. If the reading already has a word with this dictionary form, nothing changes
   * and the existing word is returned.
   */
  suspend fun save(word: NewSavedWord): SavedWord

  /** Removes the word and returns it, for Undo. Returns null if it is already gone. */
  suspend fun remove(id: String): SavedWord?

  /** Puts a removed word back, with its id, note and time. */
  suspend fun restore(word: SavedWord)

  /** Sets the note. A blank note removes it; a note is cut to `MAX_NOTE_LENGTH` characters. */
  suspend fun setNote(id: String, note: String?)
}
