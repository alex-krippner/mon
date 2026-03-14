package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun YomuPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    val bg = if (enabled) {
        Brush.horizontalGradient(
            colors = listOf(YomuColors.Vermilion, YomuColors.VermilionLight

        ))
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                YomuColors.Ink.copy(alpha = 0.13f),
                YomuColors.Ink.copy(alpha = 0.13f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (subtitle != null) 56.dp else 48.dp)
            .clip(RoundedCornerShape(16.dp)) //TODO: Define universal sizes
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column (horizontalAlignment = Alignment.CenterHorizontally){
            Text(
                text = label,
                style = YomuType.Body.copy(
                    fontSize = 15.sp,
                    color = if (enabled) Color.White else YomuColors.InkDisabled,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = YomuType.Meta.copy(
                        color = if (enabled) Color.White.copy(alpha = 0.65f)
                        else YomuColors.Ink.copy(alpha = 0.20f),
                        fontSize = 9.sp
                    )
                )
            }
        }
    }
}