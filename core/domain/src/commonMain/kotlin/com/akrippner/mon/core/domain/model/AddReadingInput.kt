package com.akrippner.mon.core.domain.model

import com.akrippner.mon.core.model.data.Language

data class AddReadingInput(
  val title: String,
  val text: String,
  val translation: String,
  val sourceLanguage: Language,
  val translationLanguage: Language,
)
