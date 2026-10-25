package com.akrippner.mon.core.model.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LanguageTest {
  @Test
  fun fromTag_roundTripsEveryLanguage() {
    Language.entries.forEach { language -> assertEquals(language, Language.fromTag(language.tag)) }
  }

  @Test
  fun fromTag_unknownTag_fails() {
    assertFailsWith<IllegalStateException> { Language.fromTag("xx") }
  }
}
