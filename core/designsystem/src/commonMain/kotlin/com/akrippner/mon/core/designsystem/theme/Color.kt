package com.akrippner.mon.core.designsystem.theme

import androidx.compose.ui.graphics.Color

object YomuColors {
    /** Deep ink — primary text, dark surfaces */
    val Ink         = Color(0xFF1A1714)
    /** Vermilion — accent, selection, furigana */
    val Vermilion   = Color(0xFFC0392B)
    val VermilionLight = Color(0xFFE74C3C)
    /** Paper — main background */
    val Paper       = Color(0xFFFDFAF4)
    val Parchment   = Color(0xFFF7F2E8)
    /** Canvas — app-level background */
    val Canvas      = Color(0xFFEDE7DA)
    /** Night — bottom-sheet / dark surface */
    val Night       = Color(0xFF1C1C2E)
    val NightDeep   = Color(0xFF13131F)
    /** Dark featured-card gradient stops */
    val CardDark1   = Color(0xFF2C1810)
    val CardDark2   = Color(0xFF1A1714)

    // Alphas used frequently in the design
    val InkSubtle    = Ink.copy(alpha = 0.45f)
    val InkDisabled  = Ink.copy(alpha = 0.25f)
    val InkHairline  = Ink.copy(alpha = 0.08f)
    val InkDivider   = Ink.copy(alpha = 0.07f)
    val WhiteSubtle  = Color.White.copy(alpha = 0.45f)
    val WhiteFaint   = Color.White.copy(alpha = 0.20f)
    val VermilionBg  = Vermilion.copy(alpha = 0.10f)
    val VermilionBgDark = Vermilion.copy(alpha = 0.15f)
}
