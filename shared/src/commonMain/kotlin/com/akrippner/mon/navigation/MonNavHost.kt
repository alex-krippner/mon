package com.akrippner.mon.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.akrippner.mon.core.navigation.Library
import com.akrippner.mon.core.navigation.MonNavKey
import com.akrippner.mon.feature.library.LibraryScreen
import com.akrippner.mon.feature.library.LibraryViewModel
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.viewmodel.koinViewModel

private val navSavedStateConfiguration = SavedStateConfiguration {
  serializersModule = SerializersModule {
    polymorphic(MonNavKey::class) { subclass(Library::class) }
    polymorphic(NavKey::class) { subclass(Library::class) }
  }
}

@Composable
fun MonNavHost() {
  val backStack = rememberNavBackStack(navSavedStateConfiguration, Library)
  val navigator = remember(backStack) { MonAppNavigator(backStack) }

  NavDisplay(
    backStack = backStack,
    onBack = navigator::back,
    entryProvider =
      entryProvider {
        entry<Library> {
          val viewModel = koinViewModel<LibraryViewModel>()
          val state by viewModel.state.collectAsStateWithLifecycle()
          LibraryScreen(state = state, onIntent = viewModel::onIntent)
        }
      },
  )
}
