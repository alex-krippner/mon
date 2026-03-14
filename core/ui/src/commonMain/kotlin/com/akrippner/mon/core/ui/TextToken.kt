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
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.component.AnnotationDot
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun TextToken(
    token: DisplayToken,
    showFurigana: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPunctuation = token.state is TokenState.Punctuation

    Box(
        modifier = modifier.let {
            if (!isPunctuation) {
                it.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = false, radius = 20.dp),
                    onClick = onTap
                )
            } else {
                it
            }
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .tokenBackground(token.state)
                .tokenUnderline(token.state)
                .padding(horizontal = 2.dp)
        ) {
            Box(modifier = Modifier.height(14.dp), contentAlignment = Alignment.BottomCenter) {
                if (showFurigana && token.reading.isNotEmpty() && !isPunctuation) {
                    Text(
                        text = token.reading,
                        style = YomuType.Furigana.copy(
                            color = when (token.state) {
                                is TokenState.Selected -> YomuColors.Vermilion.copy(alpha = 0.90f)
                                is TokenState.Annotated -> YomuColors.Vermilion.copy(alpha = 0.70f)
                                else -> YomuColors.Vermilion.copy(alpha = 0.75f)
                            }
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
            Text(
                text = token.surface,
                style = YomuType.ReaderBody.copy(
                    color = when (token.state) {
                        is TokenState.Selected -> YomuColors.Vermilion
                        else -> YomuColors.Ink
                    },
                    fontWeight = when (token.state) {
                        is TokenState.Selected -> FontWeight.SemiBold
                        else -> FontWeight.Normal
                    }
                )
            )
        }

        if (token.state is TokenState.Annotated) {
            AnnotationDot(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
            )
        }
    }
}

private fun Modifier.tokenUnderline(state: TokenState): Modifier = drawBehind {
    when (state) {
        is TokenState.Selected -> drawLine(
            color = YomuColors.Vermilion,
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        is TokenState.Annotated -> drawLine(
            color = YomuColors.Vermilion.copy(alpha = 0.55f),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 5f))
        )

        is TokenState.Noted -> drawLine(
            color = YomuColors.Ink.copy(alpha = 0.35f),
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            strokeWidth = 1.5.dp.toPx()
        )

        else -> Unit
    }
}

private fun Modifier.tokenBackground(state: TokenState): Modifier =
    when (state) {
        is TokenState.Selected -> background(
            YomuColors.Vermilion.copy(alpha = 0.12f),
            RoundedCornerShape(5.dp),
        )

        is TokenState.Annotated -> background(
            YomuColors.Vermilion.copy(alpha = 0.06f),
            RoundedCornerShape(5.dp),
        )

        is TokenState.Noted -> background(
            YomuColors.Ink.copy(alpha = 0.04f),
            RoundedCornerShape(5.dp),
        )

        else -> this
    }