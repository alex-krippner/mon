package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.ProgressVariant
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun TextListItem(
    title: String,
    meta: String,
    progress: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = if (progress > 0f)
        YomuColors.Vermilion.copy(alpha = 0.55f)
    else
        YomuColors.Ink.copy(alpha = 0.16f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(YomuColors.Ink.copy(alpha = 0.04f))
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min)
    ) {
        AccentBar(color = accentColor)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(text = title, style = YomuType.ListItemTitle)
            Spacer(Modifier.height(4.dp))
            Text(text = meta, style = YomuType.Meta)
            Spacer(Modifier.height(6.dp))
            ReadingProgressBar(progress = progress, variant = ProgressVariant.Mini)
        }
        Text(
            text = ",",
            style = YomuType.Body.copy(
                color = YomuColors.Ink.copy(alpha = 0.15f),
                fontSize = 20.sp
            ),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = 12.dp)
        )
    }
}
