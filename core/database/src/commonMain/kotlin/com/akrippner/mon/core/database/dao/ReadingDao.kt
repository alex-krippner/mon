package com.akrippner.mon.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.akrippner.mon.core.database.entity.ReadingEntity
import com.akrippner.mon.core.database.entity.ReadingSummaryRow
import com.akrippner.mon.core.database.entity.ReadingTokenEntity
import com.akrippner.mon.core.database.entity.ReadingWithTokens
import kotlinx.coroutines.flow.Flow

/**
 * Access to readings and their tokens. A reading and its tokens form one unit, so they are written
 * together in a single transaction.
 */
@Dao
abstract class ReadingDao {
  @Insert protected abstract suspend fun insertReading(reading: ReadingEntity)

  @Insert protected abstract suspend fun insertTokens(tokens: List<ReadingTokenEntity>)

  @Transaction
  open suspend fun insertReadingWithTokens(
    reading: ReadingEntity,
    tokens: List<ReadingTokenEntity>,
  ) {
    insertReading(reading)
    insertTokens(tokens)
  }

  @Transaction
  @Query("SELECT * FROM reading WHERE userId = :userId ORDER BY createdAt DESC")
  abstract fun observeReadingsWithTokens(userId: String): Flow<List<ReadingWithTokens>>

  @Query("SELECT * FROM reading_token WHERE readingId = :readingId ORDER BY tokenIndex ASC")
  abstract suspend fun getTokens(readingId: String): List<ReadingTokenEntity>

  @Query("DELETE FROM reading WHERE id = :readingId")
  abstract suspend fun deleteById(readingId: String)

  @Query(
    """
    SELECT r.id, r.title, r.sourceLanguage, r.translationLanguage, r.createdAt,
    (SELECT COUNT(*) FROM reading_token t WHERE t.readingId = r.id) AS tokenCount
    FROM reading r WHERE r.userId = :userId ORDER BY r.createdAt DESC
  """
  )
  abstract fun observeReadingSummaries(userId: String): Flow<List<ReadingSummaryRow>>

  @Transaction
  @Query("SELECT * FROM reading WHERE id = :readingId")
  abstract suspend fun getReadingWithTokens(readingId: String): ReadingWithTokens?
}
