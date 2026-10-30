package com.akrippner.mon.core.model.data

data class ReadingSummary(
  val id: String,
  val title: String,
  val sourceLanguage: Language,
  val translationLanguage: Language,
  val tokenCount: Int,
  val createdAt: Long,
)
