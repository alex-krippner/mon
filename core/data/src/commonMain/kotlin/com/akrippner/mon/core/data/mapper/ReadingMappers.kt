package com.akrippner.mon.core.data.mapper

import com.akrippner.mon.core.database.entity.PendingReadingRow
import com.akrippner.mon.core.database.entity.ReadingSummaryRow
import com.akrippner.mon.core.database.entity.ReadingTokenEntity
import com.akrippner.mon.core.database.entity.ReadingWithTokens
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.model.data.PendingReading
import com.akrippner.mon.core.model.data.Reading
import com.akrippner.mon.core.model.data.ReadingSummary

/** Returns null if the stored languages are not supported, so one bad row cannot hide the rest. */
internal fun ReadingWithTokens.toDomainOrNull(): Reading? {
  val sourceLanguage = Language.fromTagOrNull(reading.sourceLanguage) ?: return null
  val translationLanguage = Language.fromTagOrNull(reading.translationLanguage) ?: return null
  return Reading(
    id = reading.id,
    title = reading.title,
    text = reading.text,
    translation = reading.translation,
    sourceLanguage = sourceLanguage,
    translationLanguage = translationLanguage,
    tokens = tokens.sortedBy { it.tokenIndex }.map { it.toDomain() },
    isTokenized = reading.tokenizedWith != null,
    createdAt = reading.createdAt,
  )
}

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

internal fun ReadingSummaryRow.toDomainOrNull(): ReadingSummary? {
  val sourceLanguage = Language.fromTagOrNull(sourceLanguage) ?: return null
  val translationLanguage = Language.fromTagOrNull(translationLanguage) ?: return null

  return ReadingSummary(
    id = id,
    title = title,
    sourceLanguage = sourceLanguage,
    translationLanguage = translationLanguage,
    createdAt = createdAt,
    tokenCount = tokenCount,
    isTokenized = isTokenized,
  )
}

/** Returns null if the stored language is not supported, so one bad row cannot hide the rest. */
internal fun PendingReadingRow.toDomainOrNull(): PendingReading? =
  Language.fromTagOrNull(sourceLanguage)?.let { PendingReading(id, text, it) }
