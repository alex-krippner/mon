package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun KanjiDetailLink(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape((8.dp))),
        color = Color.White.copy(alpha = 0.05f),
        onClick = onClick

    ) {
        Text(
            text = "View kanji breakdown →",
            style = MonTheme.typography.bodyMedium.copy(
                color = Color.White.copy(alpha = 0.40f),
                letterSpacing = 1.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}
