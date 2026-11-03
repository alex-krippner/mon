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
  /** True once the reading is stored; guards against saving twice while the screen closes. */
  val isSaved: Boolean = false,
) {
  val canSave: Boolean
    get() = title.isNotBlank() && text.isNotBlank() && !isSaving && !isSaved
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

sealed interface AddReadingEffect {
  /** The reading was stored, so the host should leave the screen. */
  data class Saved(val readingId: String) : AddReadingEffect
}
