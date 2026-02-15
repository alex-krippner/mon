package com.example.mon

import androidx.compose.runtime.Composable
import com.example.mon.ui.screens.home.HomeScreen
import com.example.mon.ui.screens.home.HomeViewModel
import com.example.mon.ui.theme.MonTheme
import org.koin.compose.koinInject

@Composable
fun App() {
    MonTheme {
        val viewModel: HomeViewModel = koinInject()
        HomeScreen(viewModel)
    }
}
