package com.akrippner.mon.core.designsystem.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.GeorgiaFamily

@Composable
fun KanjiWaterMark(
    kanji: String,
    modifier: Modifier = Modifier,
    alpha: Float = 0.04f
) {
    Text(
        text = kanji,
        style = TextStyle(
            fontFamily = GeorgiaFamily,
            fontSize= 110.sp,
            color = Color.White.copy(alpha = alpha)
        ),
        modifier = modifier
    )
}