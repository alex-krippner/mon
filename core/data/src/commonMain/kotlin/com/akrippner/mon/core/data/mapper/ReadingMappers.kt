package com.akrippner.mon.core.data.mapper

import com.akrippner.mon.core.database.entity.ReadingTokenEntity
import com.akrippner.mon.core.database.entity.ReadingWithTokens
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.Reading

internal fun ReadingWithTokens.toDomain(): Reading =
  Reading(
    id = reading.id,
    title = reading.title,
    text = reading.text,
    translation = reading.translation,
    sourceLanguage = reading.sourceLanguage,
    translationLanguage = reading.translationLanguage,
    tokens = tokens.sortedBy { it.tokenIndex }.map { it.toDomain() },
    createdAt = reading.createdAt,
  )

internal fun ReadingTokenEntity.toDomain(): MonToken =
  MonToken(
    surface = surface,
    dictionaryForm = dictionaryForm,
    pronunciation = pronunciation,
    partOfSpeech = partOfSpeech,
  )

internal fun MonToken.toEntity(readingId: String, tokenIndex: Int): ReadingTokenEntity =
  ReadingTokenEntity(
    readingId = readingId,
    tokenIndex = tokenIndex,
    surface = surface,
    pronunciation = pronunciation,
    dictionaryForm = dictionaryForm,
    partOfSpeech = partOfSpeech,
  )
