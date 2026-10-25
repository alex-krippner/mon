package com.akrippner.mon.core.model.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class LanguageTest {
  @Test
  fun fromTag_roundTripsEveryLanguage() {
    Language.entries.forEach { language -> assertEquals(language, Language.fromTag(language.tag)) }
  }

  @Test
  fun fromTagOrNull_matchesPrimarySubtagIgnoringCase() {
    assertEquals(Language.JAPANESE, Language.fromTagOrNull("ja-JP"))
    assertEquals(Language.ENGLISH, Language.fromTagOrNull("EN"))
  }

  @Test
  fun fromTagOrNull_unknownTag_returnsNull() {
    assertNull(Language.fromTagOrNull("zh"))
    assertNull(Language.fromTagOrNull(""))
  }

  @Test
  fun fromTag_unknownTag_fails() {
    assertFailsWith<IllegalStateException> { Language.fromTag("xx") }
  }
}
