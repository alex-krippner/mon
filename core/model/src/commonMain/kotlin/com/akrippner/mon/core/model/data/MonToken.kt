package com.akrippner.mon.core.model.data

data class MonToken(
  val surface: String,
  val dictionaryForm: String,
  val pronunciation: String,
  val partOfSpeech: List<String>,
)
