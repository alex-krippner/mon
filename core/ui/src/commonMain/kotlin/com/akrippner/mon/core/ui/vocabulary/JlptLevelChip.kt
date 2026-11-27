package com.akrippner.mon.core.ui.vocabulary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.core.model.data.JlptLevel

/** Where a [JlptLevelChip] is drawn: the design has a smaller chip on a card than on a sheet. */
enum class JlptChipSize {
  Card,
  Sheet,
}

/** The JLPT level of a saved word, on the paper colour. Callers leave it out for a null level. */
@Composable
fun JlptLevelChip(
  level: JlptLevel,
  modifier: Modifier = Modifier,
  size: JlptChipSize = JlptChipSize.Card,
) {
  val (width, height) =
    when (size) {
      JlptChipSize.Card -> 32.dp to 16.dp
      JlptChipSize.Sheet -> 36.dp to 20.dp
    }
  Box(
    modifier =
      modifier
        .size(width, height)
        .clip(RoundedCornerShape(4.dp))
        .background(MonTheme.colors.surfacePrimary),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = level.name,
      style =
        MonTheme.typography.labelSmall.copy(
          color = MonTheme.colors.inkSoft,
          fontWeight = if (size == JlptChipSize.Sheet) FontWeight.Bold else FontWeight.Medium,
        ),
    )
  }
}
