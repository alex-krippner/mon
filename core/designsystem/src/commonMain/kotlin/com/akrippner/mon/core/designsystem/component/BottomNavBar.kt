package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.NavTab
import com.akrippner.mon.core.designsystem.theme.YomuColors

@Composable
fun YomuBottomNavBar(
    selectedTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = YomuColors.Night,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
    ) {
        Column() {
            HorizontalDivider(color = YomuColors.InkDivider, thickness = 0.5.dp)
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                NavTab.entries.forEach { tab ->
                    YomuNavTabItem(
                        modifier = Modifier.weight(1f),
                        tab = tab,
                        selected = tab == selectedTab,
                        onClick = { onTabSelected(tab)}
                    )
                }
            }
        }
    }
}