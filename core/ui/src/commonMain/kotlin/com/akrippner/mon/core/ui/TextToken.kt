package com.akrippner.mon.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.component.AnnotationDot
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun TextToken(
  token: DisplayToken,
  showFurigana: Boolean,
  onTap: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isPunctuation = token.state is TokenState.Punctuation

  Box(
    modifier =
      modifier.let {
        if (!isPunctuation) {
          it.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = ripple(bounded = false, radius = 20.dp),
            onClick = onTap,
          )
        } else {
          it
        }
      }
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier =
        Modifier.tokenBackground(
            token.state,
            selectedColor = MonTheme.colors.tokenizedText.background,
          )
          .padding(horizontal = 2.dp),
    ) {
      Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.BottomCenter) {
        if (showFurigana && token.reading.isNotEmpty() && !isPunctuation) {
          Text(
            text = token.reading,
            style =
              MonTheme.typography.bodyMedium.copy(
                color =
                  when (token.state) {
                    is TokenState.Selected -> MonTheme.colors.secondary.copy(alpha = 0.90f)
                    is TokenState.Annotated -> MonTheme.colors.secondary.copy(alpha = 0.70f)
                    else -> MonTheme.colors.secondary.copy(alpha = 0.75f)
                  }
              ),
            textAlign = TextAlign.Center,
          )
        }
      }
      Text(
        text = token.surface,
        style =
          MonTheme.typography.bodyMedium.copy(
            color =
              when (token.state) {
                is TokenState.Selected -> MonTheme.colors.secondary
                else -> MonTheme.colors.textPrimary
              },
            fontWeight =
              when (token.state) {
                is TokenState.Selected -> FontWeight.SemiBold
                else -> FontWeight.Normal
              },
          ),
      )
    }

    if (token.state is TokenState.Annotated) {
      AnnotationDot(modifier = Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp))
    }
  }
}

@Composable
private fun Modifier.tokenUnderline(
  state: TokenState,
  selectedColor: Color,
  annotatedColor: Color,
): Modifier = drawBehind {
  when (state) {
    is TokenState.Selected ->
      drawLine(
        color = selectedColor,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = 2.dp.toPx(),
        cap = StrokeCap.Round,
      )

    is TokenState.Annotated ->
      drawLine(
        color = annotatedColor,
        start = Offset(0f, size.height),
        end = Offset(size.width, size.height),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f)),
      )

    else -> Unit
  }
}

private fun Modifier.tokenBackground(state: TokenState, selectedColor: Color): Modifier =
  when (state) {
    is TokenState.Selected -> background(selectedColor, RoundedCornerShape(5.dp))
    else -> this
  }
