package com.akrippner.mon.core.data

import com.akrippner.mon.core.data.internal.newId
import com.akrippner.mon.core.data.internal.nowMillis
import com.akrippner.mon.core.data.mapper.toDomain
import com.akrippner.mon.core.data.mapper.toEntity
import com.akrippner.mon.core.database.dao.ReadingDao
import com.akrippner.mon.core.database.dao.ReadingUsageDao
import com.akrippner.mon.core.database.entity.ReadingEntity
import com.akrippner.mon.core.database.entity.ReadingUsageEntity
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ReadingRepository(
  private val readingDao: ReadingDao,
  private val readingUsageDao: ReadingUsageDao,
  private val userRepository: UserRepository,
) {

  /** Emits the current user's readings, newest first, whenever they change. */
  @OptIn(ExperimentalCoroutinesApi::class)
  fun observeReadings(): Flow<List<Reading>> =
    flow { emit(userRepository.currentUserId()) }
      .flatMapLatest { userId ->
        readingDao.observeReadingsWithTokens(userId).map { readings ->
          readings.map { it.toDomain() }
        }
      }

  /** Stores a reading with its tokens atomically and returns the id of the new reading. */
  suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: String,
    translationLanguage: String,
    tokens: List<MonToken>,
  ): String {
    val readingId = newId()
    val now = nowMillis()
    readingDao.insertReadingWithTokens(
      reading =
        ReadingEntity(
          id = readingId,
          userId = userRepository.currentUserId(),
          title = title,
          text = text,
          translation = translation,
          sourceLanguage = sourceLanguage,
          translationLanguage = translationLanguage,
          createdAt = now,
          updatedAt = now,
        ),
      tokens = tokens.mapIndexed { index, token -> token.toEntity(readingId, index) },
    )
    return readingId
  }

  suspend fun deleteReading(readingId: String) {
    readingDao.deleteById(readingId)
  }

  suspend fun recordReadingOpened(readingId: String) {
    readingUsageDao.insertUsage(
      ReadingUsageEntity(
        id = newId(),
        userId = userRepository.currentUserId(),
        readingId = readingId,
        usedAt = nowMillis(),
      )
    )
  }
}
