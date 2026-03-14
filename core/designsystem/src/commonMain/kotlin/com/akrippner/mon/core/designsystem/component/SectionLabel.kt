package com.akrippner.mon.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    onDark: Boolean = false
) {
    Text(
        text = text,
        style = YomuType.SectionLabel.copy(
            color = if (onDark) Color.White.copy(alpha = 0.35f) else YomuColors.InkSubtle,
        ),
        modifier = modifier,
    )
}