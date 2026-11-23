package com.akrippner.mon.core.domain.fake

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import com.akrippner.mon.core.model.data.TokenAnalysis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class AddedReading(
  val title: String,
  val text: String,
  val translation: String,
  val sourceLanguage: Language,
  val translationLanguage: Language,
  val analysis: TokenAnalysis?,
) {
  val tokens: List<MonToken>
    get() = analysis?.tokens.orEmpty()
}

class FakeReadingRepository(private val newReadingId: String = "reading-1") : ReadingRepository {
  val added = mutableListOf<AddedReading>()
  var failure: Throwable? = null
  var pendingFailure: Throwable? = null

  /** The readings that wait for tokens. Saving an analysis removes the reading from here. */
  val pending = mutableListOf<PendingReading>()
  val saved = mutableMapOf<String, TokenAnalysis>()

  override fun observeReadingSummaries(): Flow<List<ReadingSummary>> = flowOf(emptyList())

  override suspend fun getReading(readingId: String): Reading? = null

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    analysis: TokenAnalysis?,
  ): String {
    failure?.let { throw it }
    added += AddedReading(title, text, translation, sourceLanguage, translationLanguage, analysis)
    if (analysis == null) pending += PendingReading(newReadingId, text, sourceLanguage)
    return newReadingId
  }

  override suspend fun getPendingReadings(): List<PendingReading> {
    pendingFailure?.let { throw it }
    return pending.toList()
  }

  override suspend fun saveAnalysis(readingId: String, analysis: TokenAnalysis): Boolean {
    if (pending.none { it.id == readingId }) return false
    pending.removeAll { it.id == readingId }
    saved[readingId] = analysis
    return true
  }

  override suspend fun deleteReading(readingId: String) = Unit

  override suspend fun recordReadingOpened(readingId: String) = Unit
}
