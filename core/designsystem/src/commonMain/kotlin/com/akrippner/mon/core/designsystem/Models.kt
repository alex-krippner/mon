package com.akrippner.mon.core.designsystem

// ─────────────────────────────────────────────
// Domain / display models
// (lightweight UI-layer representations — map from your domain models before passing in)
// ─────────────────────────────────────────────





/** A saved vocabulary word */
data class VocabEntry(
    val id: Long,
    val surface: String, //TODO: Rename to vocab?
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
    val progress: Float,        // 0f..1f
)

// ─────────────────────────────────────────────
// UI-only enums used as composable parameters
// ─────────────────────────────────────────────

enum class NavTab(val kanjiLabel: String, val latinLabel: String) {
    Library("本", "本棚"),
    Vocab("語", "単語帳"),
}

enum class CardStyle   { Primary, Secondary }
enum class PillVariant { Filled, Outline }
enum class FieldVariant { Light, Dark }
enum class BadgeTheme  { Light, Dark }
enum class ProgressVariant { Card, Reader, Mini }
