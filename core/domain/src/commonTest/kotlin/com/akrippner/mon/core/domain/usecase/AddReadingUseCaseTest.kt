package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.domain.fake.FakeReadingRepository
import com.akrippner.mon.core.domain.fake.FakeTokenizer
import com.akrippner.mon.core.domain.model.AddReadingInput
import com.akrippner.mon.core.domain.model.AddReadingInputError
import com.akrippner.mon.core.domain.model.AddReadingResult
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import kotlin.collections.emptyList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

class AddReadingUseCaseTest {
  private val repository = FakeReadingRepository(newReadingId = "new-id")
  private val tokenizer =
    FakeTokenizer(
      listOf(
        MonToken("私", dictionaryForm = "私", pronunciation = "わたし", partOfSpeech = listOf("代名詞")),
        MonToken("は", dictionaryForm = "は", pronunciation = "ワ", partOfSpeech = listOf("助詞")),
      )
    )

  private fun input(title: String = "Title", text: String = "私は", translation: String = "I") =
    AddReadingInput(
      title = title,
      text = text,
      translation = translation,
      sourceLanguage = Language.JAPANESE,
      translationLanguage = Language.ENGLISH,
    )

  private fun TestScope.useCase() =
    AddReadingUseCase(
      repository = repository,
      tokenizer = tokenizer,
      tokenizeDispatcher = UnconfinedTestDispatcher(testScheduler),
    )

  @Test
  fun blankTitle_isInvalid_andNothingIsStored() = runTest {
    val result = useCase().execute(input(title = "  "))

    assertEquals(AddReadingResult.Invalid(setOf(AddReadingInputError.MissingTitle)), result)
    assertEquals(emptyList(), repository.added)
    assertEquals(emptyList(), tokenizer.tokenizedTexts)
  }

  @Test
  fun blankTitleAndText_reportsBothErrors() = runTest {
    val result = useCase().execute(input(title = "", text = ""))

    assertEquals(
      AddReadingResult.Invalid(
        setOf(AddReadingInputError.MissingTitle, AddReadingInputError.MissingText)
      ),
      result,
    )
  }

  @Test
  fun input_isTrimmedBeforeTokenizingAndStoring() = runTest {
    useCase().execute(input(title = " Title ", text = " 私は\n", translation = " I "))

    assertEquals(listOf("私は"), tokenizer.tokenizedTexts)
    val added = repository.added.single()
    assertEquals("Title", added.title)
    assertEquals("私は", added.text)
    assertEquals("I", added.translation)
  }

  @Test
  fun tokens_areMappedInOrder_andLanguagesPassedThrough() = runTest {
    useCase().execute(input())

    val added = repository.added.single()
    assertEquals(Language.JAPANESE, added.sourceLanguage)
    assertEquals(Language.ENGLISH, added.translationLanguage)
    assertEquals(
      listOf(MonToken("私", "私", "わたし", listOf("代名詞")), MonToken("は", "は", "ワ", listOf("助詞"))),
      added.tokens,
    )
  }

  @Test
  fun success_carriesTheRepositoryId() = runTest {
    val result = useCase().execute(input())

    assertEquals(AddReadingResult.Success("new-id"), result)
  }

  @Test
  fun repositoryFailure_propagates() = runTest {
    repository.failure = IllegalStateException("disk full")

    assertFailsWith<IllegalStateException> { useCase().execute(input()) }
  }
}
