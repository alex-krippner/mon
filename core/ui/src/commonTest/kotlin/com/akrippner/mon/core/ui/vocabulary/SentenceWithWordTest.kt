package com.akrippner.mon.core.ui.vocabulary

import com.akrippner.mon.core.model.data.SentenceRef
import kotlin.test.Test
import kotlin.test.assertEquals

class SentenceWithWordTest {
  private val sentence =
    SentenceRef(
      text = "あ".repeat(20) + "夏" + "い".repeat(5),
      wordStart = 20,
      wordEnd = 21,
      textOffset = 0,
    )

  @Test
  fun aWordThatShowsWithoutCutting_cutsNothing() {
    assertEquals(0, firstVisibleCut(sentence) { true })
  }

  @Test
  fun aWordPastTheLastLine_cutsTheFewestCharactersThatShowIt() {
    // The word shows once at least 7 characters are cut from the start.
    assertEquals(7, firstVisibleCut(sentence) { cut -> cut >= 7 })
    assertEquals(1, firstVisibleCut(sentence) { cut -> cut >= 1 })
  }

  @Test
  fun aWordThatNeverShows_cutsUpToTheWord() {
    assertEquals(20, firstVisibleCut(sentence) { false })
  }

  @Test
  fun aCut_neverSplitsASurrogatePair() {
    // 𠮷 (U+20BB7) is two chars, at offsets 1 and 2.
    val pair = SentenceRef("a𠮷bc", wordStart = 3, wordEnd = 4, textOffset = 0)

    assertEquals(1, firstVisibleCut(pair) { cut -> cut >= 2 })
  }
}
