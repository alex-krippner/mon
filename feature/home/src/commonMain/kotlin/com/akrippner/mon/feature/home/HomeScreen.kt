package com.akrippner.mon.feature.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun HomeScreen(viewModel: HomeViewModel) {

  Text(text = "Home", style = MonTheme.typography.bodyMedium, color = MonTheme.colors.textSecondary)
}
