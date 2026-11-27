package com.akrippner.mon.feature.addreading

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import com.akrippner.mon.core.model.data.TokenAnalysis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeReadingRepository : ReadingRepository {
  val addedTitles = mutableListOf<String>()
  var failure: Throwable? = null

  override fun observeReadingSummaries(): Flow<List<ReadingSummary>> = flowOf(emptyList())

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    analysis: TokenAnalysis?,
  ): String {
    failure?.let { throw it }
    addedTitles += title
    return "reading-${addedTitles.size}"
  }

  override suspend fun getReading(readingId: String): Reading? = null

  override fun observeReading(readingId: String): Flow<Reading?> = flowOf(null)

  override suspend fun getPendingReadings(): List<PendingReading> = emptyList()

  override suspend fun saveAnalysis(readingId: String, analysis: TokenAnalysis) = false

  override suspend fun deleteReading(readingId: String) = Unit

  override suspend fun recordReadingOpened(readingId: String) = Unit
}
