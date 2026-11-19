package com.akrippner.mon.core.domain.usecase

import co.touchlab.kermit.Logger
import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.domain.model.AddReadingInput
import com.akrippner.mon.core.domain.model.AddReadingInputError
import com.akrippner.mon.core.domain.model.AddReadingResult
import com.akrippner.mon.core.model.data.TokenAnalysis
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import com.akrippner.mon.core.tokenizer.TokenizerUnavailableException

private val log = Logger.withTag("AddReadingUseCase")

/**
 * Validates a new reading, tokenizes its text and stores both. If there is no tokenizer for the
 * reading's language yet, the reading is stored without tokens and [TokenizePendingReadingsUseCase]
 * tokenizes it once one is available.
 */
class AddReadingUseCase(
  private val repository: ReadingRepository,
  private val tokenizers: TokenizerProvider,
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
    return AddReadingResult.Success(readingId, tokenized = analysis != null)
  }

  /** Null when there is no tokenizer for the language, or it can't answer right now. */
  private suspend fun analyse(input: AddReadingInput, text: String): TokenAnalysis? {
    val tokenizer = tokenizers.tokenizerFor(input.sourceLanguage) ?: return null
    return try {
      TokenAnalysis(tokenizer.tokenize(text), tokenizer.id)
    } catch (e: TokenizerUnavailableException) {
      log.w(e) {
        "Tokenizer '${tokenizer.id}' is unavailable; the reading is stored without tokens"
      }
      null
    }
  }
}
