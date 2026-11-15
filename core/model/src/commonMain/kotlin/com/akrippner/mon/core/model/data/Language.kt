package com.akrippner.mon.core.model.data

/** A language a reading can be written in or translated into. */
enum class Language(val tag: String) {
  JAPANESE("ja"),
  ENGLISH("en"),
  GERMAN("de");

  companion object {
    /**
     * Returns the language for a BCP-47 [tag], or null if it is not supported. Only the primary
     * language subtag is compared, ignoring case, so "ja-JP" is [JAPANESE].
     */
    fun fromTagOrNull(tag: String): Language? {
      val primary = tag.substringBefore('-')
      return entries.firstOrNull { it.tag.equals(primary, ignoreCase = true) }
    }

    /** Returns the language for a BCP-47 [tag], failing for languages that are not supported. */
    fun fromTag(tag: String): Language =
      fromTagOrNull(tag) ?: error("Unsupported language tag: $tag")
  }
}
