package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.BadgeTheme
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun JlptBadge(
    level: String, //TODO: Create enum?
    theme: BadgeTheme,
    modifier: Modifier = Modifier,
) {
    val isHighlighted = level == "N5" //TODO: Why only highlight N5?
    val bgColor = when {
        theme == BadgeTheme.Dark && isHighlighted -> YomuColors.Vermilion.copy(alpha = 0.22f)
        theme == BadgeTheme.Dark -> Color.White.copy(alpha = 0.07f)
        isHighlighted -> YomuColors.Vermilion.copy(alpha = 0.15f)
        else -> YomuColors.Ink.copy(alpha = 0.06f)

    }
    val borderColor = when {
        theme == BadgeTheme.Dark && isHighlighted -> YomuColors.Vermilion.copy(alpha = 0.40f)
        else -> Color.Transparent
    }
    val textColor = when {
        isHighlighted -> YomuColors.Vermilion
        theme == BadgeTheme.Dark -> Color.White.copy(alpha = 0.50f)
        else -> YomuColors.InkSubtle
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = level, style = YomuType.Badge.copy(color = textColor))
    }
}