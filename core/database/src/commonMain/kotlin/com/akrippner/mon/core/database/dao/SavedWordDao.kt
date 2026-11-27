package com.akrippner.mon.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.akrippner.mon.core.database.entity.SavedWordEntity
import com.akrippner.mon.core.database.entity.SavedWordGroupRow
import kotlinx.coroutines.flow.Flow

/** Access to the words the user saved from their readings. */
@Dao
abstract class SavedWordDao {
  /** Ignores the word if the reading already has one with the same dictionary form. */
  @Insert(onConflict = OnConflictStrategy.IGNORE)
  protected abstract suspend fun insertIgnoring(word: SavedWordEntity): Long

  /**
   * Restores a removed word, with its note. It replaces a word that was saved again with the same
   * dictionary form in the meantime.
   */
  @Insert(onConflict = OnConflictStrategy.REPLACE)
  abstract suspend fun insertOrReplace(word: SavedWordEntity)

  @Query(
    "SELECT * FROM saved_word WHERE readingId = :readingId AND dictionaryForm = :dictionaryForm"
  )
  abstract suspend fun getByForm(readingId: String, dictionaryForm: String): SavedWordEntity?

  @Query("SELECT * FROM saved_word WHERE id = :id")
  abstract suspend fun getById(id: String): SavedWordEntity?

  /**
   * Saves the word unless the reading already has it, and returns the stored word either way, so
   * saving twice does nothing.
   */
  @Transaction
  open suspend fun saveIfAbsent(word: SavedWordEntity): SavedWordEntity {
    insertIgnoring(word)
    return getByForm(word.readingId, word.dictionaryForm) ?: word
  }

  @Query("DELETE FROM saved_word WHERE id = :id")
  protected abstract suspend fun deleteById(id: String)

  /** Deletes the word and returns it, or null if it is gone. */
  @Transaction
  open suspend fun remove(id: String): SavedWordEntity? {
    val word = getById(id) ?: return null
    deleteById(id)
    return word
  }

  @Query("UPDATE saved_word SET note = :note WHERE id = :id")
  abstract suspend fun setNote(id: String, note: String?)

  /** The reading's words in the order they appear in the text. */
  @Query(
    "SELECT * FROM saved_word WHERE readingId = :readingId ORDER BY textOffset + wordStart ASC"
  )
  abstract fun observeForReading(readingId: String): Flow<List<SavedWordEntity>>

  @Query("SELECT COUNT(*) FROM saved_word WHERE readingId = :readingId")
  abstract fun observeCount(readingId: String): Flow<Int>

  @Query(
    "SELECT * FROM saved_word WHERE readingId = :readingId AND dictionaryForm = :dictionaryForm"
  )
  abstract fun observeByForm(readingId: String, dictionaryForm: String): Flow<SavedWordEntity?>

  /** Emits null once the word is removed. */
  @Query("SELECT * FROM saved_word WHERE id = :id")
  abstract fun observeById(id: String): Flow<SavedWordEntity?>

  /**
   * The user's saved words with their reading, readings most recently opened first (a reading that
   * was never opened counts from its creation), and each reading's newest word first.
   */
  @Query(
    """
    SELECT w.*, r.title AS readingTitle,
    COALESCE((SELECT MAX(u.usedAt) FROM reading_usage u WHERE u.readingId = r.id), r.createdAt)
      AS lastOpenedAt
    FROM saved_word w JOIN reading r ON r.id = w.readingId
    WHERE r.userId = :userId
    ORDER BY lastOpenedAt DESC, w.readingId ASC, w.savedAt DESC
  """
  )
  abstract fun observeGroupRows(userId: String): Flow<List<SavedWordGroupRow>>
}
