package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconId
import com.akrippner.mon.core.designsystem.theme.MonTheme

@Composable
fun BottomNavBar(
  selectedTab: String,
  onTabSelected: (String) -> Unit,
  navTabs: List<IconId>,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .background(MonTheme.colors.navigation.background)
        .navigationBarsPadding()
        .height(53.dp),
    horizontalArrangement = Arrangement.SpaceAround,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    navTabs.forEach { tab ->
      YomuNavTabItem(
        iconId = tab,
        selected = tab.category + tab.name == selectedTab,
        onClick = { onTabSelected(tab.category + tab.name) },
      )
    }
  }
}

@Preview(showBackground = true)
@Composable
private fun PreviewBottomNavBar() {

  val navTabs =
    listOf(Icon.Core.Furigana, Icon.Core.Library, Icon.Core.Settings, Icon.Core.Vocabulary)
  var selectedTab by remember { mutableStateOf("") }

  fun onSelected(tab: String) {
    selectedTab = tab
  }
  MonTheme {
    BottomNavBar(selectedTab = selectedTab, navTabs = navTabs, onTabSelected = { onSelected(it) })
  }
}
