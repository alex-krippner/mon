package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun FuriganaToggle(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor =
        if (enabled) MonTheme.colors.furiganaToggle.background else MonTheme.colors.furiganaToggle.disabled

    Button(
        colors = ButtonColors(
            containerColor = containerColor,
            disabledContainerColor = MonTheme.colors.furiganaToggle.disabled,
            contentColor = MonTheme.colors.furiganaToggle.icon,
            disabledContentColor = MonTheme.colors.furiganaToggle.icon
        ), onClick = onToggle,
        modifier = modifier
    ) {
        MonIcon(
            id = Icon.Core.Furigana,
            size = IconSize.ExtraSmall,
            primaryColor = MonTheme.colors.furiganaToggle.icon
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewFuriganaToggle() {
    MonTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            FuriganaToggle(enabled = true, onToggle = {})
            FuriganaToggle(enabled = false, onToggle = {})
        }
    }
}