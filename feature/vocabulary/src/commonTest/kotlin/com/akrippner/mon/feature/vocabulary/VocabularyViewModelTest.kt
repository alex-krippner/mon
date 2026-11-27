package com.akrippner.mon.feature.vocabulary

import com.akrippner.mon.core.domain.usecase.RemoveSavedWordUseCase
import com.akrippner.mon.core.domain.usecase.RestoreSavedWordUseCase
import com.akrippner.mon.core.domain.usecase.SetSavedWordNoteUseCase
import com.akrippner.mon.core.model.data.JlptLevel
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.ReadingSummary
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

class VocabularyViewModelTest {
  private val testDispatcher = StandardTestDispatcher()
  private val vocabulary = FakeVocabularyRepository()
  private val readings = FakeReadingRepository()

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun viewModel(readingId: String? = null) =
    VocabularyViewModel(
      readingId = readingId,
      vocabulary = vocabulary,
      readings = readings,
      removeWord = RemoveSavedWordUseCase(vocabulary),
      restoreWord = RestoreSavedWordUseCase(vocabulary),
      setNote = SetSavedWordNoteUseCase(vocabulary),
    )

  private fun seed() {
    vocabulary.words.value =
      listOf(
        savedWord("a", savedAt = 1),
        savedWord("b", savedAt = 2),
        savedWord("c", savedAt = 3),
        savedWord("d", readingId = "r2", savedAt = 1),
      )
  }

  @Test
  fun theTab_groupsWordsByReading_withTheNewestFirstAndAMoreCount() =
    runTest(testDispatcher) {
      seed()
      val viewModel = viewModel()
      advanceUntilIdle()

      val state = viewModel.state.value
      assertFalse(state.isLoading)
      assertEquals(listOf("r1", "r2"), state.sections.map { it.readingId })
      assertEquals(listOf("c", "b"), state.sections[0].words.map { it.id })
      assertEquals(1, state.sections[0].moreCount)
      assertEquals(4, state.totalWords)
    }

  @Test
  fun noSavedWords_isEmpty_notNoMatches() =
    runTest(testDispatcher) {
      val viewModel = viewModel()
      advanceUntilIdle()

      assertTrue(viewModel.state.value.isEmpty)
      assertFalse(viewModel.state.value.hasNoMatches)
    }

  @Test
  fun theListOfOneReading_showsOnlyItsWords_andItsTitle() =
    runTest(testDispatcher) {
      seed()
      readings.summaries.value =
        listOf(
          ReadingSummary("r1", "First", Language.JAPANESE, Language.ENGLISH, 10, true, 0),
          ReadingSummary("r2", "Second", Language.JAPANESE, Language.ENGLISH, 10, true, 0),
        )
      val viewModel = viewModel("r1")
      advanceUntilIdle()

      val state = viewModel.state.value
      assertTrue(state.forOneReading)
      assertEquals("First", state.readingTitle)
      assertEquals(listOf("c", "b", "a"), state.sections.single().words.map { it.id })
      assertEquals(0, state.sections.single().moreCount)
    }

  @Test
  fun searching_filtersAndASearchWithoutAMatch_hasNoMatches() =
    runTest(testDispatcher) {
      seed()
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(VocabularyIntent.QueryChanged("d"))
      assertEquals(listOf("r2"), viewModel.state.value.sections.map { it.readingId })

      viewModel.onIntent(VocabularyIntent.QueryChanged("zzz"))
      assertTrue(viewModel.state.value.hasNoMatches)
    }

  @Test
  fun theLevelFilter_isHiddenUntilAWordHasALevel() =
    runTest(testDispatcher) {
      seed()
      val viewModel = viewModel()
      advanceUntilIdle()
      assertFalse(viewModel.state.value.showLevelFilter)

      vocabulary.words.value += savedWord("e", jlpt = JlptLevel.N4)
      advanceUntilIdle()
      assertTrue(viewModel.state.value.showLevelFilter)

      viewModel.onIntent(VocabularyIntent.LevelToggled(JlptLevel.N4))
      assertEquals(listOf("e"), viewModel.state.value.sections.single().words.map { it.id })
      viewModel.onIntent(VocabularyIntent.ClearLevels)
      assertEquals(emptySet(), viewModel.state.value.levels)
    }

  @Test
  fun tappingAWord_opensItsSheetWithItsNote_andClosingStoresAChangedNote() =
    runTest(testDispatcher) {
      vocabulary.words.value = listOf(savedWord("a", note = "old"))
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(VocabularyIntent.WordTapped("a"))
      assertEquals("old", viewModel.state.value.openWord?.noteDraft)
      assertEquals("First", viewModel.state.value.openWord?.readingTitle)

      viewModel.onIntent(VocabularyIntent.NoteChanged("new"))
      assertTrue(vocabulary.noteWrites.isEmpty())
      viewModel.onIntent(VocabularyIntent.DismissWord)
      advanceUntilIdle()

      assertNull(viewModel.state.value.openWord)
      assertEquals(listOf<Pair<String, String?>>("a" to "new"), vocabulary.noteWrites)
    }

  @Test
  fun closingTheSheet_withoutEditingTheNote_writesNothing() =
    runTest(testDispatcher) {
      vocabulary.words.value = listOf(savedWord("a", note = "old"))
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(VocabularyIntent.WordTapped("a"))
      viewModel.onIntent(VocabularyIntent.DismissWord)
      advanceUntilIdle()

      assertTrue(vocabulary.noteWrites.isEmpty())
    }

  @Test
  fun removing_closesTheSheet_offersUndo_andUndoRestoresTheNote() =
    runTest(testDispatcher) {
      vocabulary.words.value = listOf(savedWord("a"))
      val viewModel = viewModel()
      advanceUntilIdle()
      viewModel.onIntent(VocabularyIntent.WordTapped("a"))
      viewModel.onIntent(VocabularyIntent.NoteChanged("keep me"))

      viewModel.onIntent(VocabularyIntent.Remove)
      advanceUntilIdle()

      val removed = assertIs<VocabularyEffect.ShowRemoved>(viewModel.effects.first()).word
      assertEquals("keep me", removed.note)
      assertNull(viewModel.state.value.openWord)
      assertTrue(viewModel.state.value.isEmpty)

      viewModel.onIntent(VocabularyIntent.UndoRemove(removed))
      advanceUntilIdle()

      assertEquals("keep me", vocabulary.words.value.single().note)
      assertEquals(1, viewModel.state.value.totalWords)
    }

  @Test
  fun showInReading_closesTheSheet_sendsTheEffect_andRecordsTheReadingAsOpened() =
    runTest(testDispatcher) {
      seed()
      val viewModel = viewModel()
      advanceUntilIdle()
      viewModel.onIntent(VocabularyIntent.WordTapped("d"))

      viewModel.onIntent(VocabularyIntent.ShowInReading)
      advanceUntilIdle()

      assertEquals(VocabularyEffect.ShowInReading("r2", "d"), viewModel.effects.first())
      assertNull(viewModel.state.value.openWord)
      assertEquals(listOf("r2"), readings.opened)
    }

  @Test
  fun seeAllAndBack_areSentAsEffects() =
    runTest(testDispatcher) {
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(VocabularyIntent.OpenReading("r2"))
      assertEquals(VocabularyEffect.OpenReadingVocabulary("r2"), viewModel.effects.first())
      viewModel.onIntent(VocabularyIntent.Back)
      assertEquals(VocabularyEffect.Close, viewModel.effects.first())
    }
}
