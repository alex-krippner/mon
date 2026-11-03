package com.akrippner.mon.feature.library

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.ReadingSummary
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class LibraryViewModelTest {
  private val testDispatcher = StandardTestDispatcher()
  private val repository = FakeReadingRepository()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun summary(id: String) =
    ReadingSummary(
      id = id,
      title = "Title $id",
      sourceLanguage = Language.JAPANESE,
      translationLanguage = Language.ENGLISH,
      tokenCount = 3,
      createdAt = 0L,
    )

  @Test
  fun initialState_isLoading_untilFirstEmission() =
    runTest(testDispatcher) {
      val viewModel = LibraryViewModel(repository)

      assertTrue(viewModel.state.value.isLoading)
      advanceUntilIdle()
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun readings_areReflectedInState_andUpdateOnChange() =
    runTest(testDispatcher) {
      repository.summaries.value = listOf(summary("a"))
      val viewModel = LibraryViewModel(repository)
      advanceUntilIdle()

      assertEquals(listOf(summary("a")), viewModel.state.value.readings)
      assertNull(viewModel.state.value.error)

      repository.summaries.value = listOf(summary("b"), summary("a"))
      advanceUntilIdle()

      assertEquals(listOf(summary("b"), summary("a")), viewModel.state.value.readings)
    }

  @Test
  fun loadFailure_setsTypedError_andStopsLoading() =
    runTest(testDispatcher) {
      repository.observeFailure = IllegalStateException("db closed")
      val viewModel = LibraryViewModel(repository)
      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals(LibraryError.LoadFailed, state.error)
      assertFalse(state.isLoading)
    }

  @Test
  fun openReading_sendsOpenReaderEffect_andRecordsUsage() =
    runTest(testDispatcher) {
      val viewModel = LibraryViewModel(repository)

      viewModel.onIntent(LibraryIntent.OpenReading("a"))
      advanceUntilIdle()

      assertEquals(LibraryEffect.OpenReader("a"), viewModel.effects.first())
      assertEquals(listOf("a"), repository.openedIds)
    }

  @Test
  fun openReading_stillNavigates_whenUsageTrackingFails() =
    runTest(testDispatcher) {
      repository.recordFailure = IllegalStateException("reading deleted")
      val viewModel = LibraryViewModel(repository)

      viewModel.onIntent(LibraryIntent.OpenReading("a"))
      advanceUntilIdle()

      assertEquals(LibraryEffect.OpenReader("a"), viewModel.effects.first())
      assertEquals(emptyList(), repository.openedIds)
    }

  @Test
  fun addReading_sendsOpenAddReadingEffect() =
    runTest(testDispatcher) {
      val viewModel = LibraryViewModel(repository)

      viewModel.onIntent(LibraryIntent.AddReading)

      assertEquals(LibraryEffect.OpenAddReading, viewModel.effects.first())
    }
}
