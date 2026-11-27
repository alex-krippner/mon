@file:OptIn(ExperimentalMaterial3Api::class)

package com.akrippner.mon.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconId
import com.akrippner.mon.core.designsystem.icon.IconSize
import com.akrippner.mon.core.designsystem.icon.MonIcon
import com.akrippner.mon.core.designsystem.theme.MonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonTopAppBar(
  title: String,
  navigationIconId: IconId? = null,
  actions: @Composable (RowScope.() -> Unit) = {},
  onNavigationClick: () -> Unit = {},
  onActionClick: () -> Unit = {},
  /** Read by screen readers for the navigation icon, e.g. "Back to vocabulary". */
  navigationDescription: String? = null,
  /** A smaller line under the title. The title is then smaller too, as on a pushed list. */
  subtitle: String? = null,
) {
  CenterAlignedTopAppBar(
    title = {
      if (subtitle == null) {
        Text(
          text = title,
          style = MonTheme.typography.titleMedium,
          color = MonTheme.colors.navigation.text,
        )
      } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = title,
            style = MonTheme.typography.titleCompact,
            color = MonTheme.colors.navigation.text,
          )
          Text(
            text = subtitle,
            style = MonTheme.typography.labelSmall,
            color = MonTheme.colors.inkSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
      }
    },
    navigationIcon = {
      if (navigationIconId != null) {
        IconButton(
          onClick = onNavigationClick,
          modifier =
            if (navigationDescription == null) Modifier
            else Modifier.semantics { contentDescription = navigationDescription },
        ) {
          MonIcon(
            id = navigationIconId,
            primaryColor = MonTheme.colors.navigation.text,
            size = IconSize.ExtraSmall,
          )
        }
      }
    },
    actions = actions,
    colors =
      TopAppBarColors(
        containerColor = MonTheme.colors.navigation.background,
        navigationIconContentColor = MonTheme.colors.navigation.text,
        scrolledContainerColor = Color.Unspecified,
        titleContentColor = Color.Unspecified,
        subtitleContentColor = Color.Unspecified,
        actionIconContentColor = Color.Unspecified,
      ),
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
      actions = { FuriganaToggle(onToggle = {}, enabled = true) },
    )
  }
}
