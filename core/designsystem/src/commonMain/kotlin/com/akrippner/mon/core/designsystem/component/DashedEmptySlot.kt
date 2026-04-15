package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.dashedBorder
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun DashedEmptySlot(
    label: String,
    modifier: Modifier= Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .dashedBorder(
                color = MonTheme.colors.sheetBackground.copy(alpha = 0.12f),
                strokeWidth = 1.dp,
                dashedLength = 6.dp,
                gapLength = 4.dp,
                cornerRadius = 12.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MonTheme.typography.labelSmall.copy(
                color = MonTheme.colors.textPrimary.copy(alpha = 0.30f),
                letterSpacing = 1.sp
            )
        )
    }
}