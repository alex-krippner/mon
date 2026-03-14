package com.akrippner.mon.core.designsystem.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


val GeorgiaFamily = FontFamily.Serif          // swap → FontFamily(Font(R.font.georgia))
val HelveticaFamily = FontFamily.SansSerif    // swap → FontFamily(Font(R.font.helvetica_neue))

object YomuType {
    /** 42 sp — word display in LookupSheet */
    val WordDisplay = TextStyle(
        fontFamily = GeorgiaFamily,
        fontSize = 42.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = 4.sp,
        color = Color.White,
    )
    /** 26 sp — page titles (本棚, 単語帳) */
    val PageTitle = TextStyle(
        fontFamily = GeorgiaFamily,
        fontSize = 26.sp,
        fontWeight = FontWeight.Light,
        letterSpacing = 4.sp,
        color = YomuColors.Ink,
    )
    /** 22 sp — vocab entry kanji */
    val VocabKanji = TextStyle(
        fontFamily = GeorgiaFamily,
        fontSize = 22.sp,
        fontWeight = FontWeight.Normal,
        color = YomuColors.Ink,
    )
    /** 19 sp — reader body Japanese */
    val ReaderBody = TextStyle(
        fontFamily = GeorgiaFamily,
        fontSize = 19.sp,
        fontWeight = FontWeight.Normal,
        color = YomuColors.Ink,
    )
    /** 18 sp — featured card title */
    val CardTitle = TextStyle(
        fontFamily = GeorgiaFamily,
        fontSize = 18.sp,
        letterSpacing = 2.sp,
        color = Color.White,
    )
    /** 14 sp — nav header title */
    val NavTitle = TextStyle(
        fontFamily = GeorgiaFamily,
        fontSize = 14.sp,
        letterSpacing = 2.sp,
        color = YomuColors.Ink,
    )
    /** 13 sp — list item titles */
    val ListItemTitle = TextStyle(
        fontFamily = GeorgiaFamily,
        fontSize = 13.sp,
        color = YomuColors.Ink,
    )
    /** 13 sp — lookup sheet reading (kana) */
    val Reading = TextStyle(
        fontFamily = GeorgiaFamily,
        fontSize = 13.sp,
        letterSpacing = 4.sp,
        color = YomuColors.Vermilion,
    )
    /** 8.5 sp — furigana above kanji */
    val Furigana = TextStyle(
        fontFamily = GeorgiaFamily,
        fontSize = 8.5.sp,
        color = YomuColors.Vermilion,
        lineHeight = 10.sp,
    )
    /** 14 sp — definitions */
    val Definition = TextStyle(
        fontFamily = HelveticaFamily,
        fontSize = 14.sp,
        color = Color.White,
    )
    /** 11 sp — body / labels */
    val Body = TextStyle(
        fontFamily = HelveticaFamily,
        fontSize = 11.sp,
        color = YomuColors.Ink,
    )
    /** 10 sp — metadata, subtitles */
    val Meta = TextStyle(
        fontFamily = HelveticaFamily,
        fontSize = 10.sp,
        color = YomuColors.InkSubtle,
    )
    /** 9 sp — section labels (letter-spaced caps) */
    val SectionLabel = TextStyle(
        fontFamily = HelveticaFamily,
        fontSize = 9.sp,
        letterSpacing = 3.sp,
        color = YomuColors.InkSubtle,
    )
    /** 9 sp — badge labels */
    val Badge = TextStyle(
        fontFamily = HelveticaFamily,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
    )
    /** 7 sp — nav tab labels */
    val NavLabel = TextStyle(
        fontFamily = HelveticaFamily,
        fontSize = 7.sp,
        letterSpacing = 1.sp,
    )
}