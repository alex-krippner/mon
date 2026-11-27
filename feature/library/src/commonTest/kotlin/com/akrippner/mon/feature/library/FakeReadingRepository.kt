package com.akrippner.mon.feature.library

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import com.akrippner.mon.core.model.data.TokenAnalysis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class FakeReadingRepository : ReadingRepository {
  val summaries = MutableStateFlow<List<ReadingSummary>>(emptyList())
  val openedIds = mutableListOf<String>()
  var observeFailure: Throwable? = null
  var recordFailure: Throwable? = null

  override fun observeReadingSummaries(): Flow<List<ReadingSummary>> {
    val failure = observeFailure ?: return summaries
    return flow { throw failure }
  }

  override suspend fun recordReadingOpened(readingId: String) {
    recordFailure?.let { throw it }
    openedIds += readingId
  }

  override suspend fun getReading(readingId: String): Reading? = null

  override fun observeReading(readingId: String): Flow<Reading?> = MutableStateFlow(null)

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    analysis: TokenAnalysis?,
  ): String = error("Not used by the library")

  override suspend fun getPendingReadings(): List<PendingReading> = emptyList()

  override suspend fun saveAnalysis(readingId: String, analysis: TokenAnalysis) = false

  override suspend fun deleteReading(readingId: String) = Unit
}
