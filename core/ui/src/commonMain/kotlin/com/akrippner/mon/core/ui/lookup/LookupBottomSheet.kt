package com.akrippner.mon.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.PillVariant
import com.akrippner.mon.core.designsystem.component.YomuPillButton
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType
import com.akrippner.mon.core.ui.lookup.BadgeRow
import com.akrippner.mon.core.ui.lookup.KanjiDetailLink
import com.akrippner.mon.core.ui.lookup.LookupUiState
import com.akrippner.mon.core.ui.lookup.SaveRow
import com.akrippner.mon.core.ui.lookup.SenseBlock
import com.akrippner.mon.core.ui.lookup.WordDisplay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LookupBottomSheet(
    token: DisplayToken,
    lookupState: LookupUiState,
    onSave: (note: String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true) //TODO: Check if setting state here is correct
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = YomuColors.Night,
        contentColor = Color.White,
        dragHandle = {
            // Custom handle — matches 60×4dp design spec
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 60.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White.copy(alpha = 0.18f)),
            )
        },
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState() )
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp) //TODO: Check if two padding modificaitons are needed
                .navigationBarsPadding(),
        ) {
            when (lookupState) {
                is LookupUiState.Loading -> LookupLoadingContent()
                is LookupUiState.NotFound ->  LookupNotFoundContent(surface = token.surface)
                is LookupUiState.Error -> LookupErrorContent(onRetry = lookupState.onRetry)
                is LookupUiState.Found -> LookupFoundContent (
                    entry = lookupState.entry,
                    savedToVocab = lookupState.savedToVocab,
                    onSave = onSave
                )
            }
        }
    }
}

@Composable
private fun LookupLoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = YomuColors.Vermilion)
    }
}

@Composable
private fun LookupNotFoundContent(surface: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = surface,
                style = YomuType.VocabKanji.copy(color = Color.White.copy(alpha = 0.40f))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "No dictionary entry found",
                style = YomuType.Meta.copy(color = Color.White.copy(alpha = 0.35f)),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LookupErrorContent(onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Could not load entry",
                style = YomuType.Meta.copy(color = Color.White.copy(alpha = 0.45f)),
            )
            Spacer(Modifier.height(12.dp))
            YomuPillButton(label = "Retry", variant = PillVariant.Outline, onClick = onRetry)
        }
    }
}

@Composable
private fun LookupFoundContent(
    entry: JmdictEntry,
    savedToVocab: Boolean,
    onSave: (note: String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }

    WordDisplay(surface = entry.surface, reading = entry.reading)
    Spacer(Modifier.height(8.dp))

    BadgeRow(
        jlptLevel = entry.jlptLevel,
        pos = entry.senses.firstOrNull()?.posLabel,
    )
    Spacer(Modifier.height(16.dp))

    HorizontalDivider(color = Color.White.copy(alpha = 0.10f), thickness = 0.5.dp)

    entry.senses.forEachIndexed { index, sense ->
        Spacer(Modifier.height(12.dp))
        SenseBlock(index = index + 1, sense = sense)
        if (index < entry.senses.lastIndex) {
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
        }
    }

    Spacer(Modifier.height(20.dp))
    SaveRow(
        noteText = noteText,
        onNoteChange = { noteText = it },
        savedToVocab = savedToVocab,
        onSave = { onSave(noteText) }
    )
    Spacer(Modifier.height(12.dp))
    KanjiDetailLink( onClick = {
        //TODO: Navigate to kanji detail
    }
    )
}