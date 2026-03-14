package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun YomuSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search words..."
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        textStyle = YomuType.Body.copy(color = YomuColors.Ink),
        cursorBrush = SolidColor(YomuColors.Vermilion),
        singleLine = true,

        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(YomuColors.Ink.copy(alpha = 0.05f))
            .border(1.dp, YomuColors.Ink.copy(alpha = 0.09f), RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "⌕",//TODO: This should be an icon
                    style = YomuType.Body.copy(color = YomuColors.InkSubtle, fontSize = 16.sp)

                )
                Spacer(Modifier.width(8.dp))
                if (query.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = YomuType.Body.copy(color = YomuColors.Ink.copy(alpha = 0.28f))

                    )
                }
                innerTextField()
            }
        }

    )
}