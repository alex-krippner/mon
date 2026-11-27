package com.akrippner.mon.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberDecoratedNavEntries
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
import com.akrippner.mon.core.navigation.ReadingVocabulary
import com.akrippner.mon.core.navigation.Vocabulary
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
import com.akrippner.mon.feature.reader.showReaderEffect
import com.akrippner.mon.feature.vocabulary.VocabularyEffect
import com.akrippner.mon.feature.vocabulary.VocabularyScreen
import com.akrippner.mon.feature.vocabulary.VocabularyViewModel
import com.akrippner.mon.feature.vocabulary.showVocabularyEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun MonNavHost() {
  // One back stack per tab. All of them stay alive, so a tab found again looks as it was left.
  val stacks = tabs.associate { it.key to rememberNavBackStack(MonNavConfiguration, it.key) }
  val selectedTabIndex = rememberSaveable { mutableIntStateOf(0) }
  val selectedTab = remember(selectedTabIndex) { SelectedTab(selectedTabIndex) }
  val navigator = remember(stacks) { MonAppNavigator(stacks, selectedTab) }

  // The bottom bar belongs to the app, not to a feature: features do not know about each other.
  // The keyboard covers the bottom of a phone, so the bar steps aside while it is open.
  val currentTab = tabs[selectedTabIndex.intValue]
  val top = stacks.getValue(currentTab.key).lastOrNull()
  val showBottomBar = !isKeyboardOpen()

  // A tab tap must not drop text typed on the Import screen. While it holds text, the tap asks the
  // screen to close instead, which shows its discard dialog.
  var importHasText by remember { mutableStateOf(false) }
  val importCloseRequests = remember { MutableSharedFlow<Unit>(extraBufferCapacity = 1) }

  val entryProviders =
    tabs.associate { tab ->
      tab.key to
        monEntryProvider(
          tab = tab.key,
          navigator = navigator,
          importCloseRequests = importCloseRequests,
          onImportHasText = { importHasText = it },
        )
    }
  // Each entry gets its own view models, cleared when the entry is popped. Every tab decorates its
  // own entries, so a tab that is not shown keeps its screens' state.
  val entriesByTab =
    tabs.associate { tab ->
      tab.key to
        rememberDecoratedNavEntries(
          backStack = stacks.getValue(tab.key),
          entryDecorators =
            listOf(
              rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
              rememberViewModelStoreNavEntryDecorator<NavKey>(),
            ),
          entryProvider = entryProviders.getValue(tab.key),
        )
    }
  // Another tab sits on top of Library's screens, so back from its root goes to where Library was.
  val shownEntries =
    if (currentTab.key == Library) {
      entriesByTab.getValue(Library)
    } else {
      entriesByTab.getValue(Library) + entriesByTab.getValue(currentTab.key)
    }

  Column(modifier = Modifier.fillMaxSize()) {
    NavDisplay(
      entries = shownEntries,
      onBack = navigator::back,
      // With the bar below, the screens must not also leave room for the system navigation bar.
      modifier =
        Modifier.weight(1f)
          .then(
            if (showBottomBar) Modifier.consumeWindowInsets(WindowInsets.navigationBars)
            else Modifier
          ),
    )
    if (showBottomBar) {
      BottomNavBar(
        selectedTab = currentTab.icon.tabId(),
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

/** The selected tab as a [MutableState] of its root key, backed by the saved tab index. */
private class SelectedTab(private val index: MutableIntState) : MutableState<MonNavKey> {
  override var value: MonNavKey
    get() = tabs[index.intValue].key
    set(newTab) {
      index.intValue = tabs.indexOfFirst { it.key == newTab }.coerceAtLeast(0)
    }

  override fun component1() = value

  override fun component2(): (MonNavKey) -> Unit = { value = it }
}

/** A top-level destination in the bottom bar, with the back stack that starts at [key]. */
private class Tab(val key: MonNavKey, val icon: IconId)

private val tabs =
  listOf(
    // The Import screen keeps the bar, with Library selected, as in the design.
    Tab(Library, Icon.Core.Library),
    Tab(Vocabulary, Icon.Core.Vocabulary),
    Tab(Dictionaries, Icon.Core.Dictionaries),
  )

/** The id [BottomNavBar] uses for a tab. */
private fun IconId.tabId() = category + name

@Composable
private fun monEntryProvider(
  /** The tab whose stack these entries are for. */
  tab: MonNavKey,
  navigator: MonAppNavigator,
  importCloseRequests: Flow<Unit>,
  onImportHasText: (Boolean) -> Unit,
): (NavKey) -> NavEntry<NavKey> =
  entryProvider<NavKey> {
    entry<Library>(clazzContentKey = { tabContentKey(tab, it) }) {
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
    entry<AddReading>(clazzContentKey = { tabContentKey(tab, it) }) {
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
    entry<Dictionaries>(clazzContentKey = { tabContentKey(tab, it) }) {
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
    entry<DictionaryDetail>(clazzContentKey = { tabContentKey(tab, it) }) { key ->
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
    entry<Vocabulary>(clazzContentKey = { tabContentKey(tab, it) }) {
      // No reading id: this lists the words of all readings.
      val viewModel = koinViewModel<VocabularyViewModel> { parametersOf() }
      VocabularyEntry(viewModel, navigator, close = null)
    }
    entry<ReadingVocabulary>(clazzContentKey = { tabContentKey(tab, it) }) { key ->
      val viewModel = koinViewModel<VocabularyViewModel> { parametersOf(key.readingId) }
      VocabularyEntry(viewModel, navigator, close = key)
    }
    entry<Reader>(clazzContentKey = { tabContentKey(tab, it) }) { key ->
      val viewModel =
        koinViewModel<ReaderViewModel> { parametersOf(key.readingId, key.focusWordId) }
      val state by viewModel.state.collectAsStateWithLifecycle()
      val snackbarHostState = remember { SnackbarHostState() }
      LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
          when (effect) {
            ReaderEffect.Close -> navigator.close(key)
            ReaderEffect.OpenDictionaries -> navigator.switchTab(Dictionaries)
            is ReaderEffect.OpenVocabulary ->
              navigator.navigateTo(ReadingVocabulary(effect.readingId))
            // A snackbar waits until it is gone; the next effect must not wait for it.
            else -> launch { snackbarHostState.showReaderEffect(effect, viewModel::onIntent) }
          }
        }
      }
      ReaderScreen(
        state = state,
        onIntent = viewModel::onIntent,
        snackbarHostState = snackbarHostState,
      )
    }
  }

/**
 * The Vocabulary tab and the list of one reading are the same screen, for different view models.
 */
@Composable
private fun VocabularyEntry(
  viewModel: VocabularyViewModel,
  navigator: MonAppNavigator,
  /** The key to pop on back; null for the tab, whose back is the navigator's. */
  close: NavKey?,
) {
  val state by viewModel.state.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  LaunchedEffect(viewModel) {
    viewModel.effects.collect { effect ->
      when (effect) {
        VocabularyEffect.Close -> if (close != null) navigator.close(close) else navigator.back()
        is VocabularyEffect.OpenReadingVocabulary ->
          navigator.navigateTo(ReadingVocabulary(effect.readingId))
        // Always opens a new reader on top, so back returns to this list as it was.
        is VocabularyEffect.ShowInReading ->
          navigator.navigateTo(Reader(effect.readingId, effect.wordId))
        // A snackbar waits until it is gone; the next effect must not wait for it.
        is VocabularyEffect.ShowRemoved ->
          launch { snackbarHostState.showVocabularyEffect(effect, viewModel::onIntent) }
      }
    }
  }
  VocabularyScreen(
    state = state,
    onIntent = viewModel::onIntent,
    snackbarHostState = snackbarHostState,
  )
}

/**
 * The content key of an entry. The same key can be on the stacks of two tabs (the list of a reading
 * reached from Library and from Vocabulary), and the view model store and saved state are kept by
 * content key, so the tab is part of it: each tab's screen has its own state.
 */
private fun tabContentKey(tab: MonNavKey, key: NavKey): String = "$tab/$key"
