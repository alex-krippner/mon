package com.akrippner.mon.core.domain.usecase

import co.touchlab.kermit.Logger
import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.domain.model.AddReadingInput
import com.akrippner.mon.core.domain.model.AddReadingInputError
import com.akrippner.mon.core.domain.model.AddReadingResult
import com.akrippner.mon.core.model.data.TokenAnalysis
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import com.akrippner.mon.core.tokenizer.TokenizerUnavailableException
import kotlinx.coroutines.CancellationException

private val log = Logger.withTag("AddReadingUseCase")

/**
 * Validates a new reading, tokenizes its text and stores both. If there is no tokenizer for the
 * reading's language yet, or it fails, the reading is stored without tokens and
 * [TokenizePendingReadingsUseCase] tokenizes it once one is available. [requestTokenization] asks
 * for that run right after such a reading is stored, so a tokenizer that appeared in the meantime
 * is not missed.
 */
class AddReadingUseCase(
  private val repository: ReadingRepository,
  private val tokenizers: TokenizerProvider,
  private val requestTokenization: () -> Unit,
) {
  suspend fun execute(input: AddReadingInput): AddReadingResult {
    val title = input.title.trim()
    val text = input.text.trim()
    val translation = input.translation.trim()

    val errors = buildSet {
      if (title.isEmpty()) add(AddReadingInputError.MissingTitle)
      if (text.isEmpty()) add(AddReadingInputError.MissingText)
    }
    if (errors.isNotEmpty()) return AddReadingResult.Invalid(errors)

    val analysis = analyse(input, text)

    val readingId =
      repository.addReading(
        title = title,
        text = text,
        translation = translation,
        sourceLanguage = input.sourceLanguage,
        translationLanguage = input.translationLanguage,
        analysis = analysis,
      )
    if (analysis == null) requestTokenization()
    return AddReadingResult.Success(readingId, tokenized = analysis != null)
  }

  /** Null when there is no tokenizer for the language, or it can't answer or fails. */
  private suspend fun analyse(input: AddReadingInput, text: String): TokenAnalysis? =
    try {
      val tokenizer = tokenizers.tokenizerFor(input.sourceLanguage)
      tokenizer?.let { TokenAnalysis(it.tokenize(text), it.id) }
    } catch (e: CancellationException) {
      throw e
    } catch (e: TokenizerUnavailableException) {
      log.w(e) { "Tokenizer is unavailable; the reading is stored without tokens" }
      null
    } catch (e: Exception) {
      // A downloaded dictionary that can't be loaded must not make saving impossible.
      log.e(e) { "Tokenizing failed; the reading is stored without tokens" }
      null
    }
}
