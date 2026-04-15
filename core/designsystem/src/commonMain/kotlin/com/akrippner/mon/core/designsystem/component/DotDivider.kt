package com.akrippner.mon.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun DotDivider(style: TextStyle?, modifier: Modifier = Modifier) {
    Text(
        text = "·",
        style = style ?: MonTheme.typography.bodySmall,
        modifier = modifier
    )
}