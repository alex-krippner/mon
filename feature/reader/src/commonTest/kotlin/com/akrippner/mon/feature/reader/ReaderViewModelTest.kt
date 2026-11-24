package com.akrippner.mon.feature.reader

import com.akrippner.mon.core.dictionary.DictionaryUnavailableException
import com.akrippner.mon.core.domain.usecase.LookupWordUseCase
import com.akrippner.mon.core.model.data.DictionaryEntry
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.Sense
import com.akrippner.mon.core.ui.TokenState
import com.akrippner.mon.core.ui.lookup.LookupUiState
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
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

class ReaderViewModelTest {
  private val testDispatcher = StandardTestDispatcher()
  private val repository = FakeReadingRepository()
  private val dictionary = FakeDictionary(entries = listOf(entry()))

  @BeforeTest
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
  }

  @AfterTest
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun token(surface: String, reading: String = surface, pos: String = "名詞") =
    MonToken(surface, dictionaryForm = surface, pronunciation = reading, partOfSpeech = listOf(pos))

  // Indexes: 0 先生, 1 は, 2 。, 3 \n, 4 本
  private val tokens =
    listOf(
      token("先生", "センセイ"),
      token("は", pos = "助詞"),
      token("。", pos = "補助記号"),
      token("\n", pos = "空白"),
      token("本", "ホン"),
    )

  private fun reading(
    tokens: List<MonToken>,
    tokenized: Boolean = true,
    language: Language = Language.JAPANESE,
  ) =
    Reading(
      id = "r1",
      title = "Title",
      text = "先生は。\n本",
      translation = "",
      sourceLanguage = language,
      translationLanguage = Language.ENGLISH,
      tokens = tokens,
      isTokenized = tokenized,
      createdAt = 0L,
    )

  private fun entry() =
    DictionaryEntry(
      id = "jmdict:1",
      source = DictionarySource("jmdict", "JMdict", "attribution", "1"),
      headwords = listOf(Headword("先生", "せんせい")),
      senses = listOf(Sense(listOf("teacher"))),
    )

  private fun viewModel(vararg dictionaries: FakeDictionary = arrayOf(dictionary)) =
    ReaderViewModel(
      readingId = "r1",
      readings = repository,
      lookupWord = LookupWordUseCase(FakeDictionaryProvider(dictionaries.toList())),
    )

  @Test
  fun initialState_isLoading_untilTheReadingArrives() =
    runTest(testDispatcher) {
      val viewModel = viewModel()

      assertTrue(viewModel.state.value.isLoading)
      advanceUntilIdle()
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun aTokenizedReading_isShownAsParagraphsWithFurigana() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      val state = viewModel.state.value
      assertEquals("Title", state.title)
      assertFalse(state.isPending)
      assertNull(state.error)
      assertEquals(
        listOf(listOf(0, 1, 2), listOf(4)),
        state.paragraphs.map { p -> p.map { it.index } },
      )
      assertEquals("せんせい", state.paragraphs[0][0].display.reading)
    }

  @Test
  fun aPendingReading_showsPlainText_thenSwitchesToTokensWhenAnalysed() =
    runTest(testDispatcher) {
      repository.reading.value = reading(emptyList(), tokenized = false)
      val viewModel = viewModel()
      advanceUntilIdle()

      assertTrue(viewModel.state.value.isPending)
      assertEquals("先生は。\n本", viewModel.state.value.plainText)
      assertTrue(viewModel.state.value.paragraphs.isEmpty())

      repository.reading.value = reading(tokens)
      advanceUntilIdle()

      assertFalse(viewModel.state.value.isPending)
      assertEquals(2, viewModel.state.value.paragraphs.size)
    }

  @Test
  fun aPendingReading_mayGetATokenizerOnlyInALanguageThatHasOne() =
    runTest(testDispatcher) {
      repository.reading.value = reading(emptyList(), tokenized = false)
      val viewModel = viewModel()
      advanceUntilIdle()
      assertTrue(viewModel.state.value.tokenizerMayArrive)

      repository.reading.value = reading(emptyList(), tokenized = false, language = Language.GERMAN)
      advanceUntilIdle()
      assertFalse(viewModel.state.value.tokenizerMayArrive)
    }

  @Test
  fun aMissingReading_isAnError() =
    runTest(testDispatcher) {
      val viewModel = viewModel()
      advanceUntilIdle()

      assertEquals(ReaderError.NotFound, viewModel.state.value.error)
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun aReadingThatCannotBeObserved_isALoadError() =
    runTest(testDispatcher) {
      repository.observeFailure = IllegalStateException("db")
      val viewModel = viewModel()
      advanceUntilIdle()

      assertEquals(ReaderError.LoadFailed, viewModel.state.value.error)
      assertFalse(viewModel.state.value.isLoading)
    }

  @Test
  fun aDeletedReading_becomesAnErrorAndClosesTheSheet() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()
      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      advanceUntilIdle()

      repository.reading.value = null
      advanceUntilIdle()

      assertEquals(ReaderError.NotFound, viewModel.state.value.error)
      assertNull(viewModel.state.value.selection)
    }

  @Test
  fun tappingAWord_showsItsDictionaryEntries() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      assertIs<LookupUiState.Loading>(viewModel.state.value.selection?.lookup)
      advanceUntilIdle()

      val selection = viewModel.state.value.selection!!
      assertEquals(0, selection.index)
      assertEquals("先生", selection.token.surface)
      val found = assertIs<LookupUiState.Found>(selection.lookup)
      assertEquals("先生", found.entries.single().surface)
      assertEquals("先生", dictionary.queries.single().lemma)
    }

  @Test
  fun tappingPunctuation_doesNothing() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.TokenTapped(2))
      viewModel.onIntent(ReaderIntent.TokenTapped(3))
      viewModel.onIntent(ReaderIntent.TokenTapped(99))
      advanceUntilIdle()

      assertNull(viewModel.state.value.selection)
      assertTrue(dictionary.queries.isEmpty())
    }

  @Test
  fun aWordTheDictionaryDoesNotKnow_isNotFound() =
    runTest(testDispatcher) {
      dictionary.entries = emptyList()
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      advanceUntilIdle()

      assertEquals(LookupUiState.NotFound("先生"), viewModel.state.value.selection?.lookup)
    }

  @Test
  fun withoutADictionary_theSheetSaysSo() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel(/* no dictionaries */ *emptyArray())
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      advanceUntilIdle()

      assertEquals(LookupUiState.NoDictionaryInstalled, viewModel.state.value.selection?.lookup)
    }

  @Test
  fun aDictionaryThatCannotAnswer_isUnavailable_andRetryAsksAgain() =
    runTest(testDispatcher) {
      dictionary.failure = DictionaryUnavailableException("replaced")
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      advanceUntilIdle()
      assertEquals(LookupUiState.Unavailable, viewModel.state.value.selection?.lookup)

      dictionary.failure = null
      viewModel.onIntent(ReaderIntent.RetryLookup)
      advanceUntilIdle()

      assertIs<LookupUiState.Found>(viewModel.state.value.selection?.lookup)
      assertEquals(2, dictionary.queries.size)
    }

  @Test
  fun aSecondTap_replacesTheFirstLookup_evenIfTheFirstFinishesLater() =
    runTest(testDispatcher) {
      val slow = CompletableDeferred<Unit>()
      dictionary.gates["先生"] = slow
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      advanceUntilIdle()
      viewModel.onIntent(ReaderIntent.TokenTapped(4))
      advanceUntilIdle()
      slow.complete(Unit)
      advanceUntilIdle()

      val selection = viewModel.state.value.selection!!
      assertEquals(4, selection.index)
      assertEquals("本", selection.token.surface)
      assertIs<LookupUiState.Found>(selection.lookup)
    }

  @Test
  fun dismissingTheSheet_clearsTheSelection_andIgnoresALateAnswer() =
    runTest(testDispatcher) {
      val slow = CompletableDeferred<Unit>()
      dictionary.gates["先生"] = slow
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      advanceUntilIdle()
      viewModel.onIntent(ReaderIntent.DismissLookup)
      slow.complete(Unit)
      advanceUntilIdle()

      assertNull(viewModel.state.value.selection)
    }

  @Test
  fun newTokens_closeTheSheet_soItNeverPointsAtOtherWords() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()
      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      advanceUntilIdle()
      assertEquals(0, viewModel.state.value.selection?.index)

      repository.reading.value = reading(listOf(token("犬", "イヌ")))
      advanceUntilIdle()

      assertNull(viewModel.state.value.selection)
      assertEquals(1, viewModel.state.value.paragraphs.single().size)
    }

  @Test
  fun retryWithoutASelection_doesNothing() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.RetryLookup)
      advanceUntilIdle()

      assertNull(viewModel.state.value.selection)
      assertTrue(dictionary.queries.isEmpty())
    }

  @Test
  fun tappingTheSameWordAgain_looksItUpAgain() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      advanceUntilIdle()
      viewModel.onIntent(ReaderIntent.TokenTapped(0))
      advanceUntilIdle()

      assertEquals(2, dictionary.queries.size)
      assertIs<LookupUiState.Found>(viewModel.state.value.selection?.lookup)
    }

  @Test
  fun toggleFurigana_flipsTheFlag_andSurvivesReadingUpdates() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()
      assertTrue(viewModel.state.value.showFurigana)

      viewModel.onIntent(ReaderIntent.ToggleFurigana)
      repository.reading.value = reading(tokens).copy(title = "New title")
      advanceUntilIdle()

      assertFalse(viewModel.state.value.showFurigana)
      assertEquals("New title", viewModel.state.value.title)
    }

  @Test
  fun backAndOpenDictionaries_areSentAsEffects() =
    runTest(testDispatcher) {
      val viewModel = viewModel()
      advanceUntilIdle()

      viewModel.onIntent(ReaderIntent.Back)
      assertEquals(ReaderEffect.Close, viewModel.effects.first())
      viewModel.onIntent(ReaderIntent.OpenDictionaries)
      assertEquals(ReaderEffect.OpenDictionaries, viewModel.effects.first())
    }

  @Test
  fun tokenStates_areDefaultForWordsAndPunctuationForSymbols() =
    runTest(testDispatcher) {
      repository.reading.value = reading(tokens)
      val viewModel = viewModel()
      advanceUntilIdle()

      val states = viewModel.state.value.paragraphs[0].map { it.display.state }
      assertEquals(listOf(TokenState.Default, TokenState.Default, TokenState.Punctuation), states)
    }
}
