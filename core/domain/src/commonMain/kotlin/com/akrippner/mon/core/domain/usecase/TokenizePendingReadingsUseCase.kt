package com.akrippner.mon.core.domain.usecase

import co.touchlab.kermit.Logger
import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.model.data.Language
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
    val unavailable = mutableSetOf<Language>()
    for ((id, text, language) in repository.getPendingReadings()) {
      if (language in unavailable) continue
      try {
        val tokenizer = tokenizers.tokenizerFor(language) ?: continue
        val analysis = TokenAnalysis(tokenizer.tokenize(text), tokenizer.id)
        if (repository.saveAnalysis(id, analysis)) done++
      } catch (e: CancellationException) {
        throw e
      } catch (e: TokenizerUnavailableException) {
        // It can't answer right now, so the other readings of its language would fail too. The
        // readings of other languages go on. The next trigger tries again.
        log.w(e) { "No tokenizer for $language; leaving its readings pending" }
        unavailable += language
      } catch (e: Exception) {
        // One reading that can't be tokenized must not block the others.
        log.e(e) { "Could not tokenize reading $id" }
      }
    }
    return done
  }
}
