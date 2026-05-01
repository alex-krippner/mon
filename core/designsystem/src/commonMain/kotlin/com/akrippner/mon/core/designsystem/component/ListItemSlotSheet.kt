package com.akrippner.mon.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.modifier.conditional
import com.akrippner.mon.core.designsystem.theme.MonTheme

@DslMarker private annotation class SlotSheetScope

@SlotSheetScope
internal class SlotSheetScopeImpl {
  internal var startSlot by mutableStateOf<(@Composable () -> Unit)?>(null)
  internal var contentSlot by mutableStateOf<(@Composable () -> Unit)?>(null)
  internal var endSlot by mutableStateOf<(@Composable () -> Unit)?>(null)

  fun startSlot(component: @Composable () -> Unit) {
    startSlot = component
  }

  fun contentSlot(component: @Composable () -> Unit) {
    contentSlot = component
  }

  fun endSlot(component: @Composable () -> Unit) {
    endSlot = component
  }
}

@Composable
internal fun ListItemSlotSheet(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  content: SlotSheetScopeImpl.() -> Unit = {},
) {
  val scope = SlotSheetScopeImpl().apply(content)
  val startSlot = scope.startSlot
  val contentSlot = scope.contentSlot ?: return
  val endSlot = scope.endSlot

  val isContentFirstChild = startSlot == null
  val isContentLastChild = endSlot == null

  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()

  val backgroundColor by
    animateColorAsState(
      targetValue =
        if (isPressed) MonTheme.colors.listItem.background.copy(alpha = 0.5f)
        else MonTheme.colors.listItem.background,
      label = "backgroundColor",
    )

  Column(
    modifier =
      Modifier.fillMaxWidth()
        .background(color = backgroundColor, shape = RoundedCornerShape(size = 50.dp))
        .then(modifier)
        .pointerInput(Unit) {
          detectTapGestures(
            onPress = {
              val press = PressInteraction.Press(it)
              interactionSource.emit(press)
              tryAwaitRelease()
              interactionSource.emit(PressInteraction.Release(press))
            },
            onTap = { onClick?.invoke() },
          )
        },
    verticalArrangement = Arrangement.Center,
  ) {
    Row(
      modifier =
        Modifier.padding(horizontal = MonTheme.space.spacing4, vertical = MonTheme.space.spacing3)
          .conditional(onClick != null) {
            clickable(onClick = onClick!!, interactionSource = interactionSource, indication = null)
          },
      verticalAlignment = Alignment.CenterVertically,
    ) {
      if (startSlot != null) {
        Column(
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.size(MonTheme.sizes.iconSmall),
        ) {
          startSlot()
        }
      }
      Column(
        Modifier.padding(vertical = MonTheme.space.spacing2, horizontal = MonTheme.space.spacing2)
          .weight(1f)
          .conditional(isContentFirstChild) { padding(start = MonTheme.space.spacing5) }
          .conditional(isContentLastChild) { padding(end = MonTheme.space.spacing5) }
      ) {
        contentSlot()
      }

      if (endSlot != null) {
        BoxWithConstraints { Row(modifier = Modifier.widthIn(max = maxWidth * 0.5f)) { endSlot() } }
      }
    }
  }
}
