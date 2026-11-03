package com.akrippner.mon.feature.library

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
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

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    tokens: List<MonToken>,
  ): String = error("Not used by the library")

  override suspend fun deleteReading(readingId: String) = Unit
}
