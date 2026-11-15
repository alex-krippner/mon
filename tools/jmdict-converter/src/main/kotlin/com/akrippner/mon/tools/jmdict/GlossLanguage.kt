package com.akrippner.mon.tools.jmdict

/** A gloss language jmdict-simplified publishes, and how its pack is described in the catalog. */
internal enum class GlossLanguage(
  /** The code in jmdict-simplified's `languages` field and file names, e.g. `eng`. */
  val code: String,
  /** The BCP-47 tag the app uses for the pack's `glossLanguage`, e.g. `en`. */
  val tag: String,
  val displayName: String,
) {
  ENGLISH("eng", "en", "English"),
  GERMAN("ger", "de", "German");

  /** Stays `jmdict-eng` for English, so packs that are already installed keep their id. */
  val packId: String
    get() = "jmdict-$code"

  companion object {
    fun fromCode(code: String): GlossLanguage? = entries.firstOrNull { it.code == code }
  }
}
