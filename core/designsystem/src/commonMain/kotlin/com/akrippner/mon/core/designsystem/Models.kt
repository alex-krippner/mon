package com.akrippner.mon.core.designsystem

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

enum class PillSize {
  /** 30 dp high: dense rows such as the reader's toolbar. */
  Compact,
  /** 44 dp high (48 with a sublabel): screen actions that must meet the touch-target size. */
  Large,
}

/** How loud a [com.akrippner.mon.core.designsystem.component.MonNotice] is. */
enum class NoticeTone {
  /** Something finished: a vermilion-tinted card with a check. */
  Success,
  /** A quiet note that changes nothing the reader can do: a paper card with an info icon. */
  Info,
  /** Something failed: inline red text with an error icon, no card. */
  Error,
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
