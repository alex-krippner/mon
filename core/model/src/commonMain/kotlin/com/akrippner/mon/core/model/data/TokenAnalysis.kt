package com.akrippner.mon.core.model.data

/** The tokens of a reading and the tokenizer that produced them (see `Tokenizer.id`). */
data class TokenAnalysis(val tokens: List<MonToken>, val tokenizedWith: String)

/** A reading that is still waiting for its tokens. */
data class PendingReading(val id: String, val text: String, val sourceLanguage: Language)
