package com.example.mon.domain.model

data class MyToken(
    val surface: String,
    val partOfSpeech: List<String>,
    val normalizedForm: String,
    val reading: String
)
