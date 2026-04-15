package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun YomuIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(MonTheme.colors.backgroundSecondary.copy(alpha = 0.05f)) //TODO: Define universal alphas
            .clickable(
                interactionSource = remember { MutableInteractionSource() }, //TODO: Check why interactionSource is set here
                indication = ripple(bounded = true, radius = 18.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}