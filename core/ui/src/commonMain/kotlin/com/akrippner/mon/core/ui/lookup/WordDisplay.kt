package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WordDisplay(
    surface: String,
    reading: String,
    modifier: Modifier = Modifier
) {
    Column (modifier = modifier) {
        Text(text = surface, style = MonTheme.typography.bodyMedium, color = MonTheme.colors.sheetTextPrimary )
        Spacer(Modifier.height(4.dp))
        Text(text = reading, style = MonTheme.typography.bodyMedium)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF1C1C2E)
@Composable
private fun PreviewWordDisplay() {
    MonTheme{
        WordDisplay(
            surface = "先生",
            reading = "せんせい",
            modifier = Modifier.padding(24.dp),
        )
    }
}
