package com.akrippner.mon.core.model.data

data class Reading(
  val id: String,
  val title: String,
  val text: String,
  val translation: String,
  val sourceLanguage: Language,
  val translationLanguage: Language,
  val tokens: List<MonToken>,
  val createdAt: Long,
)
