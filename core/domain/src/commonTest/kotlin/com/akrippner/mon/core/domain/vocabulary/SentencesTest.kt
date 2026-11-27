package com.akrippner.mon.core.domain.vocabulary

import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.SentenceRef
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SentencesTest {
  private fun tokens(vararg surfaces: String) = surfaces.map { MonToken(it, it, "", listOf("名詞")) }

  /** 先生と呼んだ。<newline>鎌倉で会った。 */
  private val twoSentences = tokens("先生", "と", "呼ん", "だ", "。", "\n", "鎌倉", "で", "会っ", "た", "。")

  @Test
  fun theSentenceAroundAWord_endsAfterTheFullStop() {
    val sentence = sentenceAround(twoSentences, index = 2)

    assertEquals(SentenceRef("先生と呼んだ。", wordStart = 3, wordEnd = 5, textOffset = 0), sentence)
  }

  @Test
  fun aLaterSentence_startsAfterTheLineBreak_andKeepsItsOffsetInTheText() {
    val sentence = sentenceAround(twoSentences, index = 8)!!

    assertEquals("鎌倉で会った。", sentence.text)
    assertEquals(3, sentence.wordStart)
    assertEquals(5, sentence.wordEnd)
    // 先生と呼んだ。 is 7 characters and the line break one more.
    assertEquals(8, sentence.textOffset)
    assertEquals(11, sentence.wordOffset)
  }

  @Test
  fun theOffsets_pointAtTheWordInTheReadingsText() {
    val text = twoSentences.joinToString("") { it.surface }

    for (index in listOf(0, 2, 6, 8)) {
      val sentence = sentenceAround(twoSentences, index)!!
      assertEquals(
        twoSentences[index].surface,
        text.substring(
          sentence.wordOffset,
          sentence.wordOffset + sentence.wordEnd - sentence.wordStart,
        ),
      )
      assertEquals(
        twoSentences[index].surface,
        sentence.text.substring(sentence.wordStart, sentence.wordEnd),
      )
    }
  }

  @Test
  fun aClosingQuote_staysWithTheSentenceItFollows() {
    val tokens = tokens("「", "行く", "。", "」", "と", "言っ", "た", "。")

    val first = sentenceAround(tokens, 1)!!
    val second = sentenceAround(tokens, 4)!!

    assertEquals("「行く。」", first.text)
    assertEquals("と言った。", second.text)
    assertEquals(5, second.textOffset)
  }

  @Test
  fun questionAndExclamationMarks_endASentence_inBothWidths() {
    val tokens = tokens("本当", "？", "はい", "!", "うん")

    assertEquals("本当？", sentenceAround(tokens, 0)!!.text)
    assertEquals("はい!", sentenceAround(tokens, 2)!!.text)
    assertEquals("うん", sentenceAround(tokens, 4)!!.text)
  }

  @Test
  fun spacesAroundASentence_areNotPartOfIt() {
    val tokens = tokens("　", "夏", "だ", "。", " ", "冬")

    val sentence = sentenceAround(tokens, 1)!!

    assertEquals("夏だ。", sentence.text)
    assertEquals(1, sentence.textOffset)
    assertEquals(0, sentence.wordStart)
  }

  @Test
  fun aTextWithoutAFullStop_isOneSentenceUpToTheLineBreak() {
    val tokens = tokens("夏", "\n", "冬")

    assertEquals("夏", sentenceAround(tokens, 0)!!.text)
    assertEquals("冬", sentenceAround(tokens, 2)!!.text)
  }

  @Test
  fun anIndexOutsideTheTokens_hasNoSentence() {
    assertNull(sentenceAround(twoSentences, -1))
    assertNull(sentenceAround(twoSentences, twoSentences.size))
    assertNull(sentenceAround(emptyList(), 0))
  }

  @Test
  fun tokenIndexAtOffset_findsTheTokenThatCoversTheCharacter() {
    // 先生(0-1) と(2) 呼ん(3-4) だ(5) 。(6)
    val tokens = tokens("先生", "と", "呼ん", "だ", "。")

    assertEquals(0, tokenIndexAtOffset(tokens, 0))
    assertEquals(0, tokenIndexAtOffset(tokens, 1))
    assertEquals(1, tokenIndexAtOffset(tokens, 2))
    assertEquals(2, tokenIndexAtOffset(tokens, 4))
    assertEquals(4, tokenIndexAtOffset(tokens, 6))
    assertNull(tokenIndexAtOffset(tokens, 7))
    assertNull(tokenIndexAtOffset(tokens, -1))
  }

  @Test
  fun tokenIndexAtOffset_isTheReverseOfTheSavedOffset() {
    val sentence = sentenceAround(twoSentences, 8)!!

    assertEquals(8, tokenIndexAtOffset(twoSentences, sentence.wordOffset))
  }
}
