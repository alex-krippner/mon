package com.akrippner.mon.feature.addreading

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.akrippner.mon.core.designsystem.FieldVariant
import com.akrippner.mon.core.designsystem.component.PrimaryButton
import com.akrippner.mon.core.designsystem.component.Scaffold
import com.akrippner.mon.core.designsystem.component.YomuTextField
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.feature.addreading.generated.resources.Res
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_missing_text
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_missing_title
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_save_failed
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_field_text
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_field_title
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_field_translation
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_save
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_saving
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddReadingScreen(
  state: AddReadingState,
  onIntent: (AddReadingIntent) -> Unit,
  onBack: () -> Unit,
) {
  Scaffold(
    title = stringResource(Res.string.add_reading_title),
    navigationIcon = Icon.Core.ChevronLeft,
    onNavigationClick = onBack,
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(MonTheme.space.spacing10),
      verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing5),
    ) {
      YomuTextField(
        value = state.title,
        onValueChange = { onIntent(AddReadingIntent.TitleChanged(it)) },
        label = stringResource(Res.string.add_reading_field_title),
        variant = FieldVariant.Light,
        modifier = Modifier.fillMaxWidth(),
      )
      YomuTextField(
        value = state.text,
        onValueChange = { onIntent(AddReadingIntent.TextChanged(it)) },
        label = stringResource(Res.string.add_reading_field_text),
        variant = FieldVariant.Light,
        modifier = Modifier.fillMaxWidth(),
      )
      YomuTextField(
        value = state.translation,
        onValueChange = { onIntent(AddReadingIntent.TranslationChanged(it)) },
        label = stringResource(Res.string.add_reading_field_translation),
        variant = FieldVariant.Light,
        modifier = Modifier.fillMaxWidth(),
      )
      state.error?.let { error ->
        Text(
          text = error.message(),
          style = MonTheme.typography.bodyMedium,
          color = MonTheme.colors.textPrimary,
        )
      }
      PrimaryButton(
        label =
          stringResource(
            if (state.isSaving) Res.string.add_reading_saving else Res.string.add_reading_save
          ),
        onClick = { onIntent(AddReadingIntent.Save) },
        enabled = state.canSave,
      )
    }
  }
}

@Composable
private fun AddReadingError.message(): String =
  when (this) {
    AddReadingError.MissingTitle -> stringResource(Res.string.add_reading_error_missing_title)
    AddReadingError.MissingText -> stringResource(Res.string.add_reading_error_missing_text)
    AddReadingError.SaveFailed -> stringResource(Res.string.add_reading_error_save_failed)
  }
