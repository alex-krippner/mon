package com.akrippner.mon.core.data.mapper

import com.akrippner.mon.core.database.entity.ReadingEntity
import com.akrippner.mon.core.database.entity.ReadingTokenEntity
import com.akrippner.mon.core.database.entity.ReadingWithTokens
import com.akrippner.mon.core.model.data.Language
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ReadingMappersTest {
  private fun readingWithTokens(
    sourceLanguage: String = "ja",
    translationLanguage: String = "en",
    tokens: List<ReadingTokenEntity> = emptyList(),
  ) =
    ReadingWithTokens(
      reading =
        ReadingEntity(
          id = "r1",
          userId = "u1",
          title = "Title",
          text = "私は",
          translation = "I",
          sourceLanguage = sourceLanguage,
          translationLanguage = translationLanguage,
          createdAt = 5,
          updatedAt = 5,
        ),
      tokens = tokens,
    )

  private fun token(index: Int, surface: String) =
    ReadingTokenEntity("r1", index, surface, "よみ", surface, listOf("名詞"))

  @Test
  fun toDomainOrNull_mapsFieldsAndOrdersTokensByIndex() {
    val reading = readingWithTokens(tokens = listOf(token(1, "は"), token(0, "私"))).toDomainOrNull()

    assertEquals("r1", reading?.id)
    assertEquals(Language.JAPANESE, reading?.sourceLanguage)
    assertEquals(Language.ENGLISH, reading?.translationLanguage)
    assertEquals(listOf("私", "は"), reading?.tokens?.map { it.surface })
    assertEquals("よみ", reading?.tokens?.first()?.pronunciation)
  }

  @Test
  fun toDomainOrNull_acceptsRegionTags() {
    assertEquals(
      Language.JAPANESE,
      readingWithTokens(sourceLanguage = "ja-JP").toDomainOrNull()?.sourceLanguage,
    )
  }

  @Test
  fun toDomainOrNull_unsupportedLanguage_returnsNull() {
    assertNull(readingWithTokens(sourceLanguage = "zh").toDomainOrNull())
    assertNull(readingWithTokens(translationLanguage = "xx").toDomainOrNull())
  }
}
