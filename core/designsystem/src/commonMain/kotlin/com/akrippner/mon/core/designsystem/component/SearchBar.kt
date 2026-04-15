package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun MonSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search words...",
    startIcon: @Composable (() -> Unit)? = null,
    endIcon: @Composable (() -> Unit)? = null
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = MonTheme.typography.bodyMedium.copy(color = MonTheme.colors.textPrimary),
        cursorBrush = SolidColor(MonTheme.colors.input.cursor),
        singleLine = true,

        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MonTheme.colors.input.background)
            .border(1.dp, MonTheme.colors.input.border, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                startIcon?.let { startIcon() }
                Spacer(Modifier.width(8.dp))
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MonTheme.typography.labelMedium.copy(color = MonTheme.colors.input.placeholder),
                    )
                }
                innerTextField()
                endIcon?.let { endIcon() }
            }
        }

    )
}

@Preview
@Composable
private fun MonSearchBarPreview() {
    MonTheme {
        MonSearchBar(
            query = "",
            onQueryChange = {},
            startIcon = {
                MonIcon(
                    id = Icon.Core.Search,
                    primaryColor = MonTheme.colors.input.placeholder,
                    size = IconSize.ExtraSmall
                )
            }
        )
    }
}