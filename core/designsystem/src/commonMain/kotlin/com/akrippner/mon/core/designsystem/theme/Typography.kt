package com.akrippner.mon.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import mon.core.designsystem.generated.resources.Res
import mon.core.designsystem.generated.resources.RobotoMono_VariableFont_wght
import org.jetbrains.compose.resources.Font


internal val RobotoMono: FontFamily
    @Composable
    get() = FontFamily(
        Font(Res.font.RobotoMono_VariableFont_wght, FontWeight.Light),
        Font(Res.font.RobotoMono_VariableFont_wght, FontWeight.Normal),
        Font(Res.font.RobotoMono_VariableFont_wght, FontWeight.Medium),
        Font(Res.font.RobotoMono_VariableFont_wght, FontWeight.SemiBold),
        Font(Res.font.RobotoMono_VariableFont_wght, FontWeight.Bold),
    )


val MonTypography: TypographyTokens
@Composable
get() {
    return TypographyTokens(
        bodyExtraSmall = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 8.sp,
            fontWeight = FontWeight.Light,
        ),
        bodySmall = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 12.sp,
            fontWeight = FontWeight.Light,
        ),
        bodyMedium = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 14.sp,
            fontWeight = FontWeight.Light,
        ),
        bodyLarge = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 16.sp,
            fontWeight = FontWeight.Light,
            letterSpacing = 0.1.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 10.sp,
            fontWeight = FontWeight.W500,
        ),
        labelMedium = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        ),
        labelLarge = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        ),
        titleSmall = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        ),
        titleMedium = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        ),
        titleLarge = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        ),
        displaySmall = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 36.sp,
        ),
        displayMedium = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 45.sp,
        ),
        displayLarge = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 57.sp,
        ),
        headlineSmall = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
        ),
        headlineMedium = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 28.sp,
            fontWeight = FontWeight.Normal,
        ),
        headlineLarge = TextStyle(
            fontFamily = RobotoMono,
            fontSize = 32.sp,
            fontWeight = FontWeight.Normal,
        ),
    )
}