package com.akrippner.mon.core.domain.model

sealed interface AddReadingResult {
  /**
   * [tokenized] is false when no tokenizer was available: the reading is stored without tokens and
   * gets them later.
   */
  data class Success(val readingId: String, val tokenized: Boolean) : AddReadingResult

  data class Invalid(val errors: Set<AddReadingInputError>) : AddReadingResult
}

enum class AddReadingInputError {
  MissingTitle,
  MissingText,
}
