package com.akrippner.mon.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.akrippner.mon.core.designsystem.component.FabPosition
import com.akrippner.mon.core.designsystem.component.FloatingActionButton
import com.akrippner.mon.core.designsystem.component.Scaffold
import com.akrippner.mon.core.designsystem.component.TextListItem
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.modifier.conditional
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun LibraryScreen(state: LibraryState, onIntent: (LibraryIntent) -> Unit) {
  val listState = rememberLazyListState()
  val isScrolled by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
    }
  }

  val navTabs = listOf(Icon.Core.Library, Icon.Core.Vocabulary)
  var selectedTab by remember { mutableStateOf("") }

  fun onSelected(tab: String) {
    selectedTab = tab
  }

  Scaffold(
    title = "Library",
    selectedTab = selectedTab,
    navTabs = navTabs,
    navigationIcon = Icon.Core.ChevronLeft,
    onTabSelected = { selectedTab = it },
    onNavigationClick = { println("Alex: back") },
    floatingActionButtonPosition = FabPosition.END,
    floatingActionButton = {
      FloatingActionButton(onClick = {}, modifier = Modifier.offset(-MonTheme.space.spacing10))
    },
  ) { padding ->
    Column(
      modifier = Modifier.fillMaxSize().padding(MonTheme.space.spacing10),
      verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing5),
    ) {
      FeaturedReading(
        title = "こころ",
        wordCount = 25,
        vocabCount = 12,
        onOpen = { println("Alex: open") },
      )
      Spacer(modifier = Modifier.height(MonTheme.space.spacing8))
      Text(
        text = "All texts",
        style = MonTheme.typography.titleMedium,
        color = MonTheme.colors.textPrimary,
        modifier =
          Modifier.fillMaxWidth()
            .conditional(isScrolled) {
              bottomBorder(
                MonTheme.space.spacing2px,
                color = MonTheme.colors.secondary.copy(alpha = 0.1f),
              )
            }
            .padding(bottom = MonTheme.space.spacing5),
      )
      LazyColumn(
        state = listState,
        verticalArrangement = Arrangement.spacedBy(MonTheme.space.spacing3),
        contentPadding = PaddingValues(bottom = MonTheme.space.spacing4),
        modifier = Modifier.weight(1f).padding(bottom = MonTheme.space.spacing10),
      ) {
        items(state.readings, key = { it.id }) { reading ->
          TextListItem(
            title = reading.title,
            meta = "${reading.tokens.size} words",
            onClick = { onIntent(LibraryIntent.OpenReading(reading.id)) },
          )
        }
      }
    }
  }
}

@Composable
fun Modifier.bottomBorder(strokeWidth: Dp, color: Color): Modifier {
  val density = LocalDensity.current
  val strokeWidthPx = density.run { strokeWidth.toPx() }

  return this.then(
    Modifier.drawBehind {
      val width = size.width
      val height = size.height - strokeWidthPx / 2

      drawLine(
        color = color,
        start = Offset(x = 0f, y = height),
        end = Offset(x = width, y = height),
        strokeWidth = strokeWidthPx,
      )
    }
  )
}

@Composable
fun Modifier.topBorder(strokeWidth: Dp, color: Color): Modifier {
  val density = LocalDensity.current
  val strokeWidthPx = density.run { strokeWidth.toPx() }

  return this.then(
    Modifier.drawBehind {
      val width = size.width
      val y = strokeWidthPx / 2

      drawLine(
        color = color,
        start = Offset(x = 0f, y = y),
        end = Offset(x = width, y = y),
        strokeWidth = strokeWidthPx,
      )
    }
  )
}
