package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.domain.fake.FakeReadingRepository
import com.akrippner.mon.core.domain.fake.FakeTokenizer
import com.akrippner.mon.core.domain.fake.FakeTokenizerProvider
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.PendingReading
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class PendingTokenizationTest {
  private val repository = FakeReadingRepository()
  private val provider = FakeTokenizerProvider()

  private fun pendingTokenization() =
    PendingTokenization(TokenizePendingReadingsUseCase(repository, provider), provider)

  @Test
  fun tokenizesWhatIsPendingAtStart() = runTest {
    repository.pending += PendingReading("a", "text", Language.JAPANESE)
    provider.tokenizer = FakeTokenizer()

    val job = pendingTokenization().start(backgroundScope)
    testScheduler.runCurrent()

    assertEquals(emptyList(), repository.pending)
    job.cancel()
  }

  @Test
  fun tokenizesAgainWhenATokenizerBecomesAvailable() = runTest {
    repository.pending += PendingReading("a", "text", Language.JAPANESE)
    pendingTokenization().start(backgroundScope)
    testScheduler.runCurrent()
    assertEquals(1, repository.pending.size) // no tokenizer yet

    provider.tokenizer = FakeTokenizer()
    provider.changed()
    testScheduler.runCurrent()

    assertEquals(emptyList(), repository.pending)
  }
}
