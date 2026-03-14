package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.component.SectionLabel
import com.akrippner.mon.core.designsystem.theme.YomuType
import com.akrippner.mon.core.ui.Sense

@Composable
fun SenseBlock(
    index: Int,
    sense: Sense,
    modifier: Modifier = Modifier,
) {
    Column (modifier = modifier) {
        SectionLabel(
            text = "MEANING $index",
            onDark = true
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = sense.glosses.joinToString("; "),
            style = YomuType.Definition
            )
        if (sense.usageNote != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = sense.usageNote,
                style = YomuType.Meta.copy(color = Color.White.copy(alpha = 0.45f))
            )
        }
    }
}