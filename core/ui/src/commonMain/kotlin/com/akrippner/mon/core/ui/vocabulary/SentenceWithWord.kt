package com.akrippner.mon.core.ui.vocabulary

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import com.akrippner.mon.core.model.data.SentenceRef

private const val ELLIPSIS = "…"

/**
 * The sentence a word was saved from, with the word styled by [wordStyle].
 *
 * If the word would fall past [maxLines], the start of the sentence is cut and replaced by "…", so
 * the word stays visible.
 */
@Composable
fun SentenceWithWord(
  sentence: SentenceRef,
  wordStyle: SpanStyle,
  maxLines: Int,
  style: TextStyle,
  modifier: Modifier = Modifier,
) {
  val textMeasurer = rememberTextMeasurer()
  // Sentences are Japanese: the tokenizer only has a Japanese pack. The locale picks the CJK
  // glyphs.
  val textStyle = style.copy(localeList = LocaleList("ja"))

  BoxWithConstraints(modifier = modifier) {
    val width = constraints.maxWidth
    val text =
      remember(sentence, wordStyle, maxLines, textStyle, width) {
        if (width == Constraints.Infinity) {
          sentence.annotated(cut = 0, wordStyle)
        } else {
          val wordIsVisible = { cut: Int ->
            val candidate = sentence.annotated(cut, wordStyle)
            val layout =
              textMeasurer.measure(
                text = candidate,
                style = textStyle,
                constraints = Constraints(maxWidth = width),
              )
            val lastWordChar = candidate.length - (sentence.text.length - sentence.wordEnd) - 1
            layout.getLineForOffset(lastWordChar.coerceAtLeast(0)) < maxLines
          }
          sentence.annotated(firstVisibleCut(sentence, wordIsVisible), wordStyle)
        }
      }
    Text(text = text, style = textStyle, maxLines = maxLines, overflow = TextOverflow.Ellipsis)
  }
}

/**
 * The smallest number of characters to cut from the start of the sentence so that the word shows,
 * found by bisection: cutting more only moves the word up. [wordIsVisible] says whether the word is
 * on a visible line after cutting that many.
 */
internal fun firstVisibleCut(sentence: SentenceRef, wordIsVisible: (cut: Int) -> Boolean): Int {
  if (wordIsVisible(0)) return 0
  // Cutting up to the word always shows it (as far as it fits on a line), so the answer is there.
  var low = 0 // does not show the word
  var high = sentence.wordStart // shows it
  while (high - low > 1) {
    val middle = (low + high) / 2
    if (wordIsVisible(middle)) high = middle else low = middle
  }
  return sentence.text.safeCut(high)
}

/** Moves a cut back by one if it would split a surrogate pair. */
private fun String.safeCut(cut: Int): Int =
  if (cut in 1 until length && this[cut].isLowSurrogate() && this[cut - 1].isHighSurrogate()) {
    cut - 1
  } else {
    cut
  }

private fun SentenceRef.annotated(cut: Int, wordStyle: SpanStyle): AnnotatedString =
  buildAnnotatedString {
    if (cut > 0) append(ELLIPSIS)
    append(text.substring(cut, wordStart))
    withStyle(wordStyle) { append(text.substring(wordStart, wordEnd)) }
    append(text.substring(wordEnd))
  }
