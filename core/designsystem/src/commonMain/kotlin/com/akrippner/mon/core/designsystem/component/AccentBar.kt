package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AccentBar(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(3.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
            .background(color)
    )
}