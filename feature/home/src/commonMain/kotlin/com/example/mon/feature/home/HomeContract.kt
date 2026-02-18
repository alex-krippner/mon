package com.example.mon.feature.home

import com.example.mon.core.model.data.MyToken
import com.example.mon.feature.home.viewmodel.UiEffect
import com.example.mon.feature.home.viewmodel.UiState

data class HomeState(
    val tokens: List<MyToken> = emptyList()
) : UiState

sealed interface HomeEffect : UiEffect