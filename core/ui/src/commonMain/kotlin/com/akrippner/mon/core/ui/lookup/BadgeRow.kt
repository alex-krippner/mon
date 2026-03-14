package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.BadgeTheme
import com.akrippner.mon.core.designsystem.component.JlptBadge
import com.akrippner.mon.core.designsystem.component.PosBadge

@Composable
fun BadgeRow(
    jlptLevel: String?,
    pos: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (jlptLevel != null) {
            JlptBadge(level = jlptLevel, theme = BadgeTheme.Dark)
        }
        if (pos != null) {
            PosBadge(pos = pos)
        }
    }
}
