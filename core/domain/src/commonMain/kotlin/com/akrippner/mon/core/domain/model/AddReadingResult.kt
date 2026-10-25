package com.akrippner.mon.core.domain.model

sealed interface AddReadingResult {
  data class Success(val readingId: String) : AddReadingResult

  data class Invalid(val errors: Set<AddReadingInputError>) : AddReadingResult
}

enum class AddReadingInputError {
  MissingTitle,
  MissingText,
}
