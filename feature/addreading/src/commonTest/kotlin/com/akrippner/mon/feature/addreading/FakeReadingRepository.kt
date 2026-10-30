package com.akrippner.mon.feature.addreading

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
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
    tokens: List<MonToken>,
  ): String {
    failure?.let { throw it }
    addedTitles += title
    return "reading-${addedTitles.size}"
  }

  override suspend fun getReading(readingId: String): Reading? = null

  override suspend fun deleteReading(readingId: String) = Unit

  override suspend fun recordReadingOpened(readingId: String) = Unit
}
