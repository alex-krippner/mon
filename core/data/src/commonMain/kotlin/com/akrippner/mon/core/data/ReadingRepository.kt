package com.akrippner.mon.core.data

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import com.akrippner.mon.core.model.data.TokenAnalysis
import kotlinx.coroutines.flow.Flow

// TODO: Create and move to a directory for exposed interfaces
interface ReadingRepository {
  /** Emits the current user's readings, newest first, whenever they change. */
  fun observeReadingSummaries(): Flow<List<ReadingSummary>>

  suspend fun getReading(readingId: String): Reading?

  /**
   * Emits the reading, and again whenever it changes (for example when a pending reading gets its
   * tokens). Emits null if the reading does not exist or is gone.
   */
  fun observeReading(readingId: String): Flow<Reading?>

  /**
   * Stores a reading with its tokens atomically and returns the id of the new reading. Without an
   * [analysis] the reading is stored as pending and gets its tokens later, see [saveAnalysis].
   */
  suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    analysis: TokenAnalysis?,
  ): String

  /** The current user's readings that wait for their tokens, oldest first. */
  suspend fun getPendingReadings(): List<PendingReading>

  /**
   * Gives a pending reading its tokens. Returns false and changes nothing if the reading is gone or
   * already has tokens.
   */
  suspend fun saveAnalysis(readingId: String, analysis: TokenAnalysis): Boolean

  suspend fun deleteReading(readingId: String)

  /** Records that the user opened the reading, for usage tracking. */
  suspend fun recordReadingOpened(readingId: String)
}
