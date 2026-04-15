package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.component.SectionLabel
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.ui.JmdictEntry
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
            style = MonTheme.typography.bodyMedium
            )
        if (sense.usageNote != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = sense.usageNote,
                style = MonTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.45f))
            )
        }
    }
}
private val sampleJmdictEntry = JmdictEntry(
    surface = "先生",
    reading = "せんせい",
    jlptLevel = "N5",
    senses = listOf(
        Sense(
            posLabel = "名詞",
            glosses = listOf("teacher", "master", "doctor"),
            usageNote = "Used as a form of address",
        ),
        Sense(
            posLabel = "名詞",
            glosses = listOf("previous existence"),
        ),
    ),
)

@Preview(showBackground = true, backgroundColor = 0xFF1C1C2E)
@Composable
private fun PreviewSenseBlock() {
    MonTheme {
        SenseBlock(
            index = 1,
            sense = sampleJmdictEntry.senses.first(),
            modifier = Modifier.padding(24.dp),
        )
    }
}
