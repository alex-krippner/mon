package com.akrippner.mon.core.designsystem.component

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.akrippner.mon.core.designsystem.theme.MonTheme
import kotlinx.coroutines.withTimeoutOrNull

/** How long a snackbar with Undo stays: long enough to read it and reach the button. */
const val UNDO_SNACKBAR_MILLIS = 5_000L

/** The app's snackbars: ink on the screen, with the action in the sheet's light vermilion. */
@Composable
fun MonSnackbarHost(state: SnackbarHostState, modifier: Modifier = Modifier) {
  SnackbarHost(hostState = state, modifier = modifier) { data ->
    Snackbar(
      snackbarData = data,
      containerColor = MonTheme.colors.textPrimary,
      contentColor = MonTheme.colors.surfaceRaised,
      actionColor = MonTheme.colors.sheetAccentText,
    )
  }
}

/**
 * Shows [message] with an action for [UNDO_SNACKBAR_MILLIS]. Returns true if the user tapped the
 * action. The snackbar is announced to screen readers by the host.
 */
suspend fun SnackbarHostState.showUndo(message: String, actionLabel: String): Boolean =
  withTimeoutOrNull(UNDO_SNACKBAR_MILLIS) {
    showSnackbar(
      message = message,
      actionLabel = actionLabel,
      // The timeout above ends it, so it lasts exactly as long as promised.
      duration = SnackbarDuration.Indefinite,
    )
  } == SnackbarResult.ActionPerformed
