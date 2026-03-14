package com.akrippner.mon.core.ui.lookup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.FieldVariant
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.component.YomuPillButton
import com.akrippner.mon.core.designsystem.component.YomuTextField

@Composable
fun SaveRow(
    noteText: String,
    onNoteChange: (String) -> Unit,
    savedToVocab: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        YomuTextField(
            value = noteText,
            onValueChange = onNoteChange,
            label = "Add a note...",
            variant = FieldVariant.Dark,
            modifier = Modifier.weight(1f)
        )
        YomuPillButton(
            label = "保存",
            sublabel = if (savedToVocab) "Saved ✓" else "Save",
            variant = PillVariant.Filled,
            onClick = onSave,
            enabled = !savedToVocab
        )
    }
}