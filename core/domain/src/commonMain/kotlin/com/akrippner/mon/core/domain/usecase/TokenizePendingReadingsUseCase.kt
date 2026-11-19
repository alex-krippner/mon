package com.akrippner.mon.core.domain.usecase

import co.touchlab.kermit.Logger
import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.model.data.TokenAnalysis
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import com.akrippner.mon.core.tokenizer.TokenizerUnavailableException
import kotlinx.coroutines.CancellationException

private val log = Logger.withTag("TokenizePendingReadings")

/**
 * Gives the readings that were stored without tokens their tokens, as far as a tokenizer is
 * available for their language. Readings that can't be tokenized yet stay pending. Safe to run any
 * number of times: saving the tokens of a reading that already has them does nothing.
 */
class TokenizePendingReadingsUseCase(
  private val repository: ReadingRepository,
  private val tokenizers: TokenizerProvider,
) {
  /** Returns how many readings got their tokens. */
  suspend fun execute(): Int {
    var done = 0
    for (reading in repository.getPendingReadings()) {
      val tokenizer = tokenizers.tokenizerFor(reading.sourceLanguage) ?: continue
      try {
        val analysis = TokenAnalysis(tokenizer.tokenize(reading.text), tokenizer.id)
        if (repository.saveAnalysis(reading.id, analysis)) done++
      } catch (e: CancellationException) {
        throw e
      } catch (e: TokenizerUnavailableException) {
        // It can't answer right now, so the rest would fail too. The next trigger tries again.
        log.w(e) { "Tokenizer '${tokenizer.id}' is unavailable; leaving the readings pending" }
        return done
      } catch (e: Exception) {
        // One reading that can't be tokenized must not block the others.
        log.e(e) { "Could not tokenize reading ${reading.id}" }
      }
    }
    return done
  }
}
