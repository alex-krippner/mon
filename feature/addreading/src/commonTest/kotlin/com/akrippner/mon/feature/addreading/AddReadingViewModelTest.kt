package com.akrippner.mon.feature.addreading

import com.akrippner.mon.core.domain.usecase.AddReadingUseCase
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

class AddReadingViewModelTest {
  private val testDispatcher = StandardTestDispatcher()
  private val repository = FakeReadingRepository()
  private lateinit var viewModel: AddReadingViewModel

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    val useCase =
      AddReadingUseCase(
        repository = repository,
        tokenizers = FakeTokenizerProvider(),
        requestTokenization = {},
      )
    viewModel = AddReadingViewModel(useCase)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun fillValidForm() {
    viewModel.onIntent(AddReadingIntent.TitleChanged("Title"))
    viewModel.onIntent(AddReadingIntent.TextChanged("こんにちは"))
    viewModel.onIntent(AddReadingIntent.TranslationChanged("Hello"))
  }

  @Test
  fun fieldIntents_updateState() {
    fillValidForm()

    val state = viewModel.state.value
    assertEquals("Title", state.title)
    assertEquals("こんにちは", state.text)
    assertEquals("Hello", state.translation)
    assertTrue(state.canSave)
  }

  @Test
  fun save_withBlankTitle_setsMissingTitle_andClearsOnEdit() =
    runTest(testDispatcher) {
      viewModel.onIntent(AddReadingIntent.TextChanged("こんにちは"))

      viewModel.onIntent(AddReadingIntent.Save)
      advanceUntilIdle()

      assertEquals(AddReadingError.MissingTitle, viewModel.state.value.error)
      assertFalse(viewModel.state.value.isSaving)
      assertEquals(emptyList(), repository.addedTitles)

      viewModel.onIntent(AddReadingIntent.TitleChanged("Title"))
      assertNull(viewModel.state.value.error)
    }

  @Test
  fun save_success_emitsSavedEffect_andStopsSaving() =
    runTest(testDispatcher) {
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Save)
      assertTrue(viewModel.state.value.isSaving)
      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals(AddReadingEffect.Saved("reading-1"), viewModel.effects.first())
      assertTrue(state.isSaved)
      assertFalse(state.isSaving)
      assertNull(state.error)
      assertFalse(state.canSave)
    }

  @Test
  fun save_whenRepositoryFails_setsSaveFailed() =
    runTest(testDispatcher) {
      repository.failure = IllegalStateException("disk full")
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Save)
      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals(AddReadingError.SaveFailed, state.error)
      assertFalse(state.isSaving)
      assertFalse(state.isSaved)
    }

  @Test
  fun save_whileSaving_isIgnored() =
    runTest(testDispatcher) {
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Save)
      viewModel.onIntent(AddReadingIntent.Save)
      advanceUntilIdle()

      assertEquals(listOf("Title"), repository.addedTitles)
    }
}
