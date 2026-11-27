package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.ui.JmdictEntry
import com.akrippner.mon.core.ui.Sense

/** One numbered meaning: its part of speech above, then the glosses and an optional note. */
@Composable
fun SenseBlock(index: Int, sense: Sense, modifier: Modifier = Modifier) {
  val colors = MonTheme.colors.lookup
  Column(modifier = modifier) {
    if (sense.posLabel != null) {
      Text(
        text = sense.posLabel,
        style = MonTheme.typography.labelSmall,
        color = colors.partsOfSpeech,
      )
      Spacer(Modifier.height(4.dp))
    }
    Row {
      Text(
        text = "$index.",
        modifier = Modifier.width(NUMBER_WIDTH),
        style = MonTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = colors.title,
      )
      Column {
        Text(
          text = sense.glosses.joinToString("; "),
          style = MonTheme.typography.bodyMedium,
          color = colors.definition,
        )
        if (sense.usageNote != null) {
          Spacer(Modifier.height(8.dp))
          Text(
            text = sense.usageNote,
            style = MonTheme.typography.bodySmall,
            color = colors.example,
          )
        }
      }
    }
  }
}

/** The width of the sense number, so wrapped glosses line up under the first line. */
private val NUMBER_WIDTH = 28.dp

private val sampleJmdictEntry =
  JmdictEntry(
    surface = "先生",
    reading = "せんせい",
    jlptLevel = "N5",
    senses =
      listOf(
        Sense(
          posLabel = "Noun",
          glosses = listOf("teacher", "master", "doctor"),
          usageNote = "Used as a form of address",
        ),
        Sense(
          posLabel = "Noun, used as a suffix",
          glosses = listOf("sensei; title or form of address"),
        ),
      ),
  )

@Preview(showBackground = true, backgroundColor = 0xFF343449)
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
