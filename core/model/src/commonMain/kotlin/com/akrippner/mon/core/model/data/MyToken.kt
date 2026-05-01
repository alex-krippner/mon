package com.akrippner.mon.core.model.data

data class MyToken(
  val surface: String,
  val partOfSpeech: List<String>,
  val normalizedForm: String,
  val reading: String,
)
