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
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

enum class FilterPillStyle {
    InkFilled,
    VermilionOutline
}

@Composable
fun FilterPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeStyle: FilterPillStyle,
    modifier: Modifier = Modifier,
) {
    val bgColor = when {
        !selected -> YomuColors.Ink.copy(alpha = 0.06f)
        activeStyle == FilterPillStyle.InkFilled -> YomuColors.Ink
        else -> YomuColors.Vermilion.copy(alpha = 0.15f)

    }
    val textColor = when {
        !selected -> YomuColors.InkSubtle
        activeStyle == FilterPillStyle.InkFilled -> Color.White
        else -> YomuColors.Vermilion
    }
    val borderColor = when {
        selected && activeStyle == FilterPillStyle.VermilionOutline -> YomuColors.Vermilion.copy(
            alpha = 0.35f
        )
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(11.dp))
            .clickable(
                interactionSource = remember{ MutableInteractionSource() }, //TODO: Check why interactionSource is set here
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = YomuType.Badge.copy(color = textColor)
        )
    }
}