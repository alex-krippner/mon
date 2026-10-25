package com.akrippner.mon.core.data

import com.akrippner.mon.core.data.internal.newId
import com.akrippner.mon.core.data.internal.nowMillis
import com.akrippner.mon.core.data.mapper.toDomainOrNull
import com.akrippner.mon.core.data.mapper.toEntity
import com.akrippner.mon.core.database.dao.ReadingDao
import com.akrippner.mon.core.database.dao.ReadingUsageDao
import com.akrippner.mon.core.database.entity.ReadingEntity
import com.akrippner.mon.core.database.entity.ReadingUsageEntity
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

internal class RoomReadingRepository(
  private val readingDao: ReadingDao,
  private val readingUsageDao: ReadingUsageDao,
  private val userRepository: UserRepository,
) : ReadingRepository {

  override fun observeReadings(): Flow<List<Reading>> = flow {
    val userId = userRepository.currentUserId()
    emitAll(
      readingDao.observeReadingsWithTokens(userId).map { readings ->
        readings.mapNotNull { it.toDomainOrNull() }
      }
    )
  }

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
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
          sourceLanguage = sourceLanguage.tag,
          translationLanguage = translationLanguage.tag,
          createdAt = now,
          updatedAt = now,
        ),
      tokens = tokens.mapIndexed { index, token -> token.toEntity(readingId, index) },
    )
    return readingId
  }

  override suspend fun deleteReading(readingId: String) {
    readingDao.deleteById(readingId)
  }

  override suspend fun recordReadingOpened(readingId: String) {
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
