package com.akrippner.mon.feature.home

import com.akrippner.mon.core.tokenizer.Token

data class HomeState(val tokens: List<Token> = emptyList())

sealed interface HomeIntent
