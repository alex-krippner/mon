package com.akrippner.mon.core.dictionary

import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language

/**
 * What every installed pack opens into. It only says where the data comes from; what a reader can
 * answer is a capability interface on top of it, such as [MonDictionary] for word lookups. A pack
 * of another kind (kanji, example sentences) adds its own capability next to it.
 */
interface PackReader {
  val source: DictionarySource
  /** Language of the headwords. */
  val sourceLanguage: Language
  /** Language of the glosses. */
  val glossLanguage: Language
}

/**
 * The reader was closed because its pack is being updated or removed, so it can answer nothing. Any
 * call on any reader can throw it. This is not "nothing found": fetch the readers again and repeat
 * the call.
 */
class DictionaryClosedException : IllegalStateException("The pack reader is closed")
