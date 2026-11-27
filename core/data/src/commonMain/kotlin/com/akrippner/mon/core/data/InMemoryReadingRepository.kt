package com.akrippner.mon.core.data

import com.akrippner.mon.core.data.internal.newId
import com.akrippner.mon.core.data.internal.nowMillis
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import com.akrippner.mon.core.model.data.TokenAnalysis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Keeps the readings in memory, so everything is gone when the app closes. The browser build uses
 * it, because Room has no browser target; [initial] gives it the demo readings.
 */
class InMemoryReadingRepository(initial: List<Reading> = emptyList()) : ReadingRepository {
  // Oldest first, like the order they were added in; stable for readings with the same time.
  private val readings = MutableStateFlow(initial.sortedBy { it.createdAt })

  override fun observeReadingSummaries(): Flow<List<ReadingSummary>> =
    readings.map { list -> list.asReversed().map { it.toSummary() } }

  override suspend fun getReading(readingId: String): Reading? =
    readings.value.firstOrNull { it.id == readingId }

  override fun observeReading(readingId: String): Flow<Reading?> =
    readings.map { list -> list.firstOrNull { it.id == readingId } }

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    analysis: TokenAnalysis?,
  ): String {
    val reading =
      Reading(
        id = newId(),
        title = title,
        text = text,
        translation = translation,
        sourceLanguage = sourceLanguage,
        translationLanguage = translationLanguage,
        tokens = analysis?.tokens.orEmpty(),
        isTokenized = analysis != null,
        createdAt = nowMillis(),
      )
    readings.update { it + reading }
    return reading.id
  }

  override suspend fun getPendingReadings(): List<PendingReading> =
    readings.value
      .filterNot { it.isTokenized }
      .map { PendingReading(it.id, it.text, it.sourceLanguage) }

  override suspend fun saveAnalysis(readingId: String, analysis: TokenAnalysis): Boolean {
    var saved = false
    readings.update { list ->
      saved = false
      list.map { reading ->
        if (reading.id == readingId && !reading.isTokenized) {
          saved = true
          reading.copy(tokens = analysis.tokens, isTokenized = true)
        } else {
          reading
        }
      }
    }
    return saved
  }

  override suspend fun deleteReading(readingId: String) {
    readings.update { list -> list.filterNot { it.id == readingId } }
  }

  // There is no usage tracking in the demo.
  override suspend fun recordReadingOpened(readingId: String) = Unit

  private fun Reading.toSummary() =
    ReadingSummary(
      id = id,
      title = title,
      sourceLanguage = sourceLanguage,
      translationLanguage = translationLanguage,
      tokenCount = tokens.size,
      isTokenized = isTokenized,
      createdAt = createdAt,
    )
}
