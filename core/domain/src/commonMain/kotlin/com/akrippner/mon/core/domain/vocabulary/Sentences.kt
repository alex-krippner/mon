package com.akrippner.mon.core.domain.vocabulary

import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.SentenceRef

/** Characters that end a sentence. */
private const val TERMINATORS = "。！？!?"

/** Closing quotes and brackets that belong to the sentence they follow: 「…。」 */
private const val CLOSERS = "」』）)】〕］｝〉》”’\"'"

/** A whitespace token with a line break ends a sentence, as it ends a paragraph in the Reader. */
private fun MonToken.isLineBreak(): Boolean =
  surface.isNotEmpty() &&
    surface.isBlank() &&
    surface.any { it == '\n' || it == '\r' || it == ' ' || it == ' ' }

private fun MonToken.endsSentence(): Boolean = surface.isNotBlank() && surface.last() in TERMINATORS

private fun MonToken.isCloser(): Boolean = surface.isNotEmpty() && surface.all { it in CLOSERS }

/**
 * The sentence around the token at [index], with the token's place in it. Returns null if [index]
 * is not a token.
 *
 * A sentence ends after 。！？!? (and the closing quotes that follow) or at a line break, and the
 * whitespace around it is not part of it. The offsets are in the text the tokens' surfaces make up,
 * which is the reading's text.
 */
fun sentenceAround(tokens: List<MonToken>, index: Int): SentenceRef? {
  if (index !in tokens.indices) return null

  var first = 0
  var i = 0
  while (i < index) {
    val token = tokens[i]
    if (token.isLineBreak()) {
      first = i + 1
    } else if (token.endsSentence()) {
      var next = i + 1
      while (next < tokens.size && tokens[next].isCloser()) next++
      if (next > index) break // the token at index is itself a closing quote
      first = next
      i = next - 1
    }
    i++
  }
  while (first < index && tokens[first].surface.isBlank()) first++

  var last = index
  var k = index
  while (k < tokens.size) {
    val token = tokens[k]
    if (k > index && token.isLineBreak()) break
    last = k
    if (token.endsSentence()) {
      var next = k + 1
      while (next < tokens.size && tokens[next].isCloser()) {
        last = next
        next++
      }
      break
    }
    k++
  }
  while (last > index && tokens[last].surface.isBlank()) last--

  val textOffset = tokens.take(first).sumOf { it.surface.length }
  val text = tokens.subList(first, last + 1).joinToString("") { it.surface }
  val wordStart = tokens.subList(first, index).sumOf { it.surface.length }
  return SentenceRef(
    text = text,
    wordStart = wordStart,
    wordEnd = wordStart + tokens[index].surface.length,
    textOffset = textOffset,
  )
}

/**
 * The index of the token that covers the character at [offset] of the reading's text, or null if
 * the offset is outside the text. The reverse of the offsets [sentenceAround] gives.
 */
fun tokenIndexAtOffset(tokens: List<MonToken>, offset: Int): Int? {
  if (offset < 0) return null
  var start = 0
  tokens.forEachIndexed { index, token ->
    val end = start + token.surface.length
    if (offset < end) return index
    start = end
  }
  return null
}
