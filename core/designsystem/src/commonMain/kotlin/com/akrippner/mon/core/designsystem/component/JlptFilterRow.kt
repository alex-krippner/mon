package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun JlptFilterRow(
  selected: String?,
  onSelect: (String?) -> Unit,
  modifier: Modifier = Modifier,
  levels: List<String> = listOf("N5", "N4", "N3", "N2", "N1"),
) {
  MonTheme.colors.listItem.background
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FilterPill(label = "All", selected = selected == null, onClick = { onSelect(null) })

    levels.forEach { level ->
      FilterPill(label = level, selected = selected == level, onClick = { onSelect(level) })
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewJlptFilterRow() {
  MonTheme { JlptFilterRow(selected = "N5", onSelect = {}, modifier = Modifier.padding(16.dp)) }
}
