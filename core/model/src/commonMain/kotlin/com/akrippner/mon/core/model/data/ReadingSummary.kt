package com.akrippner.mon.core.model.data

data class ReadingSummary(
  val id: String,
  val title: String,
  val sourceLanguage: Language,
  val translationLanguage: Language,
  val tokenCount: Int,
  /** False while the reading waits for a tokenizer. */
  val isTokenized: Boolean,
  val createdAt: Long,
)
