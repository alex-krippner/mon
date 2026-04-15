package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun TextListItem(
    title: String,
    meta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MonTheme.colors.listItem.background)
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = title, style = MonTheme.typography.titleSmall, color = MonTheme.colors.listItem.title)
            Text(text = meta, style = MonTheme.typography.labelSmall, color = MonTheme.colors.listItem.subtitle)
        }

        MonIcon(id = Icon.Core.ChevronRight, size = IconSize.ExtraSmall, primaryColor = MonTheme.colors.listItem.iconEnd)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewTextListItem() {
    MonTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextListItem(
                title = "NHK ニュース記事",
                meta = "89 words · 3 annotations",
                onClick = {},
            )
            TextListItem(
                title = "吾輩は猫である",
                meta = "512 words · not started",
                onClick = {},
            )
        }
    }
}