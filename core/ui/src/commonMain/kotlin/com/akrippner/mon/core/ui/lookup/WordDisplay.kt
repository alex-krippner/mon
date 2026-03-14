package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun WordDisplay(
    surface: String,
    reading: String,
    modifier: Modifier = Modifier
) {
    Column (modifier = modifier) {
        Text(text = surface, style = YomuType.WordDisplay)
        Spacer(Modifier.height(4.dp))
        Text(text = reading, style = YomuType.Reading)
    }
}