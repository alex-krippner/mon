package com.akrippner.mon.core.data

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.TokenAnalysis
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class InMemoryReadingRepositoryTest {
  private val analysis =
    TokenAnalysis(listOf(MonToken("猫", "猫", "ネコ", listOf("名詞"))), tokenizedWith = "test@1")

  private suspend fun InMemoryReadingRepository.add(
    title: String,
    analysis: TokenAnalysis? = null,
  ) = addReading(title, "text", "translation", Language.JAPANESE, Language.ENGLISH, analysis)

  @Test
  fun summariesListTheNewestReadingFirst() = runTest {
    val repository = InMemoryReadingRepository()
    repository.add("first")
    repository.add("second")

    val titles = repository.observeReadingSummaries().first().map { it.title }

    assertEquals(listOf("second", "first"), titles)
  }

  @Test
  fun readingWithoutAnalysisIsPendingUntilItGetsTokens() = runTest {
    val repository = InMemoryReadingRepository()
    val id = repository.add("pending")

    assertEquals(listOf(id), repository.getPendingReadings().map { it.id })
    assertFalse(repository.getReading(id)!!.isTokenized)

    assertTrue(repository.saveAnalysis(id, analysis))

    assertTrue(repository.getPendingReadings().isEmpty())
    val reading = repository.getReading(id)!!
    assertTrue(reading.isTokenized)
    assertEquals(analysis.tokens, reading.tokens)
  }

  @Test
  fun pendingReadingsComeOldestFirst() = runTest {
    val repository = InMemoryReadingRepository()
    val first = repository.add("first")
    val second = repository.add("second")

    assertEquals(listOf(first, second), repository.getPendingReadings().map { it.id })
  }

  @Test
  fun saveAnalysisOnlyWritesToAPendingReading() = runTest {
    val repository = InMemoryReadingRepository()
    val id = repository.add("done", analysis)

    assertFalse(repository.saveAnalysis(id, TokenAnalysis(emptyList(), "other@1")))
    assertFalse(repository.saveAnalysis("missing", analysis))
    assertEquals(analysis.tokens, repository.getReading(id)!!.tokens)
  }

  @Test
  fun deletedReadingIsGone() = runTest {
    val repository = InMemoryReadingRepository()
    val id = repository.add("gone")

    repository.deleteReading(id)

    assertNull(repository.getReading(id))
    assertNull(repository.observeReading(id).first())
    assertTrue(repository.observeReadingSummaries().first().isEmpty())
  }
}
