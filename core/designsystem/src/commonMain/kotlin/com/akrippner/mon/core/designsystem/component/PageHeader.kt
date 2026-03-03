package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun MonPageHeader(
    title: String, subtitle: String, modifier: Modifier = Modifier
) {

    Column {
        Row(modifier = modifier) {
            Text(text = title)
            Text(text = subtitle)
        }
    }
}

@Preview
@Composable
fun MonPageHeaderPreview() {
    MonPageHeader(title = "Title", subtitle = "Subtitle")
}
