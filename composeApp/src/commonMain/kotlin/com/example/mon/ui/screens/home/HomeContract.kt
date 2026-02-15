package com.example.mon.ui.screens.home

import com.example.mon.ui.viewmodel.UiEffect
import com.example.mon.ui.viewmodel.UiState

data class HomeState(
    val greeting: String = "Hello, World!"
) : UiState

sealed interface HomeEffect : UiEffect