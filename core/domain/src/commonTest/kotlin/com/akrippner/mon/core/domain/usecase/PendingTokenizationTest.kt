package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.domain.fake.FakeReadingRepository
import com.akrippner.mon.core.domain.fake.FakeTokenizer
import com.akrippner.mon.core.domain.fake.FakeTokenizerProvider
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.tokenizer.Tokenizer
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceTimeBy
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

  @Test
  fun aFailedRunDoesNotStopLaterRuns() = runTest {
    repository.pending += PendingReading("a", "text", Language.JAPANESE)
    provider.tokenizer = FakeTokenizer()
    repository.pendingFailure = IllegalStateException("database locked")

    pendingTokenization().start(backgroundScope)
    testScheduler.runCurrent()
    assertEquals(1, repository.pending.size) // the first run failed

    repository.pendingFailure = null
    provider.changed()
    testScheduler.runCurrent()

    assertEquals(emptyList(), repository.pending)
  }

  @Test
  fun aRequestStartsARun() = runTest {
    provider.tokenizer = FakeTokenizer()
    val pendingTokenization = pendingTokenization()
    pendingTokenization.start(backgroundScope)
    testScheduler.runCurrent()

    repository.pending += PendingReading("a", "text", Language.JAPANESE)
    pendingTokenization.request()
    testScheduler.runCurrent()

    assertEquals(emptyList(), repository.pending)
  }

  @Test
  fun aFailingChangesFlowIsCollectedAgainAndCatchesUp() = runTest {
    var attempts = 0
    val failing =
      object : TokenizerProvider {
        val tokenizer = FakeTokenizer()

        override suspend fun tokenizerFor(language: Language): Tokenizer = tokenizer

        override val changes: Flow<Unit> = flow {
          if (attempts++ == 0) throw IllegalStateException("cannot read the packs")
          awaitCancellation()
        }
      }
    PendingTokenization(TokenizePendingReadingsUseCase(repository, failing), failing)
      .start(backgroundScope)
    testScheduler.runCurrent()
    assertEquals(1, attempts) // it failed and waits to try again

    repository.pending += PendingReading("a", "text", Language.JAPANESE)
    testScheduler.advanceTimeBy(31.seconds)
    testScheduler.runCurrent()

    assertEquals(2, attempts)
    assertEquals(emptyList(), repository.pending) // the run after the retry found it
  }
}
