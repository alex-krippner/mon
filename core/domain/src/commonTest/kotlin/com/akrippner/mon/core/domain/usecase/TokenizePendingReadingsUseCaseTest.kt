package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.domain.fake.FakeReadingRepository
import com.akrippner.mon.core.domain.fake.FakeTokenizer
import com.akrippner.mon.core.domain.fake.FakeTokenizerProvider
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.TokenAnalysis
import com.akrippner.mon.core.tokenizer.TokenizerUnavailableException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class TokenizePendingReadingsUseCaseTest {
  private val repository = FakeReadingRepository()
  private val tokens = listOf(MonToken("私", "私", "わたし", listOf("代名詞")))

  private fun pending(id: String, language: Language = Language.JAPANESE) =
    PendingReading(id, "text $id", language)

  private fun useCase(provider: FakeTokenizerProvider) =
    TokenizePendingReadingsUseCase(repository, provider)

  @Test
  fun givesEveryPendingReadingItsTokens() = runTest {
    repository.pending += listOf(pending("a"), pending("b"))
    val tokenizer = FakeTokenizer(tokens, id = "sudachi-core@1")

    val done = useCase(FakeTokenizerProvider(tokenizer)).execute()

    assertEquals(2, done)
    assertEquals(listOf("text a", "text b"), tokenizer.tokenizedTexts)
    assertEquals(
      mapOf(
        "a" to TokenAnalysis(tokens, "sudachi-core@1"),
        "b" to TokenAnalysis(tokens, "sudachi-core@1"),
      ),
      repository.saved,
    )
    assertEquals(emptyList(), repository.pending)
  }

  @Test
  fun withoutATokenizer_theReadingsStayPending() = runTest {
    repository.pending += pending("a")

    val done = useCase(FakeTokenizerProvider(null)).execute()

    assertEquals(0, done)
    assertEquals(listOf(pending("a")), repository.pending)
  }

  @Test
  fun aReadingInAnotherLanguageStaysPendingAndDoesNotBlockTheOthers() = runTest {
    repository.pending += listOf(pending("de", Language.GERMAN), pending("ja"))

    val done = useCase(FakeTokenizerProvider(FakeTokenizer(tokens))).execute()

    assertEquals(1, done)
    assertEquals(listOf(pending("de", Language.GERMAN)), repository.pending)
  }

  @Test
  fun anUnavailableTokenizerStopsTheRunAndLeavesTheReadingsPending() = runTest {
    repository.pending += listOf(pending("a"), pending("b"))
    val unavailable = FakeTokenizer(failure = TokenizerUnavailableException("pack replaced"))

    val done = useCase(FakeTokenizerProvider(unavailable)).execute()

    assertEquals(0, done)
    assertEquals(listOf("text a"), unavailable.tokenizedTexts) // it did not try "b"
    assertEquals(2, repository.pending.size)
  }

  @Test
  fun aReadingThatFailsDoesNotBlockTheOthers() = runTest {
    repository.pending += listOf(pending("a"), pending("b"))
    val tokenizer =
      object : com.akrippner.mon.core.tokenizer.Tokenizer {
        override val language = Language.JAPANESE
        override val id = "t"

        override suspend fun tokenize(text: String): List<MonToken> =
          if (text == "text a") error("bad text") else tokens
      }

    val done = useCase(FakeTokenizerProvider(tokenizer)).execute()

    assertEquals(1, done)
    assertEquals(listOf("a"), repository.pending.map { it.id })
  }

  @Test
  fun runningAgainDoesNothingWhenNothingIsPending() = runTest {
    val tokenizer = FakeTokenizer(tokens)

    assertEquals(0, useCase(FakeTokenizerProvider(tokenizer)).execute())
    assertEquals(emptyList(), tokenizer.tokenizedTexts)
  }
}
