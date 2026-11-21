package com.akrippner.mon.core.model.data

data class Reading(
  val id: String,
  val title: String,
  val text: String,
  val translation: String,
  val sourceLanguage: Language,
  val translationLanguage: Language,
  val tokens: List<MonToken>,
  /** False while the reading waits for a tokenizer; [tokens] is empty then. */
  val isTokenized: Boolean,
  val createdAt: Long,
)
