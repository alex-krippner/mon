package com.akrippner.mon.feature.dictionaries.detail

import com.akrippner.mon.core.packs.PackError
import com.akrippner.mon.core.packs.PackState
import com.akrippner.mon.feature.dictionaries.FakeDictionaryManager
import com.akrippner.mon.feature.dictionaries.testPack
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class PackDetailViewModelTest {
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
  fun showsThePackWithThatId() =
    runTest(testDispatcher) {
      val pack = testPack("b", PackState.Installed)
      manager.packs.value = listOf(testPack("a"), pack)

      val viewModel = PackDetailViewModel("b", manager)
      advanceUntilIdle()

      assertEquals(pack, viewModel.state.value.pack)
    }

  @Test
  fun aListedPack_doesNotRefresh() =
    runTest(testDispatcher) {
      manager.packs.value = listOf(testPack())

      PackDetailViewModel("jmdict-eng", manager)
      advanceUntilIdle()

      assertEquals(0, manager.refreshCount)
    }

  @Test
  fun restoredWithNothingLoaded_refreshesAndShowsThePack() =
    runTest(testDispatcher) {
      val pack = testPack(state = PackState.Installed)
      manager.onRefresh = { manager.packs.value = listOf(pack) }

      val viewModel = PackDetailViewModel("jmdict-eng", manager)
      val effects = mutableListOf<PackDetailEffect>()
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.effects.toList(effects)
      }
      advanceUntilIdle()

      assertEquals(1, manager.refreshCount)
      assertEquals(pack, viewModel.state.value.pack)
      assertTrue(effects.isEmpty())
    }

  @Test
  fun anUnknownPack_refreshesThenCloses() =
    runTest(testDispatcher) {
      val viewModel = PackDetailViewModel("missing", manager)
      advanceUntilIdle()

      assertEquals(1, manager.refreshCount)
      assertNull(viewModel.state.value.pack)
      assertEquals(PackDetailEffect.Close, viewModel.effects.first())
    }

  @Test
  fun aFailedRemoval_keepsTheScreenOpen() =
    runTest(testDispatcher) {
      val installed = testPack(state = PackState.Installed)
      manager.packs.value = listOf(installed)
      manager.onRemove = {
        manager.packs.value =
          listOf(testPack(state = PackState.Failed(PackError.REMOVE, installed.info)))
      }
      val viewModel = PackDetailViewModel("jmdict-eng", manager)
      val effects = mutableListOf<PackDetailEffect>()
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.effects.toList(effects)
      }
      advanceUntilIdle()

      viewModel.onIntent(PackDetailIntent.AskRemove)
      viewModel.onIntent(PackDetailIntent.ConfirmRemove)
      advanceUntilIdle()

      assertTrue(effects.isEmpty())
      assertEquals(PackError.REMOVE, (viewModel.state.value.pack?.state as PackState.Failed).error)
    }

  @Test
  fun removeAsksFirst_thenRemovesAndCloses() =
    runTest(testDispatcher) {
      manager.packs.value = listOf(testPack(state = PackState.Installed))
      val viewModel = PackDetailViewModel("jmdict-eng", manager)
      advanceUntilIdle()

      viewModel.onIntent(PackDetailIntent.AskRemove)
      assertTrue(viewModel.state.value.confirmingRemove)
      assertTrue(manager.removals.isEmpty())

      viewModel.onIntent(PackDetailIntent.ConfirmRemove)
      advanceUntilIdle()

      assertFalse(viewModel.state.value.confirmingRemove)
      assertEquals(listOf("jmdict-eng"), manager.removals)
      assertEquals(PackDetailEffect.Close, viewModel.effects.first())
    }

  @Test
  fun cancelRemove_keepsThePack() =
    runTest(testDispatcher) {
      manager.packs.value = listOf(testPack(state = PackState.Installed))
      val viewModel = PackDetailViewModel("jmdict-eng", manager)
      advanceUntilIdle()

      viewModel.onIntent(PackDetailIntent.AskRemove)
      viewModel.onIntent(PackDetailIntent.CancelRemove)
      advanceUntilIdle()

      assertFalse(viewModel.state.value.confirmingRemove)
      assertTrue(manager.removals.isEmpty())
    }

  @Test
  fun aPackThatLeavesTheList_closesTheScreenOnce() =
    runTest(testDispatcher) {
      manager.packs.value = listOf(testPack(state = PackState.Installed))
      val viewModel = PackDetailViewModel("jmdict-eng", manager)
      val effects = mutableListOf<PackDetailEffect>()
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.effects.toList(effects)
      }
      advanceUntilIdle()

      manager.packs.value = emptyList()
      advanceUntilIdle()

      assertEquals(listOf<PackDetailEffect>(PackDetailEffect.Close), effects)
    }

  @Test
  fun removingAPackThatThenLeavesTheList_closesTheScreenOnce() =
    runTest(testDispatcher) {
      manager.packs.value = listOf(testPack(state = PackState.Installed))
      manager.onRemove = { manager.packs.value = emptyList() } // an offline, installed-only pack
      val viewModel = PackDetailViewModel("jmdict-eng", manager)
      val effects = mutableListOf<PackDetailEffect>()
      backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
        viewModel.effects.toList(effects)
      }
      advanceUntilIdle()

      viewModel.onIntent(PackDetailIntent.AskRemove)
      viewModel.onIntent(PackDetailIntent.ConfirmRemove)
      viewModel.onIntent(PackDetailIntent.ConfirmRemove) // a double tap
      advanceUntilIdle()

      assertEquals(listOf("jmdict-eng"), manager.removals)
      assertEquals(listOf<PackDetailEffect>(PackDetailEffect.Close), effects)
    }

  @Test
  fun download_installsThePack() =
    runTest(testDispatcher) {
      manager.packs.value = listOf(testPack())
      val viewModel = PackDetailViewModel("jmdict-eng", manager)

      viewModel.onIntent(PackDetailIntent.Download)
      advanceUntilIdle()

      assertEquals(listOf("jmdict-eng"), manager.installs)
    }

  @Test
  fun back_closes() =
    runTest(testDispatcher) {
      val viewModel = PackDetailViewModel("jmdict-eng", manager)

      viewModel.onIntent(PackDetailIntent.Back)

      assertEquals(PackDetailEffect.Close, viewModel.effects.first())
    }
}
