package com.akrippner.mon.core.designsystem

/** A saved vocabulary word */
data class VocabEntry(
  val id: Long,
  val surface: String, // TODO: Rename to vocab?
  val reading: String,
  val definition: String,
  val jlptLevel: String?,
  val sourceTitle: String?,
  val userNote: String? = null,
)

/** An imported text shown in the library */
data class TextEntry(
  val id: Long,
  val title: String,
  val author: String? = null,
  val wordCount: Int,
  val annotationCount: Int,
  val progress: Float,
)

// ─────────────────────────────────────────────
// UI-only enums used as composable parameters
// ─────────────────────────────────────────────

enum class CardStyle {
  Primary,
  Secondary,
}

enum class PillVariant {
  Filled,
  Outline,
}

enum class FieldVariant {
  Light,
  Dark,
}

enum class ProgressVariant {
  Card,
  Reader,
  Mini,
}
