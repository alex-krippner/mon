package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.component.JlptBadge
import com.akrippner.mon.core.designsystem.theme.MonTheme

/**
 * The headword of an entry: large, with its reading below and the JLPT level beside it when the
 * dictionary has one.
 */
@Composable
fun WordDisplay(
  surface: String,
  reading: String,
  modifier: Modifier = Modifier,
  jlptLevel: String? = null,
) {
  Column(modifier = modifier) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = surface,
        style = MonTheme.typography.displaySmall,
        color = MonTheme.colors.lookup.title,
      )
      if (jlptLevel != null) JlptBadge(level = jlptLevel)
    }
    if (reading.isNotBlank()) {
      Spacer(Modifier.height(4.dp))
      Text(
        text = reading,
        style = MonTheme.typography.labelMedium,
        color = MonTheme.colors.lookup.reading,
      )
    }
  }
}

@Preview(showBackground = true, backgroundColor = 0xFF343449)
@Composable
private fun PreviewWordDisplay() {
  MonTheme {
    WordDisplay(
      surface = "先生",
      reading = "せんせい",
      jlptLevel = "N5",
      modifier = Modifier.padding(24.dp),
    )
  }
}
