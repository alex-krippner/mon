package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.theme.HelveticaFamily
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun YomuTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null, //TODO: Check use case
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {} //TODO: Check what is type means
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = YomuColors.Paper,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Text(
                        text = "‹",
                        style = LocalTextStyle.current.copy(
                            fontSize = 28.sp,//TODO: Set theme variables?
                            color = YomuColors.Ink,
                            fontFamily = HelveticaFamily,
                            fontWeight = FontWeight.Light,
                        )
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = YomuType.NavTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = YomuType.Meta.copy(fontSize = 8.sp, letterSpacing = 1.sp),
                        maxLines = 1
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            content = actions
        )
    }
}