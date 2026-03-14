package com.akrippner.mon.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.ProgressVariant
import com.akrippner.mon.core.designsystem.component.ReadingProgressBar
import com.akrippner.mon.core.designsystem.component.YomuPillButton
import com.akrippner.mon.core.designsystem.theme.GeorgiaFamily
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun FeaturedBookCard(
    title: String,
    author: String,
    progress: Float,
    annotationCount: Int,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
    watermarkKanji: String = "心",
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(152.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(YomuColors.CardDark1, YomuColors.CardDark2)
                )
            )
            .clickable(onClick = onOpen)
    ) {
        Text(
            text = watermarkKanji,
            style = TextStyle(
                fontFamily = GeorgiaFamily,
                fontSize = 110.sp,
                color = Color.White.copy(0.04f)
            ),
            modifier = Modifier.align(Alignment.BottomEnd).padding(end = 8.dp, bottom = 0.dp)

        )
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        )
        {
            Column {
                Text(text = title, style = YomuType.CardTitle)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = author,
                    style = YomuType.Meta.copy(color = Color.White.copy(alpha = 0.60f))
                )
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ){
                Column(modifier = Modifier.weight(1f)) {
                    ReadingProgressBar(
                        progress = progress,
                        variant = ProgressVariant.Card,
                        modifier = Modifier.fillMaxWidth(0.85f)
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${(progress * 100).toInt()}% · $annotationCount annotations",
                        style = YomuType.Meta.copy(color = Color.White.copy(alpha = 0.40f))
                    )
                }
                YomuPillButton(
                    label = "読む",
                    variant = PillVariant.Filled,
                    onClick = onOpen,
                )
            }
        }
    }
}
