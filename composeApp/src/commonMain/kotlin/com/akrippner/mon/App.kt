package com.akrippner.mon

import androidx.compose.runtime.Composable
import com.akrippner.mon.core.ui.theme.MonTheme
import com.akrippner.mon.feature.home.HomeScreen
import com.akrippner.mon.feature.home.HomeViewModel
import org.koin.compose.koinInject

@Composable
fun App() {
    MonTheme {
        val viewModel: HomeViewModel = koinInject()
        HomeScreen(viewModel)
    }
}
