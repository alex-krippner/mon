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

/**
 * The JLPT level filter. Several levels can be selected at once; "All" stands for none selected,
 * and tapping it clears the selection.
 */
@Composable
fun JlptFilterRow(
  selected: Set<String>,
  onToggle: (String) -> Unit,
  onClear: () -> Unit,
  modifier: Modifier = Modifier,
  allLabel: String = "All",
  levels: List<String> = listOf("N5", "N4", "N3", "N2", "N1"),
) {
  Row(
    modifier = modifier,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FilterPill(label = allLabel, selected = selected.isEmpty(), onClick = onClear)

    levels.forEach { level ->
      FilterPill(label = level, selected = level in selected, onClick = { onToggle(level) })
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewJlptFilterRow() {
  MonTheme {
    JlptFilterRow(
      selected = setOf("N5", "N4"),
      onToggle = {},
      onClear = {},
      modifier = Modifier.padding(16.dp),
    )
  }
}
