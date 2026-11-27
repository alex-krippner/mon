package com.akrippner.mon.feature.reader

import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Label
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Sense
import com.akrippner.mon.core.ui.TokenState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReaderTokensTest {
  private fun token(surface: String, reading: String = surface, pos: String = "名詞") =
    MonToken(surface, dictionaryForm = surface, pronunciation = reading, partOfSpeech = listOf(pos))

  @Test
  fun furigana_ofAWordWithOnlyKanji_isTheWholeReadingInHiragana() {
    assertEquals("せんせい", furigana("先生", "センセイ"))
  }

  @Test
  fun furigana_cutsOkuriganaAtTheEnd() {
    assertEquals("た", furigana("食べ", "タベ"))
    assertEquals("い", furigana("行っ", "イッ"))
  }

  @Test
  fun furigana_cutsKanaAtTheStart() {
    assertEquals("かね", furigana("お金", "オカネ"))
  }

  @Test
  fun furigana_ofAWordWithoutKanji_isEmpty() {
    assertEquals("", furigana("たべる", "タベル"))
    assertEquals("", furigana("カメラ", "カメラ"))
    assertEquals("", furigana("。", ""))
  }

  @Test
  fun furigana_withoutAReading_isEmpty() {
    assertEquals("", furigana("先生", ""))
  }

  @Test
  fun furigana_whenTheKanaDoNotMatchTheReading_isTheWholeReading() {
    assertEquals("しょく", furigana("食べ", "ショク"))
  }

  @Test
  fun furigana_neverCutsTheWholeReading() {
    // The reading is only the okurigana: cutting it would leave nothing over the kanji.
    assertEquals("べ", furigana("食べ", "ベ"))
  }

  @Test
  fun furigana_withKanaBetweenKanji_isTheWholeReading() {
    assertEquals("ひきだし", furigana("引き出し", "ヒキダシ"))
  }

  @Test
  fun furigana_cutsOkuriganaWrittenInKatakana() {
    assertEquals("み", furigana("見ル", "ミル"))
  }

  @Test
  fun symbols_areNotWords() {
    assertEquals(TokenState.Punctuation, token("◆", pos = "記号").toDisplayToken().state)
  }

  @Test
  fun paragraphs_splitAtAnyKindOfLineBreak() {
    val tokens =
      listOf(
        token("私", "ワタシ"),
        token("\r\n", pos = "空白"),
        token("本", "ホン"),
        token("\r", pos = "空白"),
        token("犬", "イヌ"),
        token(" ", pos = "空白"),
        token("猫", "ネコ"),
      )

    assertEquals(
      listOf(listOf(0), listOf(2), listOf(4), listOf(6)),
      buildParagraphs(tokens).map { p -> p.map { it.index } },
    )
  }

  @Test
  fun furigana_treatsRepeatMarkAsKanji() {
    assertEquals("ひとびと", furigana("人々", "ヒトビト"))
  }

  @Test
  fun furigana_recognisesKanjiOutsideTheBasicPlane() {
    assertEquals("しか", furigana("\uD842\uDF9F", "シカ")) // 𠮟
    assertEquals("しか", furigana("\uD842\uDF9Fる", "シカル"))
    assertEquals("", furigana("\uD83D\uDE00", "エモジ")) // an emoji is not a kanji
  }

  @Test
  fun displayToken_ofPunctuationAndWhitespace_isPunctuation() {
    assertEquals(TokenState.Punctuation, token("。", pos = "補助記号").toDisplayToken().state)
    assertEquals(TokenState.Punctuation, token(" ", pos = "空白").toDisplayToken().state)
    assertEquals(TokenState.Default, token("私", "ワタシ", "代名詞").toDisplayToken().state)
  }

  @Test
  fun paragraphs_splitAtLineBreaks_andKeepTheTokenIndexes() {
    val tokens =
      listOf(
        token("私", "ワタシ"),
        token("は", pos = "助詞"),
        token("\n", pos = "空白"),
        token("学生", "ガクセイ"),
        token("。", pos = "補助記号"),
      )

    val paragraphs = buildParagraphs(tokens)

    assertEquals(listOf(listOf(0, 1), listOf(3, 4)), paragraphs.map { p -> p.map { it.index } })
    assertEquals("わたし", paragraphs[0][0].display.reading)
  }

  @Test
  fun paragraphs_leaveOutBlankParagraphs() {
    val tokens =
      listOf(
        token("\n", pos = "空白"),
        token("私", "ワタシ"),
        token("\n\n", pos = "空白"),
        token("\n", pos = "空白"),
        token("本", "ホン"),
        token(" ", pos = "空白"),
      )

    assertEquals(
      listOf(listOf(1), listOf(4, 5)),
      buildParagraphs(tokens).map { p -> p.map { it.index } },
    )
  }

  @Test
  fun paragraphs_ofNoTokens_areEmpty() {
    assertEquals(emptyList(), buildParagraphs(emptyList()))
  }

  private fun entry(
    headwords: List<Headword> = listOf(Headword("先生", "せんせい")),
    labels: List<Label> = emptyList(),
    senses: List<Sense>,
  ) =
    DictionaryEntry(
      id = "jmdict:1",
      source = DictionarySource("jmdict", "JMdict", "attribution", "1"),
      headwords = headwords,
      senses = senses,
      labels = labels,
    )

  @Test
  fun entry_usesTheFirstHeadwordAndEachSensesLabels() {
    val result =
      entry(
          senses =
            listOf(
              Sense(
                glosses = listOf("teacher", "master"),
                labels = listOf(Label.PartOfSpeech("noun"), Label.Register("honorific")),
              ),
              Sense(glosses = listOf("doctor"), labels = listOf(Label.Domain("medicine"))),
            ),
          labels = listOf(Label.PartOfSpeech("noun (common)")),
        )
        .toJmdictEntry()!!

    assertEquals("先生", result.surface)
    assertEquals("せんせい", result.reading)
    assertNull(result.jlptLevel)
    assertEquals(listOf("teacher", "master"), result.senses[0].glosses)
    assertEquals("noun", result.senses[0].posLabel)
    assertEquals("honorific", result.senses[0].usageNote)
    // A sense without its own part of speech takes the entry's.
    assertEquals("noun (common)", result.senses[1].posLabel)
    assertEquals("medicine", result.senses[1].usageNote)
  }

  @Test
  fun entry_withoutAReadingOrLabels_hasEmptyReadingAndNoLabels() {
    val result =
      entry(headwords = listOf(Headword("Haus")), senses = listOf(Sense(listOf("house"))))
        .toJmdictEntry()!!

    assertEquals("", result.reading)
    assertNull(result.senses.single().posLabel)
    assertNull(result.senses.single().usageNote)
  }

  @Test
  fun entry_withoutHeadwords_isLeftOut() {
    assertNull(entry(headwords = emptyList(), senses = listOf(Sense(listOf("x")))).toJmdictEntry())
  }
}
