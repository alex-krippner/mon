package com.akrippner.mon.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.akrippner.mon.core.database.entity.ReadingUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingUsageDao {

  /** Records a new usage event. Call this every time a reading is opened or completed. */
  @Insert suspend fun insertUsage(usage: ReadingUsageEntity)

  /** Gets the full history for a specific reading, newest first. */
  @Query("SELECT * FROM reading_usage WHERE readingId = :readingId ORDER BY usedAt DESC")
  fun getUsageHistoryForReading(readingId: String): Flow<List<ReadingUsageEntity>>

  /**
   * Returns the timestamp (in millis) of the last time a reading was used. Returns null if it has
   * never been used.
   */
  @Query("SELECT MAX(usedAt) FROM reading_usage WHERE readingId = :readingId")
  suspend fun getLastUsedTimestamp(readingId: String): Long?

  /** Returns the total number of times a reading has been used. */
  @Query("SELECT COUNT(*) FROM reading_usage WHERE readingId = :readingId")
  fun getUsageCount(readingId: String): Flow<Int>

  /**
   * Deletes usage history for a specific reading. Useful if the user wants to "reset" their
   * progress/stats.
   */
  @Query("DELETE FROM reading_usage WHERE readingId = :readingId")
  suspend fun clearHistoryForReading(readingId: String)
}
