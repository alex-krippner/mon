package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun JlptBadge(
    level: String, //TODO: Create enum?
    modifier: Modifier = Modifier,
) {


    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MonTheme.colors.tag.background)
            .border(1.dp, Color.Unspecified, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = level, style = MonTheme.typography.labelSmall.copy(color = MonTheme.colors.tag.text))
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewJlptBadgesLight() {
    MonTheme {
        Row (
            modifier = Modifier.padding(20.dp).background(MonTheme.colors.vocabEntry.background),
            horizontalArrangement = Arrangement.spacedBy(8.dp),

        ) {
            listOf("N5", "N4", "N3", "N2", "N1").forEach {
                JlptBadge(level = it)
            }
        }
    }
}
