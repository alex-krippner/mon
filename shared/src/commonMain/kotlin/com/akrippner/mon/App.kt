package com.akrippner.mon

import androidx.compose.runtime.Composable
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.navigation.MonNavHost

@Composable
fun App() {
  MonTheme { MonNavHost() }
}
