package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun YomuFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tooltip: String = "Import text",
) {
    FloatingActionButton(
        onClick = onClick,
        shape = CircleShape,
        containerColor = YomuColors.Vermilion,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 6.dp,
            pressedElevation = 12.dp
        ),
    ) {
        Text(
            text = "+",
            style = YomuType.PageTitle.copy(
                color = Color.White,
                fontSize = 28.sp,
                letterSpacing = 0.sp,
                fontWeight = FontWeight.Light
            )
        )
    }
    Spacer(Modifier.height(8.dp))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(YomuColors.Ink.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
    {
        Text(
            text = tooltip,
            style = YomuType.NavLabel.copy(
                color = YomuColors.InkSubtle,
                fontSize = 9.sp,
                letterSpacing = 1.sp
            ),
        )
    }
}
