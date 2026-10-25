package com.akrippner.mon.core.model.data

/** A language a reading can be written in or translated into. */
enum class Language(val tag: String) {
  JAPANESE("ja"),
  ENGLISH("en");

  companion object {
    /** Returns the language for a BCP-47 [tag], failing for languages that are not supported. */
    fun fromTag(tag: String): Language =
      entries.firstOrNull { it.tag == tag } ?: error("Unsupported language tag: $tag")
  }
}
