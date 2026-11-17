package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.akrippner.mon.core.designsystem.icon.IconId
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun Scaffold(
  title: String,
  modifier: Modifier = Modifier,
  navigationIcon: IconId? = null,
  onNavigationClick: () -> Unit = {},
  topBarActions: @Composable RowScope.() -> Unit = {},
  floatingActionButton: @Composable () -> Unit = {},
  floatingActionButtonPosition: FabPosition = FabPosition.END,
  selectedTab: String = "",
  navTabs: List<IconId> = emptyList(),
  onTabSelected: (String) -> Unit = {},
  content: @Composable (PaddingValues) -> Unit,
) {
  Scaffold(
    modifier = modifier.fillMaxSize(),
    containerColor = MonTheme.colors.appBackground,
    topBar = {
      MonTopAppBar(
        title = title,
        navigationIconId = navigationIcon,
        onNavigationClick = onNavigationClick,
        actions = topBarActions,
      )
    },
    bottomBar = {
      if (navTabs.isNotEmpty()) {
        BottomNavBar(
          selectedTab = selectedTab,
          navTabs = navTabs,
          onTabSelected = onTabSelected,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    },
    floatingActionButtonPosition = floatingActionButtonPosition.toMaterialFabPosition(),
    floatingActionButton = floatingActionButton,
  ) { innerPadding ->
    // innerPadding contains the height of the top and bottom bars
    // so the content doesn't get cut off.
    Box(modifier = Modifier.padding(innerPadding)) { content(innerPadding) }
  }
}

enum class FabPosition {
  START,
  CENTER,
  END,
  END_OVERLAY,
}

private fun FabPosition.toMaterialFabPosition(): androidx.compose.material3.FabPosition =
  when (this) {
    FabPosition.START -> androidx.compose.material3.FabPosition.Start
    FabPosition.CENTER -> androidx.compose.material3.FabPosition.Center
    FabPosition.END -> androidx.compose.material3.FabPosition.End
    FabPosition.END_OVERLAY -> androidx.compose.material3.FabPosition.EndOverlay
  }
