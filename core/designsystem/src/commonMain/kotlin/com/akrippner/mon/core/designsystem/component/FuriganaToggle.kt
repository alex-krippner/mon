package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.GeorgiaFamily
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun FuriganaToggle(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (enabled) YomuColors.Ink
                else YomuColors.Ink.copy(alpha = 0.06f)
            )
            .border(
                1.dp,
                if (enabled) Color.Transparent else YomuColors.Ink.copy(alpha = 0.13f),
                RoundedCornerShape(11.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, //TODO: Check why interactionSource is set here
                indication = ripple(bounded = true),
                onClick = onToggle
            )
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ふ",
            style = YomuType.Badge.copy(
                fontFamily = GeorgiaFamily,
                fontSize = 10.sp,
                color = if (enabled) Color.White else YomuColors.InkSubtle
            )
        )
    }
}