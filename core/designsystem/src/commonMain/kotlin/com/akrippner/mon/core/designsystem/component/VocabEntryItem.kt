package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.VocabEntry
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun VocabEntryItem(
    entry: VocabEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (entry.jlptLevel != null) {
        MonTheme.colors.secondary.copy(alpha = if (entry.userNote != null) 1f else 0.50f)
    } else {
        MonTheme.colors.backgroundSecondary.copy(alpha = 0.16f)
    }

    Row (
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MonTheme.colors.vocabEntry.background)
            .clickable(onClick = onClick)
            .height(IntrinsicSize.Min)
    ){
        Column (
            modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 10.dp)
        ){
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = entry.surface, style = MonTheme.typography.titleMedium.copy(color = MonTheme.colors.vocabEntry.title))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = entry.reading,
                    style = MonTheme.typography.bodySmall.copy(
                        color = MonTheme.colors.vocabEntry.reading
                    ),
                )
                Spacer(Modifier.weight(1f))
                if (entry.jlptLevel != null) {
                    JlptBadge(level = entry.jlptLevel)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(text = entry.definition, style = MonTheme.typography.labelSmall.copy(color = MonTheme.colors.vocabEntry.subtitle))
            if (entry.userNote != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "— ${entry.userNote}",
                    style = MonTheme.typography.bodyExtraSmall.copy(
                        color = MonTheme.colors.vocabEntry.note,
                    )
                )
            }
        }

    }
}

//TODO: Check what is up with background color
@Preview
@Composable
fun VocabEntryItemPreview() {
    MonTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VocabEntryItem(
                entry = VocabEntry(
                    id = 1,
                    surface = "勉強",
                    reading = "べんきょう",
                    definition = "Study; diligence; discount",
                    jlptLevel = "N5",
                    sourceTitle = "Japanese for Busy People",
                    userNote = "Don't forget the second meaning"
                ),
                onClick = {}
            )
            VocabEntryItem(
                entry = VocabEntry(
                    id = 2,
                    surface = "猫",
                    reading = "ねこ",
                    definition = "Cat",
                    jlptLevel = null,
                    sourceTitle = "Daily Life",
                    userNote = null
                ),
                onClick = {}
            )
            VocabEntryItem(
                entry = VocabEntry(
                    id = 3,
                    surface = "食べる",
                    reading = "たべる",
                    definition = "To eat",
                    jlptLevel = "N5",
                    sourceTitle = null,
                    userNote = null
                ),
                onClick = {}
            )
        }
    }
}
