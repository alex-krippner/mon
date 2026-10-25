package com.akrippner.mon.feature.addreading

import androidx.compose.runtime.Immutable
import com.akrippner.mon.core.model.data.Language

@Immutable
data class AddReadingState(
  val title: String = "",
  val text: String = "",
  val translation: String = "",
  val sourceLanguage: Language = Language.JAPANESE,
  val translationLanguage: Language = Language.ENGLISH,
  val isSaving: Boolean = false,
  val error: AddReadingError? = null,
  /** Set once the reading is stored, so the screen can navigate away. */
  val savedReadingId: String? = null,
) {
  val canSave: Boolean
    get() = title.isNotBlank() && text.isNotBlank() && !isSaving && savedReadingId == null
}

enum class AddReadingError {
  MissingTitle,
  MissingText,
  SaveFailed,
}

sealed interface AddReadingIntent {
  data class TitleChanged(val value: String) : AddReadingIntent

  data class TextChanged(val value: String) : AddReadingIntent

  data class TranslationChanged(val value: String) : AddReadingIntent

  data object Save : AddReadingIntent
}
