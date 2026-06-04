package com.akrippner.mon.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.akrippner.mon.core.navigation.Home
import com.akrippner.mon.core.navigation.MonNavKey
import com.akrippner.mon.feature.home.HomeScreen
import com.akrippner.mon.feature.home.HomeViewModel
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject

private val navSavedStateConfiguration = SavedStateConfiguration {
  serializersModule = SerializersModule {
    polymorphic(MonNavKey::class) { subclass(Home::class) }
    polymorphic(NavKey::class) { subclass(Home::class) }
  }
}

@Composable
fun MonNavHost() {
  val backStack = rememberNavBackStack(navSavedStateConfiguration, Home)
  val navigator = remember(backStack) { MonAppNavigator(backStack) }

  NavDisplay(
    backStack = backStack,
    onBack = navigator::back,
    entryProvider =
      entryProvider {
        entry<Home> {
          val homeViewModel = koinInject<HomeViewModel>()
          HomeScreen(viewModel = homeViewModel)
        }
      },
  )
}
