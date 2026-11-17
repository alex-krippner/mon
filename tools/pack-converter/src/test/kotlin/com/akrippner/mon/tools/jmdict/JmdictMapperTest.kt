package com.akrippner.mon.tools.jmdict

import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Label
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JmdictMapperTest {
  private val tags =
    mapOf(
      "v1" to "Ichidan verb",
      "vt" to "transitive verb",
      "uk" to "usually written using kana alone",
      "col" to "colloquialism",
      "comp" to "computing",
      "ksb" to "Kansai-ben",
    )

  private fun sense(
    vararg glosses: String,
    partOfSpeech: List<String> = emptyList(),
    field: List<String> = emptyList(),
    dialect: List<String> = emptyList(),
    misc: List<String> = emptyList(),
    info: List<String> = emptyList(),
  ) =
    SimplifiedSense(
      partOfSpeech = partOfSpeech,
      field = field,
      dialect = dialect,
      misc = misc,
      info = info,
      gloss = glosses.map { SimplifiedGloss(it) },
    )

  @Test
  fun mapsAWordWithTwoSpellingsAndOneReading() {
    val taberu =
      SimplifiedWord(
        id = "1358280",
        kanji = listOf(SimplifiedKanji("食べる", common = true), SimplifiedKanji("喰べる")),
        kana = listOf(SimplifiedKana("たべる", common = true, appliesToKanji = listOf("*"))),
        sense =
          listOf(
            sense("to eat", partOfSpeech = listOf("v1", "vt")),
            sense("to live on", "to live off", partOfSpeech = listOf("v1", "vt")),
          ),
      )

    val record = assertNotNull(taberu.toRecord(tags))

    assertEquals(1358280L, record.id)
    assertEquals(true, record.common)
    assertEquals(listOf(Headword("食べる", "たべる"), Headword("喰べる", "たべる")), record.headwords)
    assertEquals(listOf("to eat"), record.senses[0].glosses)
    assertEquals(listOf("to live on", "to live off"), record.senses[1].glosses)
    assertEquals(
      listOf(Label.PartOfSpeech("Ichidan verb"), Label.PartOfSpeech("transitive verb")),
      record.senses[0].labels,
    )
    assertEquals(emptyList(), record.labels)
  }

  @Test
  fun kanaOnlyWordHasKanaHeadword() {
    val word =
      SimplifiedWord(id = "1", kana = listOf(SimplifiedKana("ああ")), sense = listOf(sense("ah")))

    assertEquals(listOf(Headword("ああ")), word.toRecord(tags)?.headwords)
  }

  @Test
  fun everyApplicableReadingIsKeptAndRestrictedReadingsOnlyApplyToTheirKanji() {
    val word =
      SimplifiedWord(
        id = "2",
        kanji = listOf(SimplifiedKanji("日"), SimplifiedKanji("陽")),
        kana =
          listOf(
            SimplifiedKana("ひ", appliesToKanji = listOf("*")),
            SimplifiedKana("にち", appliesToKanji = listOf("日")),
            SimplifiedKana("ニチ"), // applies to no kanji: a headword of its own
          ),
        sense = listOf(sense("day")),
      )

    assertEquals(
      listOf(Headword("日", "ひ"), Headword("日", "にち"), Headword("陽", "ひ"), Headword("ニチ")),
      word.toRecord(tags)?.headwords,
    )
  }

  @Test
  fun kanjiWithoutAnyReadingStillBecomesAHeadword() {
    val word =
      SimplifiedWord(id = "3", kanji = listOf(SimplifiedKanji("語")), sense = listOf(sense("word")))

    assertEquals(listOf(Headword("語")), word.toRecord(tags)?.headwords)
  }

  @Test
  fun commonIfAnySpellingOrReadingIsCommon() {
    val onlyReadingCommon =
      SimplifiedWord(
        id = "4",
        kanji = listOf(SimplifiedKanji("甲")),
        kana = listOf(SimplifiedKana("こう", common = true, appliesToKanji = listOf("*"))),
        sense = listOf(sense("x")),
      )
    val notCommon =
      SimplifiedWord(id = "5", kana = listOf(SimplifiedKana("おつ")), sense = listOf(sense("y")))

    assertEquals(true, onlyReadingCommon.toRecord(tags)?.common)
    assertEquals(false, notCommon.toRecord(tags)?.common)
  }

  @Test
  fun tagsBecomeTheMatchingLabelKinds() {
    val word =
      SimplifiedWord(
        id = "6",
        kana = listOf(SimplifiedKana("ぱそこん")),
        sense =
          listOf(
            sense(
              "PC",
              partOfSpeech = listOf("v1"),
              field = listOf("comp"),
              misc = listOf("col", "uk"),
              dialect = listOf("ksb"),
              info = listOf("esp. as a loanword"),
            )
          ),
      )

    assertEquals(
      listOf(
        Label.PartOfSpeech("Ichidan verb"),
        Label.Domain("computing"),
        Label.Register("colloquialism"),
        Label.Other("usually written using kana alone"),
        Label.Other("Kansai-ben"),
        Label.Other("esp. as a loanword"),
      ),
      word.toRecord(tags)?.senses?.single()?.labels,
    )
  }

  @Test
  fun unknownTagCodeFallsBackToTheCode() {
    val word =
      SimplifiedWord(
        id = "7",
        kana = listOf(SimplifiedKana("あ")),
        sense = listOf(sense("a", partOfSpeech = listOf("zzz"))),
      )

    assertEquals(listOf(Label.PartOfSpeech("zzz")), word.toRecord(tags)?.senses?.single()?.labels)
  }

  @Test
  fun sensesWithoutGlossAreDroppedAndAWordWithoutSensesIsSkipped() {
    val oneEmptySense =
      SimplifiedWord(
        id = "8",
        kana = listOf(SimplifiedKana("あ")),
        sense = listOf(sense(), sense("kept")),
      )
    val noUsableSense =
      SimplifiedWord(id = "9", kana = listOf(SimplifiedKana("い")), sense = listOf(sense()))
    val noSpelling = SimplifiedWord(id = "10", sense = listOf(sense("orphan")))

    assertEquals(1, oneEmptySense.toRecord(tags)?.senses?.size)
    assertNull(noUsableSense.toRecord(tags))
    assertNull(noSpelling.toRecord(tags))
  }
}
