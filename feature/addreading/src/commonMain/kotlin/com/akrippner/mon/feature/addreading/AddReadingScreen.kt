package com.akrippner.mon.feature.addreading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.akrippner.mon.core.designsystem.FieldVariant
import com.akrippner.mon.core.designsystem.NoticeTone
import com.akrippner.mon.core.designsystem.component.Button
import com.akrippner.mon.core.designsystem.component.ButtonSize
import com.akrippner.mon.core.designsystem.component.ButtonVariant
import com.akrippner.mon.core.designsystem.component.MonConfirmDialog
import com.akrippner.mon.core.designsystem.component.MonNotice
import com.akrippner.mon.core.designsystem.component.MonTextArea
import com.akrippner.mon.core.designsystem.component.PrimaryButton
import com.akrippner.mon.core.designsystem.component.Scaffold
import com.akrippner.mon.core.designsystem.component.YomuTextField
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.isKeyboardOpen
import com.akrippner.mon.core.designsystem.theme.MonTheme
import com.akrippner.mon.feature.addreading.file.rememberTextFilePicker
import com.akrippner.mon.feature.addreading.file.supportsFileDrop
import com.akrippner.mon.feature.addreading.file.textFileDropTarget
import com.akrippner.mon.feature.addreading.generated.resources.Res
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_add_file
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_add_file_dialog
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_add_file_reading
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_area_placeholder
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_area_placeholder_drop
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_area_reading
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_discard_body
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_discard_confirm
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_discard_keep
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_discard_title
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_drop_detail
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_drop_title
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_file_empty
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_file_not_text
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_file_too_large
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_file_unreadable
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_missing_text
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_missing_title
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_error_save_failed
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_field_title
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_hint_title
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_import
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_importing
import com.akrippner.mon.feature.addreading.generated.resources.add_reading_title
import org.jetbrains.compose.resources.stringResource

/** The widest the form gets; on a wide window it is centred instead of stretched. */
private val MAX_CONTENT_WIDTH = 640.dp

@Composable
fun AddReadingScreen(state: AddReadingState, onIntent: (AddReadingIntent) -> Unit) {
  val imeVisible = isKeyboardOpen()
  var isDropHovering by remember { mutableStateOf(false) }

  val openFilePicker =
    rememberTextFilePicker(title = stringResource(Res.string.add_reading_add_file_dialog)) { file ->
      onIntent(AddReadingIntent.FileChosen(file))
    }

  // With text in the area, back asks before the text is lost. Without it, back is not handled
  // here and the navigation closes the screen as usual.
  NavigationBackHandler(
    state = rememberNavigationEventState(currentInfo = NavigationEventInfo.None),
    isBackEnabled = state.text.isNotBlank(),
    onBackCompleted = { onIntent(AddReadingIntent.Close) },
  )

  Scaffold(
    title = stringResource(Res.string.add_reading_title),
    navigationIcon = Icon.Core.Close,
    onNavigationClick = { onIntent(AddReadingIntent.Close) },
  ) { innerPadding ->
    Box(
      modifier =
        Modifier.fillMaxSize()
          // The scaffold already pads for the system bars; without this the keyboard's inset,
          // which includes them, would count them twice.
          .consumeWindowInsets(innerPadding)
          .imePadding(),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(
        modifier =
          Modifier.fillMaxHeight()
            .widthIn(max = MAX_CONTENT_WIDTH)
            .fillMaxWidth()
            .onPreviewKeyEvent { event ->
              val isImportShortcut =
                event.type == KeyEventType.KeyDown &&
                  event.key == Key.Enter &&
                  (event.isCtrlPressed || event.isMetaPressed)
              if (isImportShortcut) {
                if (state.canImport) onIntent(AddReadingIntent.Import)
                true
              } else {
                false
              }
            }
            .padding(
              start = 39.dp,
              end = 39.dp,
              top = 2.dp,
              // Room for the bottom bar's overlap is not needed; with the keyboard open the area
              // shrinks and the button stays just above the keyboard.
              bottom = if (imeVisible) 12.dp else 52.dp,
            )
      ) {
        Button(
          onClick = openFilePicker,
          label =
            stringResource(
              if (state.isReadingFile) Res.string.add_reading_add_file_reading
              else Res.string.add_reading_add_file
            ),
          variant = ButtonVariant.Secondary,
          size = ButtonSize.Small,
          shape = RoundedCornerShape(MonTheme.radii.small),
          enabled = !state.isLocked,
          loading = state.isReadingFile,
          modifier = Modifier.padding(start = 10.dp),
        )

        TextAreaWithDrop(
          state = state,
          isDropHovering = isDropHovering,
          onHoverChange = { isDropHovering = it },
          onIntent = onIntent,
          modifier = Modifier.weight(1f).padding(top = 2.dp),
        )

        YomuTextField(
          value = state.title,
          onValueChange = { onIntent(AddReadingIntent.TitleChanged(it)) },
          label = stringResource(Res.string.add_reading_field_title),
          variant = FieldVariant.Light,
          enabled = !state.isLocked,
          modifier =
            Modifier.fillMaxWidth().padding(top = 22.dp).alpha(if (state.isLocked) 0.6f else 1f),
        )

        Message(state = state, modifier = Modifier.padding(top = 10.dp))

        PrimaryButton(
          label =
            stringResource(
              if (state.isSaving) Res.string.add_reading_importing
              else Res.string.add_reading_import
            ),
          onClick = { onIntent(AddReadingIntent.Import) },
          enabled = state.canImport,
          busy = state.isSaving,
          modifier = Modifier.padding(top = 14.dp),
        )
      }
    }
  }

  if (state.showDiscardDialog) {
    MonConfirmDialog(
      title = stringResource(Res.string.add_reading_discard_title),
      text = stringResource(Res.string.add_reading_discard_body),
      confirmLabel = stringResource(Res.string.add_reading_discard_confirm),
      dismissLabel = stringResource(Res.string.add_reading_discard_keep),
      onConfirm = { onIntent(AddReadingIntent.DiscardConfirmed) },
      onDismiss = { onIntent(AddReadingIntent.DiscardDismissed) },
    )
  }
}

@Composable
private fun TextAreaWithDrop(
  state: AddReadingState,
  isDropHovering: Boolean,
  onHoverChange: (Boolean) -> Unit,
  onIntent: (AddReadingIntent) -> Unit,
  modifier: Modifier = Modifier,
) {
  val placeholder =
    when {
      state.readingFileName != null ->
        stringResource(Res.string.add_reading_area_reading, state.readingFileName)
      supportsFileDrop -> stringResource(Res.string.add_reading_area_placeholder_drop)
      else -> stringResource(Res.string.add_reading_area_placeholder)
    }

  Box(
    modifier =
      modifier.textFileDropTarget(
        onHoverChange = onHoverChange,
        onDrop = { file -> onIntent(AddReadingIntent.FileChosen(file)) },
      )
  ) {
    MonTextArea(
      value = state.text,
      onValueChange = { onIntent(AddReadingIntent.TextChanged(it)) },
      placeholder = placeholder,
      enabled = !state.isLocked,
      modifier = Modifier.fillMaxSize().alpha(if (state.isLocked) 0.6f else 1f),
    )
    // A locked screen ignores a drop, so it does not invite one.
    if (isDropHovering && !state.isLocked) DropOverlay(modifier = Modifier.fillMaxSize())
  }
}

/** Shown over the text area while a file is dragged above it. */
@Composable
private fun DropOverlay(modifier: Modifier = Modifier) {
  val accent = MonTheme.colors.secondary
  Column(
    modifier =
      modifier
        .background(MonTheme.colors.accentTint, RoundedCornerShape(25.dp))
        .drawBehind {
          val stroke = 2.dp.toPx()
          drawRoundRect(
            color = accent,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(size.width - stroke, size.height - stroke),
            cornerRadius = CornerRadius(25.dp.toPx()),
            style =
              Stroke(
                width = stroke,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())),
              ),
          )
        }
        .padding(24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
  ) {
    MonIcon(id = Icon.Core.Upload, primaryColor = accent, requiredSize = 32.dp)
    Text(
      text = stringResource(Res.string.add_reading_drop_title),
      style = MonTheme.typography.titleSmall,
      color = MonTheme.colors.textPrimary,
      textAlign = TextAlign.Center,
    )
    Text(
      text = stringResource(Res.string.add_reading_drop_detail),
      style = MonTheme.typography.labelMedium,
      color = MonTheme.colors.input.placeholder,
      textAlign = TextAlign.Center,
    )
  }
}

/** One line above the button: an error, or else the hint that the title is missing. */
@Composable
private fun Message(state: AddReadingState, modifier: Modifier = Modifier) {
  val error = state.error
  when {
    error != null ->
      MonNotice(title = error.message(), tone = NoticeTone.Error, modifier = modifier)
    state.showTitleHint ->
      Text(
        text = stringResource(Res.string.add_reading_hint_title),
        style = MonTheme.typography.labelMedium,
        color = MonTheme.colors.input.placeholder,
        modifier = modifier,
      )
  }
}

@Composable
private fun AddReadingError.message(): String =
  when (this) {
    AddReadingError.MissingTitle -> stringResource(Res.string.add_reading_error_missing_title)
    AddReadingError.MissingText -> stringResource(Res.string.add_reading_error_missing_text)
    AddReadingError.SaveFailed -> stringResource(Res.string.add_reading_error_save_failed)
    AddReadingError.FileNotText -> stringResource(Res.string.add_reading_error_file_not_text)
    AddReadingError.FileTooLarge -> stringResource(Res.string.add_reading_error_file_too_large)
    AddReadingError.FileEmpty -> stringResource(Res.string.add_reading_error_file_empty)
    AddReadingError.FileUnreadable -> stringResource(Res.string.add_reading_error_file_unreadable)
  }
