package com.akrippner.mon.core.data.mapper

import com.akrippner.mon.core.database.entity.SavedWordEntity
import com.akrippner.mon.core.model.data.JlptLevel
import com.akrippner.mon.core.model.data.NewSavedWord
import com.akrippner.mon.core.model.data.SavedWord
import com.akrippner.mon.core.model.data.SentenceRef

internal fun SavedWordEntity.toDomain(): SavedWord =
  SavedWord(
    id = id,
    readingId = readingId,
    dictionaryForm = dictionaryForm,
    surface = surface,
    kana = kana,
    gloss = gloss,
    // An unknown name (a level a later version stored) shows no badge instead of failing the row.
    jlpt = jlpt?.let { name -> JlptLevel.entries.firstOrNull { it.name == name } },
    jmdictSeq = jmdictSeq,
    sentence =
      SentenceRef(
        text = sentenceText,
        wordStart = wordStart,
        wordEnd = wordEnd,
        textOffset = textOffset,
      ),
    note = note,
    savedAt = savedAt,
  )

internal fun SavedWord.toEntity(): SavedWordEntity =
  SavedWordEntity(
    id = id,
    readingId = readingId,
    dictionaryForm = dictionaryForm,
    surface = surface,
    kana = kana,
    gloss = gloss,
    jlpt = jlpt?.name,
    jmdictSeq = jmdictSeq,
    sentenceText = sentence.text,
    wordStart = sentence.wordStart,
    wordEnd = sentence.wordEnd,
    textOffset = sentence.textOffset,
    note = note,
    savedAt = savedAt,
  )

internal fun NewSavedWord.toEntity(id: String, savedAt: Long): SavedWordEntity =
  SavedWord(
      id = id,
      readingId = readingId,
      dictionaryForm = dictionaryForm,
      surface = surface,
      kana = kana,
      gloss = gloss,
      jlpt = jlpt,
      jmdictSeq = jmdictSeq,
      sentence = sentence,
      note = null,
      savedAt = savedAt,
    )
    .toEntity()
