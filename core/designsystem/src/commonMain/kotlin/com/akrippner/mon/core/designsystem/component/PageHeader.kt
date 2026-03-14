package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.akrippner.mon.core.designsystem.BadgeTheme
import com.akrippner.mon.core.designsystem.theme.YomuColors
import com.akrippner.mon.core.designsystem.theme.YomuSpacing
import com.akrippner.mon.core.designsystem.theme.YomuTheme
import com.akrippner.mon.core.designsystem.theme.YomuType

@Composable
fun YomuPageHeader(
    kanjiTitle: String,
    latinLabel: String,
    modifier: Modifier = Modifier,
    badge: @Composable () -> Unit = {},
    action: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = YomuColors.Paper,
        shadowElevation = 1.dp,
    )


    {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = YomuSpacing.screenPadding, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = kanjiTitle, style = YomuType.PageTitle)
                Text(
                    text = latinLabel,
                    style = YomuType.SectionLabel.copy(
                        fontSize = 9.sp,
                        letterSpacing = 3.sp
                    )
                )
            }
            badge()
            Spacer(Modifier.width(8.dp))
            action()
        }
    }
}

@Preview
@Composable
fun YomuPageHeaderPreview() {
    YomuTheme {
        YomuPageHeader(
            kanjiTitle = "単語帳",
            latinLabel = "VOCABULARY",
            badge = {
                JlptBadge(level = "N5", theme = BadgeTheme.Light)
            },
            action = {
                YomuIconButton(onClick = {}) {
                    Text(
                        text = "＋",
                        style = YomuType.NavTitle.copy(fontSize = 20.sp)
                    )
                }
            }
        )
    }
}
