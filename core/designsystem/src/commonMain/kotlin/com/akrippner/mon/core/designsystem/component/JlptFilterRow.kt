package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun JlptFilterRow(
    selected: String?,
    onSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
    levels: List<String> = listOf("N5", "N4", "N3", "N2", "N1")
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterPill(
            label = "All",
            selected = selected == null,
            onClick = { onSelect(null) },
            activeStyle = FilterPillStyle.InkFilled

        )
        levels.forEach { level ->
            FilterPill(
                label = level,
                selected = selected == level,
                onClick = { onSelect(level) },
                activeStyle = FilterPillStyle.VermilionOutline
            )
        }
    }

}