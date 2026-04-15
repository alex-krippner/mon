package com.akrippner.mon.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.component.DotDivider
import com.akrippner.mon.core.designsystem.theme.MonTheme

//TODO: Move to library feature
@Composable
fun FeaturedEntry(
    title: String,
    wordCount: Int,
    vocabCount: Int,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                MonTheme.colors.featured.background
            )
            .clickable(onClick = onOpen)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        )
        {
            Column {
                Text(
                    text = title,
                    style = MonTheme.typography.headlineSmall,
                    color = MonTheme.colors.featured.title
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    Text(
                        text = "$wordCount words",
                        style = MonTheme.typography.labelSmall,
                        color = MonTheme.colors.featured.subtitle
                    )
                    Spacer(Modifier.width(2.dp))
                    DotDivider(style = MonTheme.typography.labelSmall.copy(color = MonTheme.colors.featured.subtitle))
                    Text(
                        text = "$vocabCount vocabulary",
                        style = MonTheme.typography.labelSmall,
                        color = MonTheme.colors.featured.subtitle
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF7F2E8)
@Composable
private fun PreviewFeaturedEntry() {
    MonTheme {
        FeaturedEntry(
            title = "こころ",
            wordCount = 25,
            vocabCount = 12,
            onOpen = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
