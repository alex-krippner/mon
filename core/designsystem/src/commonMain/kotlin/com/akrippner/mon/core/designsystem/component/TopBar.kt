@file:OptIn(ExperimentalMaterial3Api::class)

package com.akrippner.mon.core.designsystem.component


import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconId
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonTopAppBar(
    title: String,
    navigationIconId: IconId,
    actions: @Composable (RowScope.() -> Unit) = {},
    onNavigationClick: () -> Unit = {},
    onActionClick: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MonTheme.typography.titleMedium,
                color = MonTheme.colors.navigation.text
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                MonIcon(
                    id = navigationIconId,
                    primaryColor = MonTheme.colors.navigation.text,
                    size = IconSize.ExtraSmall
                )
            }
        },
        actions = actions,
        colors = TopAppBarColors(
            containerColor = MonTheme.colors.navigation.background,
            navigationIconContentColor = MonTheme.colors.navigation.text,
            scrolledContainerColor = Color.Unspecified,
            titleContentColor = Color.Unspecified,
            subtitleContentColor = Color.Unspecified,
            actionIconContentColor = Color.Unspecified
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview("Top App Bar")
@Composable
private fun MonTopAppBarPreview() {
    MonTheme {
        MonTopAppBar(
            title = "Reading",
            navigationIconId = Icon.Core.ChevronLeft,
            actions = {
                FuriganaToggle(
                    onToggle = {},
                    enabled = true,
                    modifier = Modifier.height(30.dp).width(50.dp)
                )
            }

        )
    }
}