package com.akrippner.mon.feature.addreading

import com.akrippner.mon.core.domain.usecase.AddReadingUseCase
import com.akrippner.mon.feature.addreading.file.MAX_FILE_BYTES
import com.akrippner.mon.feature.addreading.file.PickedFile
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
  }

  private fun textFile(
    name: String,
    content: String = "吾輩は猫である",
    size: Long? = content.encodeToByteArray().size.toLong(),
    onRead: () -> Unit = {},
  ) =
    PickedFile(name, size) {
      onRead()
      content.encodeToByteArray()
    }

  @Test
  fun fieldIntents_updateState() {
    fillValidForm()

    val state = viewModel.state.value
    assertEquals("Title", state.title)
    assertEquals("こんにちは", state.text)
    assertTrue(state.canImport)
  }

  @Test
  fun titleHint_showsOnlyForTextWithoutTitle() {
    assertFalse(viewModel.state.value.showTitleHint)

    viewModel.onIntent(AddReadingIntent.TextChanged("こんにちは"))
    assertTrue(viewModel.state.value.showTitleHint)

    viewModel.onIntent(AddReadingIntent.TitleChanged("Title"))
    assertFalse(viewModel.state.value.showTitleHint)
  }

  @Test
  fun import_withBlankTitle_setsMissingTitle_andClearsOnEdit() =
    runTest(testDispatcher) {
      viewModel.onIntent(AddReadingIntent.TextChanged("こんにちは"))

      viewModel.onIntent(AddReadingIntent.Import)
      advanceUntilIdle()

      assertEquals(AddReadingError.MissingTitle, viewModel.state.value.error)
      assertFalse(viewModel.state.value.isSaving)
      assertEquals(emptyList(), repository.addedTitles)

      viewModel.onIntent(AddReadingIntent.TitleChanged("Title"))
      assertNull(viewModel.state.value.error)
    }

  @Test
  fun import_success_emitsImportedEffect_andStopsSaving() =
    runTest(testDispatcher) {
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Import)
      assertTrue(viewModel.state.value.isSaving)
      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals(AddReadingEffect.Imported("reading-1"), viewModel.effects.first())
      assertTrue(state.isSaved)
      assertFalse(state.isSaving)
      assertNull(state.error)
      assertFalse(state.canImport)
    }

  @Test
  fun import_whenRepositoryFails_setsSaveFailed_andKeepsTheText() =
    runTest(testDispatcher) {
      repository.failure = IllegalStateException("disk full")
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Import)
      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals(AddReadingError.SaveFailed, state.error)
      assertFalse(state.isSaving)
      assertFalse(state.isSaved)
      assertEquals("Title", state.title)
      assertEquals("こんにちは", state.text)
      assertTrue(state.canImport)
    }

  @Test
  fun import_whileSaving_isIgnored() =
    runTest(testDispatcher) {
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Import)
      viewModel.onIntent(AddReadingIntent.Import)
      advanceUntilIdle()

      assertEquals(listOf("Title"), repository.addedTitles)
    }

  @Test
  fun edits_areIgnoredWhileSaving() =
    runTest(testDispatcher) {
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Import)
      viewModel.onIntent(AddReadingIntent.TitleChanged("Other"))
      viewModel.onIntent(AddReadingIntent.TextChanged("Other"))

      assertEquals("Title", viewModel.state.value.title)
      assertEquals("こんにちは", viewModel.state.value.text)
      advanceUntilIdle()
    }

  @Test
  fun close_withoutText_closesAtOnce() =
    runTest(testDispatcher) {
      viewModel.onIntent(AddReadingIntent.TitleChanged("Only a title"))

      viewModel.onIntent(AddReadingIntent.Close)

      assertEquals(AddReadingEffect.Closed, viewModel.effects.first())
      assertFalse(viewModel.state.value.showDiscardDialog)
    }

  @Test
  fun close_withText_asksBeforeDiscarding() =
    runTest(testDispatcher) {
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Close)
      assertTrue(viewModel.state.value.showDiscardDialog)

      viewModel.onIntent(AddReadingIntent.DiscardDismissed)
      assertFalse(viewModel.state.value.showDiscardDialog)
      assertEquals("こんにちは", viewModel.state.value.text)

      viewModel.onIntent(AddReadingIntent.Close)
      viewModel.onIntent(AddReadingIntent.DiscardConfirmed)
      assertFalse(viewModel.state.value.showDiscardDialog)
      assertEquals(AddReadingEffect.Closed, viewModel.effects.first())
    }

  @Test
  fun close_whileSaving_isIgnored() =
    runTest(testDispatcher) {
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Import)
      viewModel.onIntent(AddReadingIntent.Close)

      assertFalse(viewModel.state.value.showDiscardDialog)
      advanceUntilIdle()
      // Only the import's own effect was sent.
      assertEquals(AddReadingEffect.Imported("reading-1"), viewModel.effects.first())
    }

  @Test
  fun fileChosen_fillsTextAndAnEmptyTitle() =
    runTest(testDispatcher) {
      viewModel.onIntent(AddReadingIntent.FileChosen(textFile("neko.txt", "吾輩は猫である")))
      assertEquals("neko.txt", viewModel.state.value.readingFileName)
      assertTrue(viewModel.state.value.isLocked)

      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals("吾輩は猫である", state.text)
      assertEquals("neko", state.title)
      assertNull(state.readingFileName)
      assertNull(state.error)
      assertTrue(state.canImport)
    }

  @Test
  fun fileChosen_replacesTheText_butKeepsATypedTitle() =
    runTest(testDispatcher) {
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.FileChosen(textFile("neko.txt", "吾輩は猫である")))
      advanceUntilIdle()

      assertEquals("Title", viewModel.state.value.title)
      assertEquals("吾輩は猫である", viewModel.state.value.text)
    }

  @Test
  fun fileChosen_notText_isRefusedWithoutReading() =
    runTest(testDispatcher) {
      var read = false

      viewModel.onIntent(
        AddReadingIntent.FileChosen(textFile("notes.md", onRead = { read = true }))
      )
      advanceUntilIdle()

      assertEquals(AddReadingError.FileNotText, viewModel.state.value.error)
      assertFalse(read)
      assertNull(viewModel.state.value.readingFileName)
    }

  @Test
  fun fileChosen_tooLarge_isRefusedWithoutReading_andKeepsTheText() =
    runTest(testDispatcher) {
      var read = false
      fillValidForm()

      viewModel.onIntent(
        AddReadingIntent.FileChosen(
          textFile("big.txt", size = MAX_FILE_BYTES + 1, onRead = { read = true })
        )
      )
      advanceUntilIdle()

      assertEquals(AddReadingError.FileTooLarge, viewModel.state.value.error)
      assertFalse(read)
      assertEquals("こんにちは", viewModel.state.value.text)
    }

  @Test
  fun fileChosen_whenReadingFails_setsUnreadable_andKeepsTheText() =
    runTest(testDispatcher) {
      fillValidForm()
      val failing = PickedFile("neko.txt", 10L) { error("permission revoked") }

      viewModel.onIntent(AddReadingIntent.FileChosen(failing))
      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals(AddReadingError.FileUnreadable, state.error)
      assertNull(state.readingFileName)
      assertEquals("こんにちは", state.text)
    }

  @Test
  fun fileChosen_emptyFile_setsFileEmpty() =
    runTest(testDispatcher) {
      viewModel.onIntent(AddReadingIntent.FileChosen(textFile("empty.txt", content = "  \n")))
      advanceUntilIdle()

      assertEquals(AddReadingError.FileEmpty, viewModel.state.value.error)
      assertEquals("", viewModel.state.value.text)
    }

  @Test
  fun fileChosen_whileSaving_isIgnored() =
    runTest(testDispatcher) {
      fillValidForm()

      viewModel.onIntent(AddReadingIntent.Import)
      viewModel.onIntent(AddReadingIntent.FileChosen(textFile("neko.txt")))
      advanceUntilIdle()

      assertEquals("こんにちは", viewModel.state.value.text)
    }

  @Test
  fun whileReadingAFile_importCloseEditsAndAnotherFileAreIgnored() =
    runTest(testDispatcher) {
      fillValidForm()
      viewModel.onIntent(AddReadingIntent.FileChosen(textFile("neko.txt", "吾輩は猫である")))
      assertTrue(viewModel.state.value.isReadingFile)

      viewModel.onIntent(AddReadingIntent.Import)
      viewModel.onIntent(AddReadingIntent.Close)
      viewModel.onIntent(AddReadingIntent.TextChanged("Other"))
      viewModel.onIntent(AddReadingIntent.FileChosen(textFile("second.txt", "二つ目")))

      assertFalse(viewModel.state.value.isSaving)
      assertFalse(viewModel.state.value.showDiscardDialog)
      assertEquals("こんにちは", viewModel.state.value.text)

      advanceUntilIdle()

      assertEquals("吾輩は猫である", viewModel.state.value.text)
      assertEquals(emptyList(), repository.addedTitles)
    }

  @Test
  fun afterImporting_closeAndDiscardDoNothingMore() =
    runTest(testDispatcher) {
      fillValidForm()
      viewModel.onIntent(AddReadingIntent.Import)
      advanceUntilIdle()

      viewModel.onIntent(AddReadingIntent.Close)

      assertFalse(viewModel.state.value.showDiscardDialog)
    }
}
