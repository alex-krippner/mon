package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
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
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun YomuPillButton(
    label: String,
    variant: PillVariant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    sublabel: String? = null,
    enabled: Boolean = true
) {
    val bgColor = when {
        !enabled -> YomuColors.Ink.copy(alpha = 0.08f)
        variant == PillVariant.Filled -> YomuColors.Vermilion
        else -> Color.Transparent

    }
    val labelColor = when {
        !enabled -> YomuColors.InkDisabled
        variant == PillVariant.Filled -> Color.White
        else -> YomuColors.InkSubtle
    }
    val borderColor = when {
        variant == PillVariant.Outline -> YomuColors.Ink.copy(alpha = 0.25f)
        else -> Color.Transparent
    }
    val shape = RoundedCornerShape(if (sublabel !== null) 14.dp else 15.dp)
    val height = if (sublabel != null) 36.dp else 30.dp

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(bgColor)
            .border(1.5.dp, borderColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                style = YomuType.Badge.copy(
                    fontSize = 11.sp,
                    color = labelColor,
                )
            )
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    style = YomuType.NavLabel.copy(
                        fontSize = 8.sp,
                        color = labelColor.copy(alpha = 0.70f),
                    )
                )
            }
        }
    }

}