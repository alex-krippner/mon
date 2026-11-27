package com.akrippner.mon.core.data.internal

import com.akrippner.mon.core.model.data.MAX_NOTE_LENGTH

/** The note as it is stored: trimmed, cut to [MAX_NOTE_LENGTH] characters, null if blank. */
internal fun String?.toStoredNote(): String? =
  this?.trim()?.take(MAX_NOTE_LENGTH)?.trimEnd()?.takeIf { it.isNotEmpty() }
