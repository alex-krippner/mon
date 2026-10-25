package com.akrippner.mon.core.data

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import kotlinx.coroutines.flow.Flow

interface ReadingRepository {
  /** Emits the current user's readings, newest first, whenever they change. */
  fun observeReadings(): Flow<List<Reading>>

  /** Stores a reading with its tokens atomically and returns the id of the new reading. */
  suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    tokens: List<MonToken>,
  ): String

  suspend fun deleteReading(readingId: String)

  /** Records that the user opened the reading, for usage tracking. */
  suspend fun recordReadingOpened(readingId: String)
}
