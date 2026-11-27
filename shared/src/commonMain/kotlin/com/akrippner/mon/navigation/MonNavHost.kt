package com.akrippner.mon.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.akrippner.mon.core.designsystem.component.BottomNavBar
import com.akrippner.mon.core.designsystem.icon.Icon
import com.akrippner.mon.core.designsystem.icon.IconId
import com.akrippner.mon.core.designsystem.isKeyboardOpen
import com.akrippner.mon.core.navigation.AddReading
import com.akrippner.mon.core.navigation.Dictionaries
import com.akrippner.mon.core.navigation.DictionaryDetail
import com.akrippner.mon.core.navigation.Library
import com.akrippner.mon.core.navigation.MonNavConfiguration
import com.akrippner.mon.core.navigation.MonNavKey
import com.akrippner.mon.core.navigation.Reader
import com.akrippner.mon.feature.addreading.AddReadingEffect
import com.akrippner.mon.feature.addreading.AddReadingIntent
import com.akrippner.mon.feature.addreading.AddReadingScreen
import com.akrippner.mon.feature.addreading.AddReadingViewModel
import com.akrippner.mon.feature.dictionaries.DictionariesEffect
import com.akrippner.mon.feature.dictionaries.DictionariesScreen
import com.akrippner.mon.feature.dictionaries.DictionariesViewModel
import com.akrippner.mon.feature.dictionaries.detail.PackDetailEffect
import com.akrippner.mon.feature.dictionaries.detail.PackDetailScreen
import com.akrippner.mon.feature.dictionaries.detail.PackDetailViewModel
import com.akrippner.mon.feature.library.LibraryEffect
import com.akrippner.mon.feature.library.LibraryScreen
import com.akrippner.mon.feature.library.LibraryViewModel
import com.akrippner.mon.feature.reader.ReaderEffect
import com.akrippner.mon.feature.reader.ReaderScreen
import com.akrippner.mon.feature.reader.ReaderViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MonNavHost() {
  val backStack = rememberNavBackStack(MonNavConfiguration, Library)
  val navigator = remember(backStack) { MonAppNavigator(backStack) }

  // The bottom bar belongs to the app, not to a feature: features do not know about each other.
  val top = backStack.lastOrNull()
  val currentTab = tabs.firstOrNull { top != null && it.shows(top) }
  // The reader is no tab, but the design keeps the bar there, with no tab selected. The keyboard
  // covers the bottom of a phone, so the bar steps aside while it is open.
  val showBottomBar = (currentTab != null || top is Reader) && !isKeyboardOpen()

  // A tab tap must not drop text typed on the Import screen. While it holds text, the tap asks the
  // screen to close instead, which shows its discard dialog.
  var importHasText by remember { mutableStateOf(false) }
  val importCloseRequests = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

  Column(modifier = Modifier.fillMaxSize()) {
    NavDisplay(
      backStack = backStack,
      onBack = navigator::back,
      // Each entry gets its own view models, cleared when the entry is popped.
      entryDecorators =
        listOf(
          rememberSaveableStateHolderNavEntryDecorator(),
          rememberViewModelStoreNavEntryDecorator(),
        ),
      // With the bar below, the screens must not also leave room for the system navigation bar.
      modifier =
        Modifier.weight(1f)
          .then(
            if (showBottomBar) Modifier.consumeWindowInsets(WindowInsets.navigationBars)
            else Modifier
          ),
      entryProvider =
        monEntryProvider(
          navigator = navigator,
          importCloseRequests = importCloseRequests,
          onImportHasText = { importHasText = it },
        ),
    )
    if (showBottomBar) {
      BottomNavBar(
        selectedTab = currentTab?.icon?.tabId().orEmpty(),
        navTabs = tabs.map { it.icon },
        onTabSelected = { id ->
          if (top is AddReading && importHasText) {
            importCloseRequests.tryEmit(Unit)
          } else {
            tabs.firstOrNull { it.icon.tabId() == id }?.let { navigator.switchTab(it.key) }
          }
        },
      )
    }
  }
}

/**
 * A top-level destination in the bottom bar. The bar also stays visible, with this tab selected, on
 * the [children] screens that belong to the tab.
 */
private class Tab(
  val key: MonNavKey,
  val icon: IconId,
  val children: (NavKey) -> Boolean = { false },
) {
  fun shows(top: NavKey): Boolean = top == key || children(top)
}

private val tabs =
  listOf(
    // The Import screen keeps the bar, with Library selected, as in the design.
    Tab(Library, Icon.Core.Library, children = { it is AddReading }),
    Tab(Dictionaries, Icon.Core.Dictionaries, children = { it is DictionaryDetail }),
  )

/** The id [BottomNavBar] uses for a tab. */
private fun IconId.tabId() = category + name

@Composable
private fun monEntryProvider(
  navigator: MonAppNavigator,
  importCloseRequests: Flow<Unit>,
  onImportHasText: (Boolean) -> Unit,
): (NavKey) -> NavEntry<NavKey> =
  entryProvider<NavKey> {
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
            // Import leaves the back stack before the reader opens, so back from the reader goes
            // to Library, where the new text is on top.
            is AddReadingEffect.Imported -> {
              navigator.close(AddReading)
              navigator.navigateTo(Reader(effect.readingId))
            }
            AddReadingEffect.Closed -> navigator.close(AddReading)
          }
        }
      }
      LaunchedEffect(viewModel) {
        importCloseRequests.collect { viewModel.onIntent(AddReadingIntent.Close) }
      }
      val hasText = state.text.isNotBlank()
      LaunchedEffect(hasText) { onImportHasText(hasText) }
      DisposableEffect(Unit) { onDispose { onImportHasText(false) } }
      AddReadingScreen(state = state, onIntent = viewModel::onIntent)
    }
    entry<Dictionaries> {
      val viewModel = koinViewModel<DictionariesViewModel>()
      val state by viewModel.state.collectAsStateWithLifecycle()
      LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
          when (effect) {
            is DictionariesEffect.OpenPack -> navigator.navigateTo(DictionaryDetail(effect.packId))
            DictionariesEffect.OpenLibrary -> navigator.switchTab(Library)
          }
        }
      }
      DictionariesScreen(state = state, onIntent = viewModel::onIntent)
    }
    entry<DictionaryDetail> { key ->
      val viewModel = koinViewModel<PackDetailViewModel> { parametersOf(key.packId) }
      val state by viewModel.state.collectAsStateWithLifecycle()
      LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
          when (effect) {
            PackDetailEffect.Close -> navigator.close(key)
          }
        }
      }
      PackDetailScreen(state = state, onIntent = viewModel::onIntent)
    }
    entry<Reader> { key ->
      val viewModel = koinViewModel<ReaderViewModel> { parametersOf(key.readingId) }
      val state by viewModel.state.collectAsStateWithLifecycle()
      LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
          when (effect) {
            ReaderEffect.Close -> navigator.close(key)
            ReaderEffect.OpenDictionaries -> navigator.switchTab(Dictionaries)
          }
        }
      }
      ReaderScreen(state = state, onIntent = viewModel::onIntent)
    }
  }
