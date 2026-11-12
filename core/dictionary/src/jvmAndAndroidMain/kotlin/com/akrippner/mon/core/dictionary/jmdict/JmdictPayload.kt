package com.akrippner.mon.core.dictionary.jmdict

import com.akrippner.mon.core.model.data.GrammaticalGender
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Label
import com.akrippner.mon.core.model.data.Sense
import kotlinx.serialization.Serializable

// The JSON stored in `entry.payload`. These classes are the storage format and are kept apart from
// the :core:model types, so the file format can change without touching the models.

@Serializable
internal class EntryPayload(
  val headwords: List<HeadwordDto>,
  val senses: List<SenseDto>,
  val labels: List<LabelDto> = emptyList(),
)

@Serializable internal class HeadwordDto(val text: String, val reading: String? = null)

@Serializable
internal class SenseDto(val glosses: List<String>, val labels: List<LabelDto> = emptyList())

@Serializable internal class LabelDto(val kind: String, val text: String)

private const val POS = "pos"
private const val GENDER = "gender"
private const val REGISTER = "register"
private const val DOMAIN = "domain"
private const val FREQUENCY = "frequency"
private const val OTHER = "other"

internal fun Label.toDto(): LabelDto =
  when (this) {
    is Label.PartOfSpeech -> LabelDto(POS, text)
    is Label.Gender -> LabelDto(GENDER, gender.name)
    is Label.Register -> LabelDto(REGISTER, text)
    is Label.Domain -> LabelDto(DOMAIN, text)
    is Label.Frequency -> LabelDto(FREQUENCY, text)
    is Label.Other -> LabelDto(OTHER, text)
  }

/** An unknown kind becomes [Label.Other], so an older app can still read a newer pack. */
internal fun LabelDto.toLabel(): Label =
  when (kind) {
    POS -> Label.PartOfSpeech(text)
    GENDER ->
      GrammaticalGender.entries.firstOrNull { it.name == text }?.let(Label::Gender)
        ?: Label.Other(text)
    REGISTER -> Label.Register(text)
    DOMAIN -> Label.Domain(text)
    FREQUENCY -> Label.Frequency(text)
    else -> Label.Other(text)
  }

internal fun Headword.toDto(): HeadwordDto = HeadwordDto(text, reading)

internal fun HeadwordDto.toHeadword(): Headword = Headword(text, reading)

internal fun Sense.toDto(): SenseDto = SenseDto(glosses, labels.map { it.toDto() })

internal fun SenseDto.toSense(): Sense = Sense(glosses, labels.map { it.toLabel() })

internal fun JmdictRecord.toPayload(): EntryPayload =
  EntryPayload(
    headwords = headwords.map { it.toDto() },
    senses = senses.map { it.toDto() },
    labels = labels.map { it.toDto() },
  )
