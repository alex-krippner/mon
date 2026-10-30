package com.akrippner.mon.core.database.entity

data class ReadingSummaryRow(
  val id: String,
  val title: String,
  val sourceLanguage: String,
  val translationLanguage: String,
  val createdAt: Long,
  val tokenCount: Int,
)
