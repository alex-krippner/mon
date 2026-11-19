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
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import com.akrippner.mon.core.model.data.TokenAnalysis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

// TODO: Move to the internal directory?
internal class RoomReadingRepository(
  private val readingDao: ReadingDao,
  private val readingUsageDao: ReadingUsageDao,
  private val userRepository: UserRepository,
) : ReadingRepository {

  override fun observeReadingSummaries(): Flow<List<ReadingSummary>> = flow {
    val userId = userRepository.currentUserId()
    emitAll(
      readingDao.observeReadingSummaries(userId).map { rows ->
        rows.mapNotNull { it.toDomainOrNull() }
      }
    )
  }

  override suspend fun getReading(readingId: String): Reading? {
    return readingDao.getReadingWithTokens(readingId = readingId)?.toDomainOrNull()
  }

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    analysis: TokenAnalysis?,
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
          tokenizedWith = analysis?.tokenizedWith,
        ),
      tokens =
        analysis?.tokens.orEmpty().mapIndexed { index, token -> token.toEntity(readingId, index) },
    )
    return readingId
  }

  override suspend fun getPendingReadings(): List<PendingReading> =
    readingDao.getPendingReadings(userRepository.currentUserId()).mapNotNull { it.toDomainOrNull() }

  override suspend fun saveAnalysis(readingId: String, analysis: TokenAnalysis): Boolean =
    readingDao.setTokens(
      readingId = readingId,
      tokens = analysis.tokens.mapIndexed { index, token -> token.toEntity(readingId, index) },
      tokenizedWith = analysis.tokenizedWith,
    )

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
