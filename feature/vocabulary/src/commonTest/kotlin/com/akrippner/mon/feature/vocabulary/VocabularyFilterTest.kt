package com.akrippner.mon.feature.vocabulary

import com.akrippner.mon.core.model.data.JlptLevel
import com.akrippner.mon.core.model.data.VocabularyGroup
import kotlin.test.Test
import kotlin.test.assertEquals

class VocabularyFilterTest {
  private val first =
    VocabularyGroup(
      "r1",
      "First",
      listOf(
        savedWord("a", gloss = "summer", jlpt = JlptLevel.N5),
        savedWord("b", kana = "ふゆ", gloss = "winter", note = "cold one"),
        savedWord("c", gloss = "autumn", jlpt = JlptLevel.N3),
      ),
    )
  private val second = VocabularyGroup("r2", "Second", listOf(savedWord("d", readingId = "r2")))

  @Test
  fun withoutAFilter_aTabSectionShowsTheTwoNewestWords_andHowManyMore() {
    val sections = buildSections(listOf(first, second), "", emptySet(), previewCount = 2)

    assertEquals(listOf("a", "b"), sections[0].words.map { it.id })
    assertEquals(3, sections[0].wordCount)
    assertEquals(1, sections[0].moreCount)
    assertEquals(0, sections[1].moreCount)
  }

  @Test
  fun theListOfOneReading_showsAllWords() {
    val sections = buildSections(listOf(first), "", emptySet(), previewCount = null)

    assertEquals(3, sections.single().words.size)
    assertEquals(0, sections.single().moreCount)
  }

  @Test
  fun aSearch_showsEveryMatch_andHidesReadingsWithoutOne() {
    val sections = buildSections(listOf(first, second), "WINTER", emptySet(), previewCount = 2)

    assertEquals(listOf("r1"), sections.map { it.readingId })
    assertEquals(listOf("b"), sections.single().words.map { it.id })
    assertEquals(1, sections.single().wordCount)
    assertEquals(0, sections.single().moreCount)
  }

  @Test
  fun aSearch_looksAtKanaAndNote_andNeedsEveryPart() {
    fun ids(query: String) =
      buildSections(listOf(first), query, emptySet(), null).flatMap { it.words }.map { it.id }

    assertEquals(listOf("b"), ids("ふゆ"))
    assertEquals(listOf("b"), ids("cold"))
    assertEquals(listOf("b"), ids("cold winter"))
    assertEquals(emptyList(), ids("cold summer"))
  }

  @Test
  fun levels_areOrdered_andWordsWithoutALevelDoNotMatch() {
    val sections =
      buildSections(listOf(first), "", setOf(JlptLevel.N5, JlptLevel.N3), previewCount = 2)

    assertEquals(listOf("a", "c"), sections.single().words.map { it.id })
  }

  @Test
  fun aSearchAndALevel_mustBothMatch() {
    val sections = buildSections(listOf(first), "autumn", setOf(JlptLevel.N5), previewCount = 2)

    assertEquals(emptyList(), sections)
  }
}
