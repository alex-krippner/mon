package com.akrippner.mon.feature.dictionaries

import com.akrippner.mon.core.dictionary.pack.CatalogError
import com.akrippner.mon.core.dictionary.pack.PackError
import com.akrippner.mon.core.dictionary.pack.PackState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class DictionariesViewModelTest {
  private val testDispatcher = StandardTestDispatcher()
  private val manager = FakeDictionaryManager()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun refreshesTheCatalogOnStart_andShowsProgressUntilDone() =
    runTest(testDispatcher) {
      val gate = CompletableDeferred<Unit>()
      manager.refreshGate = gate
      val viewModel = DictionariesViewModel(manager)

      advanceUntilIdle()
      assertEquals(1, manager.refreshCount)
      assertTrue(viewModel.state.value.isRefreshing)

      gate.complete(Unit)
      advanceUntilIdle()
      assertFalse(viewModel.state.value.isRefreshing)
    }

  @Test
  fun packsOfTheManagerAreReflectedInState() =
    runTest(testDispatcher) {
      val viewModel = DictionariesViewModel(manager)
      advanceUntilIdle()
      assertEquals(emptyList(), viewModel.state.value.packs)

      val downloading = testPack(state = PackState.Downloading(0.5f))
      manager.packs.value = listOf(downloading)
      advanceUntilIdle()
      assertEquals(listOf(downloading), viewModel.state.value.packs)

      val installed = testPack(state = PackState.Installed)
      manager.packs.value = listOf(installed)
      advanceUntilIdle()
      assertEquals(listOf(installed), viewModel.state.value.packs)
    }

  @Test
  fun download_installsThePack() =
    runTest(testDispatcher) {
      val viewModel = DictionariesViewModel(manager)

      viewModel.onIntent(DictionariesIntent.Download("jmdict-eng"))
      advanceUntilIdle()

      assertEquals(listOf("jmdict-eng"), manager.installs)
    }

  @Test
  fun packsWithAVersionOnDisk_areInTheInstalledSection() =
    runTest(testDispatcher) {
      val viewModel = DictionariesViewModel(manager)
      val installed = testPack("a", PackState.Installed)
      val update = testPack("b", PackState.UpdateAvailable(testPack("b").info))
      val failedUpdate = testPack("c", PackState.Failed(PackError.NETWORK, testPack("c").info))
      val failed = testPack("d", PackState.Failed(PackError.NETWORK))
      val available = testPack("e")
      manager.packs.value = listOf(installed, update, failedUpdate, failed, available)
      advanceUntilIdle()

      assertEquals(listOf(installed, update, failedUpdate), viewModel.state.value.installed)
      assertEquals(listOf(failed, available), viewModel.state.value.available)
    }

  @Test
  fun aDownloadingUpdate_isInstalledEvenForANewViewModel() =
    runTest(testDispatcher) {
      // The screen is reopened while the manager downloads: the first list it sees is mid-download.
      manager.packs.value =
        listOf(
          testPack("a", PackState.Downloading(0.5f, installed = testPack("a").info)),
          testPack("b", PackState.Downloading(0.5f)),
        )
      val viewModel = DictionariesViewModel(manager)
      advanceUntilIdle()

      assertEquals(setOf("a"), viewModel.state.value.installedIds)
    }

  @Test
  fun aFinishedDownload_showsTheReadyNoteUntilDismissed() =
    runTest(testDispatcher) {
      val viewModel = DictionariesViewModel(manager)
      manager.packs.value = listOf(testPack(state = PackState.Downloading(1f)))
      advanceUntilIdle()
      assertNull(viewModel.state.value.readyPackName)

      manager.packs.value = listOf(testPack(state = PackState.Installed))
      advanceUntilIdle()
      assertEquals("JMdict (jmdict-eng)", viewModel.state.value.readyPackName)

      viewModel.onIntent(DictionariesIntent.DismissReady)
      assertNull(viewModel.state.value.readyPackName)
    }

  @Test
  fun aPackThatWasAlreadyInstalled_showsNoReadyNote() =
    runTest(testDispatcher) {
      val viewModel = DictionariesViewModel(manager)
      manager.packs.value = listOf(testPack(state = PackState.Installed))
      advanceUntilIdle()

      assertNull(viewModel.state.value.readyPackName)
    }

  @Test
  fun refresh_clearsTheReadyNote() =
    runTest(testDispatcher) {
      val viewModel = DictionariesViewModel(manager)
      manager.packs.value = listOf(testPack(state = PackState.Downloading(0.3f)))
      advanceUntilIdle()
      manager.packs.value = listOf(testPack(state = PackState.Installed))
      advanceUntilIdle()

      viewModel.onIntent(DictionariesIntent.Refresh)
      advanceUntilIdle()

      assertNull(viewModel.state.value.readyPackName)
    }

  @Test
  fun openLibrary_clearsTheReadyNoteAndSwitchesTab() =
    runTest(testDispatcher) {
      val viewModel = DictionariesViewModel(manager)
      manager.packs.value = listOf(testPack(state = PackState.Downloading(0.3f)))
      advanceUntilIdle()
      manager.packs.value = listOf(testPack(state = PackState.Installed))
      advanceUntilIdle()

      viewModel.onIntent(DictionariesIntent.OpenLibrary)

      assertNull(viewModel.state.value.readyPackName)
      assertEquals(DictionariesEffect.OpenLibrary, viewModel.effects.first())
    }

  @Test
  fun openPack_opensItsDetails() =
    runTest(testDispatcher) {
      val viewModel = DictionariesViewModel(manager)

      viewModel.onIntent(DictionariesIntent.OpenPack("jmdict-eng"))

      assertEquals(DictionariesEffect.OpenPack("jmdict-eng"), viewModel.effects.first())
    }

  @Test
  fun refresh_fetchesTheCatalogAgain() =
    runTest(testDispatcher) {
      val viewModel = DictionariesViewModel(manager)
      advanceUntilIdle()

      viewModel.onIntent(DictionariesIntent.Refresh)
      advanceUntilIdle()

      assertEquals(2, manager.refreshCount)
      assertFalse(viewModel.state.value.isRefreshing)
    }

  @Test
  fun refresh_isIgnoredWhileARefreshIsRunning() =
    runTest(testDispatcher) {
      manager.refreshGate = CompletableDeferred() // never completed
      val viewModel = DictionariesViewModel(manager)
      advanceUntilIdle()

      viewModel.onIntent(DictionariesIntent.Refresh)
      advanceUntilIdle()

      assertEquals(1, manager.refreshCount)
    }

  @Test
  fun failedRefresh_showsTheErrorUntilTheNextRefreshSucceeds() =
    runTest(testDispatcher) {
      manager.refreshResult = CatalogError.NETWORK
      val viewModel = DictionariesViewModel(manager)
      advanceUntilIdle()

      assertEquals(CatalogError.NETWORK, viewModel.state.value.refreshError)
      assertFalse(viewModel.state.value.isRefreshing)

      manager.refreshResult = null
      viewModel.onIntent(DictionariesIntent.Refresh)
      advanceUntilIdle()

      assertNull(viewModel.state.value.refreshError)
    }
}
