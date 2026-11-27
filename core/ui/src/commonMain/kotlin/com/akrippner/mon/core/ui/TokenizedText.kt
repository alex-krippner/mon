package com.akrippner.mon.core.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.MonTheme

/** Reader body text. The design uses this near-black instead of the `ink50` token. */
private val BodyColor = Color(0xFF1A1714)

private val BodyFontSize = 19.sp
private val BodyLetterSpacing = 4.8.sp
private val FuriganaFontSize = 8.5.sp

/**
 * The distance between two lines of text at the default font size. It leaves room above the text
 * for the furigana. The text and the furigana scale with the user's font size, so the pitch grows
 * to fit them when they would not fit in it any more.
 */
private val LinePitch = 44.dp

private val HighlightRadius = 5.dp
private val HighlightPadding = 2.dp

/** The stroke around the saved word that "Show in reading" points at. */
private val OutlineWidth = 2.dp

/** The share of the vermilion accent that marks the tapped word. */
private const val SELECTED_BACKGROUND_ALPHA = 0.18f

/**
 * A paragraph of tokens as one flowing text, with furigana over the words that have a reading.
 *
 * The tokens are laid out as one [BasicText], so lines break between any two characters, also
 * inside a word, as in the design. Compose has no ruby support, so the furigana and the highlights
 * are drawn from the text layout: each one is placed from the bounding boxes of its token's
 * characters. A word that wraps gets its furigana over the part on its first line.
 *
 * @param tokens the paragraph, in reading order. Their surfaces are joined without a separator.
 * @param selectedIndex the index in [tokens] of the tapped word, drawn highlighted and bold.
 * @param onTokenTap called with the index in [tokens] of the tapped word. Punctuation is not
 *   tappable.
 * @param outlinedIndex the index in [tokens] of a saved word to draw with a vermilion outline, as
 *   the word that "Show in reading" points at.
 * @param onOutlinedBounds called with the bounds of the outlined word, in this text's coordinates,
 *   once it is laid out, so the caller can scroll to it.
 * @param savedLabel the word for "saved" (for example "Saved words"). When there are saved words,
 *   screen readers hear it with the words, so the highlight is not the only sign.
 */
@Composable
fun TokenizedText(
  tokens: List<DisplayToken>,
  showFurigana: Boolean,
  selectedIndex: Int?,
  onTokenTap: (index: Int) -> Unit,
  modifier: Modifier = Modifier,
  outlinedIndex: Int? = null,
  onOutlinedBounds: ((Rect) -> Unit)? = null,
  savedLabel: String? = null,
) {
  val density = LocalDensity.current
  val textMeasurer = rememberTextMeasurer()
  val accent = MonTheme.colors.secondary
  val annotatedBackground = MonTheme.colors.tokenizedText.background
  val currentOnTokenTap by rememberUpdatedState(onTokenTap)

  val furiganaStyle =
    remember(accent, density) { TextStyle(color = accent, fontSize = FuriganaFontSize) }
  val baseStyle =
    remember(density) {
      TextStyle(color = BodyColor, fontSize = BodyFontSize, letterSpacing = BodyLetterSpacing)
    }
  // The height of one line of text without the extra space above it, and of one line of furigana.
  val glyphHeight = remember(baseStyle) { textMeasurer.measure("あ", baseStyle).size.height }
  val furiganaHeight =
    remember(furiganaStyle) { textMeasurer.measure("あ", furiganaStyle).size.height }
  val bodyStyle =
    remember(baseStyle, glyphHeight, furiganaHeight) {
      val pitch =
        maxOf(
          with(density) { LinePitch.toPx() },
          glyphHeight + furiganaHeight + with(density) { HighlightPadding.toPx() },
        )
      baseStyle.copy(
        lineHeight = with(density) { pitch.toSp() },
        // The extra height of the line goes above the text, where the furigana is drawn.
        lineHeightStyle =
          LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Bottom,
            trim = LineHeightStyle.Trim.None,
          ),
      )
    }

  val ranges = remember(tokens) { tokenRanges(tokens) }
  val text =
    remember(tokens, selectedIndex, accent) {
      buildAnnotatedString {
        tokens.forEachIndexed { index, token ->
          if (index == selectedIndex) {
            withStyle(SpanStyle(color = accent, fontWeight = FontWeight.SemiBold)) {
              append(token.surface)
            }
          } else {
            append(token.surface)
          }
        }
      }
    }

  var layout by remember { mutableStateOf<TextLayoutResult?>(null) }

  val currentOnOutlinedBounds by rememberUpdatedState(onOutlinedBounds)
  LaunchedEffect(layout, outlinedIndex) {
    val textLayout = layout ?: return@LaunchedEffect
    val index = outlinedIndex?.takeIf { it in ranges.indices } ?: return@LaunchedEffect
    textLayout.lineBoxes(ranges[index]).firstOrNull()?.let { currentOnOutlinedBounds?.invoke(it) }
  }

  val savedWords =
    remember(tokens) {
      tokens.filter { it.state is TokenState.Annotated }.map { it.surface }.distinct()
    }

  BasicText(
    text = text,
    style = bodyStyle,
    onTextLayout = { layout = it },
    modifier =
      modifier
        .then(
          if (savedLabel != null && savedWords.isNotEmpty()) {
            Modifier.semantics { stateDescription = "$savedLabel: ${savedWords.joinToString()}" }
          } else {
            Modifier
          }
        )
        .pointerInput(tokens) {
          detectTapGestures { position ->
            val tapped = layout?.tokenAt(position, ranges)
            if (tapped != null && tokens[tapped].state != TokenState.Punctuation) {
              currentOnTokenTap(tapped)
            }
          }
        }
        .drawWithContent {
          val textLayout = layout
          if (textLayout == null) {
            drawContent()
            return@drawWithContent
          }
          val spacing = BodyLetterSpacing.toPx()
          val padding = HighlightPadding.toPx()

          // Behind the text: the tapped word and the saved words, and the outline of the word that
          // "Show in reading" points at.
          tokens.forEachIndexed { index, token ->
            val background =
              when {
                index == selectedIndex -> accent.copy(alpha = SELECTED_BACKGROUND_ALPHA)
                token.state is TokenState.Annotated -> annotatedBackground
                else -> null
              }
            val outlined = index == outlinedIndex
            if (background == null && !outlined) return@forEachIndexed

            textLayout.lineBoxes(ranges[index]).forEach { box ->
              val topLeft = Offset(box.left - padding, box.bottom - glyphHeight - padding)
              val size = Size(box.width - spacing + 2 * padding, glyphHeight + 2 * padding)
              val corners = CornerRadius(HighlightRadius.toPx())
              if (background != null) {
                drawRoundRect(
                  color = background,
                  topLeft = topLeft,
                  size = size,
                  cornerRadius = corners,
                )
              }
              if (outlined) {
                drawRoundRect(
                  color = accent,
                  topLeft = topLeft,
                  size = size,
                  cornerRadius = corners,
                  style = Stroke(width = OutlineWidth.toPx()),
                )
              }
            }
          }

          drawContent()

          if (showFurigana) {
            tokens.forEachIndexed { index, token ->
              if (token.reading.isEmpty() || token.state == TokenState.Punctuation) {
                return@forEachIndexed
              }
              val box = textLayout.lineBoxes(ranges[index]).firstOrNull() ?: return@forEachIndexed
              val furigana = textMeasurer.measure(token.reading, furiganaStyle)
              val centerX = (box.left + box.right - spacing) / 2
              val x =
                (centerX - furigana.size.width / 2).coerceIn(
                  0f,
                  maxOf(0f, size.width - furigana.size.width),
                )
              val y = box.bottom - glyphHeight - furigana.size.height
              drawText(furigana, topLeft = Offset(x, y))
            }
          }
        },
  )
}

/** The character range of each token in the joined text. */
private fun tokenRanges(tokens: List<DisplayToken>): List<IntRange> {
  var start = 0
  return tokens.map { token ->
    val range = start until start + token.surface.length
    start += token.surface.length
    range
  }
}

/** The index of the token drawn at [position], or null if it hits no character. */
private fun TextLayoutResult.tokenAt(position: Offset, ranges: List<IntRange>): Int? {
  val length = layoutInput.text.length
  if (length == 0) return null
  val offset = getOffsetForPosition(position).coerceIn(0, length - 1)
  if (!getBoundingBox(offset).contains(position)) return null
  return ranges.indexOfFirst { offset in it }.takeIf { it >= 0 }
}

/** One box per line that [range] covers: the union of the boxes of its characters on that line. */
private fun TextLayoutResult.lineBoxes(range: IntRange): List<Rect> {
  val boxes = mutableListOf<Rect>()
  var line = -1
  var current = Rect.Zero
  for (offset in range) {
    val box = getBoundingBox(offset)
    val boxLine = getLineForOffset(offset)
    if (boxLine == line) {
      current =
        Rect(
          left = minOf(current.left, box.left),
          top = minOf(current.top, box.top),
          right = maxOf(current.right, box.right),
          bottom = maxOf(current.bottom, box.bottom),
        )
    } else {
      if (line != -1) boxes += current
      line = boxLine
      current = box
    }
  }
  if (line != -1) boxes += current
  return boxes
}
