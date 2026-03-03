package com.akrippner.mon.feature.home

import com.akrippner.mon.core.model.data.MyToken
import com.akrippner.mon.feature.home.viewmodel.UiEffect
import com.akrippner.mon.feature.home.viewmodel.UiState

data class HomeState(
    val tokens: List<MyToken> = emptyList()
) : UiState

sealed interface HomeEffect : UiEffect