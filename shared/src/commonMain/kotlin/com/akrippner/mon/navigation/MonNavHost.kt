package com.akrippner.mon.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.akrippner.mon.core.navigation.AddReading
import com.akrippner.mon.core.navigation.Library
import com.akrippner.mon.core.navigation.MonNavConfiguration
import com.akrippner.mon.core.navigation.Reader
import com.akrippner.mon.feature.addreading.AddReadingEffect
import com.akrippner.mon.feature.addreading.AddReadingScreen
import com.akrippner.mon.feature.addreading.AddReadingViewModel
import com.akrippner.mon.feature.library.LibraryEffect
import com.akrippner.mon.feature.library.LibraryScreen
import com.akrippner.mon.feature.library.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MonNavHost() {
  val backStack = rememberNavBackStack(MonNavConfiguration, Library)
  val navigator = remember(backStack) { MonAppNavigator(backStack) }

  NavDisplay(
    backStack = backStack,
    onBack = navigator::back,
    entryProvider =
      entryProvider {
        entry<Library> {
          val viewModel = koinViewModel<LibraryViewModel>()
          val state by viewModel.state.collectAsStateWithLifecycle()
          LaunchedEffect(viewModel) {
            viewModel.effects.collect { effect ->
              when (effect) {
                is LibraryEffect.OpenReader -> navigator.navigateTo(Reader(effect.readingId))
                LibraryEffect.OpenAddReading -> navigator.navigateTo(AddReading)
              }
            }
          }
          LibraryScreen(state = state, onIntent = viewModel::onIntent)
        }
        entry<AddReading> {
          val viewModel = koinViewModel<AddReadingViewModel>()
          val state by viewModel.state.collectAsStateWithLifecycle()
          LaunchedEffect(viewModel) {
            viewModel.effects.collect { effect ->
              when (effect) {
                is AddReadingEffect.Saved -> navigator.back()
              }
            }
          }
          AddReadingScreen(state = state, onIntent = viewModel::onIntent, onBack = navigator::back)
        }
        // The reader screen does not exist yet; the key is routable so the library can link to it.
        entry<Reader> { key -> Text(text = key.readingId) }
      },
  )
}
