package com.akrippner.mon.core.domain.usecase

import com.akrippner.mon.core.data.VocabularyRepository
import com.akrippner.mon.core.model.data.SavedWord

/** Removes a word from the vocabulary. Returns it, so the caller can offer Undo. */
class RemoveSavedWordUseCase(private val vocabulary: VocabularyRepository) {
  suspend fun execute(id: String): SavedWord? = vocabulary.remove(id)
}

/** Puts a removed word back, with its note. */
class RestoreSavedWordUseCase(private val vocabulary: VocabularyRepository) {
  suspend fun execute(word: SavedWord) = vocabulary.restore(word)
}

/** Sets or clears the note of a saved word. A blank note clears it. */
class SetSavedWordNoteUseCase(private val vocabulary: VocabularyRepository) {
  suspend fun execute(id: String, note: String?) = vocabulary.setNote(id, note)
}
