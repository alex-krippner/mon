package com.example.mon

import androidx.compose.runtime.Composable
import com.example.mon.core.ui.theme.MonTheme
import com.example.mon.feature.home.HomeScreen
import com.example.mon.feature.home.HomeViewModel
import org.koin.compose.koinInject

@Composable
fun App() {
    MonTheme {
        val viewModel: HomeViewModel = koinInject()
        HomeScreen(viewModel)
    }
}
