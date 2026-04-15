package com.akrippner.mon.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    onDark: Boolean = false
) {
    Text(
        text = text,
        style = MonTheme.typography.titleMedium.copy(
            color = if (onDark) Color.White.copy(alpha = 0.35f) else MonTheme.colors.textSecondary,
        ),
        modifier = modifier,
    )
}