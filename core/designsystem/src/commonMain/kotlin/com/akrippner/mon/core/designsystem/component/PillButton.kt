package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.theme.MonTheme


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
        !enabled -> MonTheme.colors.textPrimary.copy(alpha = 0.08f)
        variant == PillVariant.Filled -> MonTheme.colors.secondary
        else -> Color.Transparent

    }
    val labelColor = when {
        !enabled -> MonTheme.colors.textPrimary.copy( alpha = 0.25f)
        variant == PillVariant.Filled -> Color.White
        else -> MonTheme.colors.textSecondary
    }
    val borderColor = when {
        variant == PillVariant.Outline -> MonTheme.colors.textPrimary.copy(alpha = 0.25f)
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
                style = MonTheme.typography.bodyMedium.copy(
                    fontSize = 11.sp,
                    color = labelColor,
                )
            )
            if (sublabel != null) {
                Text(
                    text = sublabel,
                    style = MonTheme.typography.bodyMedium.copy(
                        fontSize = 8.sp,
                        color = labelColor.copy(alpha = 0.70f),
                    )
                )
            }
        }
    }

}


@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewPillButtons() {
    MonTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            YomuPillButton(label = "読む", variant = PillVariant.Filled, onClick = {})
            YomuPillButton(label = "保存", sublabel = "Save", variant = PillVariant.Filled, onClick = {})
            YomuPillButton(label = "Cancel", variant = PillVariant.Outline, onClick = {})
        }
    }
}
