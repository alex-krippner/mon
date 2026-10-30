package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.data.ReadingRepository
import com.akrippner.mon.core.domain.model.AddReadingInput
import com.akrippner.mon.core.domain.model.AddReadingInputError
import com.akrippner.mon.core.domain.model.AddReadingResult
import com.akrippner.mon.core.tokenizer.Tokenizer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Validates a new reading, tokenizes its text and stores both. */
class AddReadingUseCase(
  private val repository: ReadingRepository,
  private val tokenizer: Tokenizer,
  private val tokenizeDispatcher: CoroutineDispatcher = Dispatchers.Default,
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

    val tokens = withContext(tokenizeDispatcher) { tokenizer.tokenize(text) }

    val readingId =
      repository.addReading(
        title = title,
        text = text,
        translation = translation,
        sourceLanguage = input.sourceLanguage,
        translationLanguage = input.translationLanguage,
        tokens = tokens,
      )
    return AddReadingResult.Success(readingId)
  }
}
