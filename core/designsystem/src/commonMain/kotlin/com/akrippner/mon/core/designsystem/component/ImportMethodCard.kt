package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.CardStyle
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun ImportMethodCard(
    label: String,
    description: String,
    style: CardStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    previewText: String? = null
) {
    val shape = RoundedCornerShape(16.dp)
    val bgModifier = when (style) {
        CardStyle.Primary -> modifier
            .clip(shape)
            .background(YomuColors.Ink)

        CardStyle.Secondary -> modifier
            .clip(shape)
            .background(YomuColors.Ink.copy(alpha = 0.05f))
            .border(1.dp, YomuColors.Ink.copy(alpha = 0.13f), shape)
    }
    val titleColor = if (style == CardStyle.Primary) Color.White else YomuColors.Ink
    val metaColor =
        if (style == CardStyle.Primary) Color.White.copy(alpha = 0.55f) else YomuColors.InkSubtle
    val chevronColor =
        if (style == CardStyle.Primary) Color.White.copy(alpha = 0.28f) else YomuColors.Ink.copy(
            alpha = 0.18f
        )

    Row(
        modifier = bgModifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = YomuType.Body.copy(color = titleColor, fontWeight = FontWeight.SemiBold))
            Spacer(Modifier.height(2.dp))
            Text(text = description, style = YomuType.Meta.copy(color = metaColor))
            if (previewText != null && style == CardStyle.Primary) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = previewText,
                    style = YomuType.Meta.copy(
                        color = Color.White.copy(alpha = 0.30f),
                        fontStyle = FontStyle.Italic,
                        fontSize = 9.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Text(
            text = "›",
            style = YomuType.Body.copy(color = chevronColor, fontSize = 20.sp),
        )
    }
}