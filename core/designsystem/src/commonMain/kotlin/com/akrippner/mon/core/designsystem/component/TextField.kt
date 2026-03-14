package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.FieldVariant
import com.akrippner.mon.core.designsystem.theme.GeorgiaFamily
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun YomuTextField(

    value: String,//TODO: Change to TextFieldValue?
    onValueChange: (String) -> Unit,
    label: String,
    variant: FieldVariant,
    modifier: Modifier = Modifier
) {
    val isFocused = remember { mutableStateOf(false) }

    val bgColor = when (variant) {
        FieldVariant.Light -> YomuColors.Paper
        FieldVariant.Dark -> Color.White.copy(alpha = 0.08f)
    }
    val borderColor = when {
        variant == FieldVariant.Dark -> Color.White.copy(alpha = 0.12f)
        isFocused.value -> YomuColors.Vermilion
        else -> YomuColors.Ink.copy(alpha = 0.13f)
    }
    val textColor = when (variant) {
        FieldVariant.Light -> YomuColors.Ink
        FieldVariant.Dark -> Color.White.copy(alpha = 0.85f)
    }
    val placeholderColor = when (variant) {
        FieldVariant.Light -> YomuColors.Ink.copy(alpha = 0.25f)
        FieldVariant.Dark -> Color.White.copy(alpha = 0.25f)

    }

    val borderWidth = if (isFocused.value && variant == FieldVariant.Light) 1.5.dp else 1.dp

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = YomuType.Body.copy(color = textColor, fontFamily = GeorgiaFamily),
        cursorBrush = SolidColor(YomuColors.Vermilion), singleLine = true,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = label,
                    style = YomuType.Body.copy(color = placeholderColor),

                    )
            }
            innerTextField()
        }
    )
}
