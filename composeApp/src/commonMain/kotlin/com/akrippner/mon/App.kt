package com.akrippner.mon

import androidx.compose.runtime.Composable
import com.akrippner.mon.core.designsystem.theme.YomuTheme
import com.akrippner.mon.feature.home.HomeScreen
import com.akrippner.mon.feature.home.HomeViewModel
import org.koin.compose.koinInject

@Composable
fun App() {
    YomuTheme {
        val viewModel: HomeViewModel = koinInject()
        HomeScreen(viewModel)
    }
}
