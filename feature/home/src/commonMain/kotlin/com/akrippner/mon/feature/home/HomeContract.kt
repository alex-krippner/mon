package com.akrippner.mon.feature.home

import com.akrippner.mon.core.tokenizer.Token
import com.akrippner.mon.feature.home.viewmodel.UiEffect
import com.akrippner.mon.feature.home.viewmodel.UiState

data class HomeState(val tokens: List<Token> = emptyList()) : UiState

sealed interface HomeEffect : UiEffect
