package com.akrippner.mon.core.domain.fake

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class AddedReading(
  val title: String,
  val text: String,
  val translation: String,
  val sourceLanguage: Language,
  val translationLanguage: Language,
  val tokens: List<MonToken>,
)

class FakeReadingRepository(private val newReadingId: String = "reading-1") : ReadingRepository {
  val added = mutableListOf<AddedReading>()
  var failure: Throwable? = null

  override fun observeReadingSummaries(): Flow<List<ReadingSummary>> = flowOf(emptyList())

  override suspend fun getReading(readingId: String): Reading? = null

  override suspend fun addReading(
    title: String,
    text: String,
    translation: String,
    sourceLanguage: Language,
    translationLanguage: Language,
    tokens: List<MonToken>,
  ): String {
    failure?.let { throw it }
    added += AddedReading(title, text, translation, sourceLanguage, translationLanguage, tokens)
    return newReadingId
  }

  override suspend fun deleteReading(readingId: String) = Unit

  override suspend fun recordReadingOpened(readingId: String) = Unit
}
