package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun AnnotationDot(
    modifier: Modifier = Modifier,
    size: Dp = 12.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(YomuColors.Vermilion), contentAlignment = Alignment.Center

    ) {
        Text(
            text = "✓",
            style = YomuType.NavLabel.copy(
                color = Color.White,
                fontSize = (size.value * 0.55f).sp,
            ),
            textAlign = TextAlign.Center,
        )
    }
}