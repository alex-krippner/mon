package com.akrippner.mon.core.tokenizer

import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.MonToken
import kotlinx.coroutines.flow.Flow

/**
 * Splits text of one [language] into tokens. How it does that (a local file, a server) is up to the
 * implementation.
 */
interface Tokenizer {
  val language: Language

  /**
   * Names the tokenizer and its version, e.g. `sudachi-core@20250825`. A reading remembers which
   * tokenizer produced its tokens, so that a different one can analyse it again later.
   */
  val id: String

  /**
   * Runs on the tokenizer's own thread, so it is safe to call from any coroutine.
   *
   * @throws TokenizerUnavailableException if it can't answer right now. This says nothing about the
   *   text; try again later.
   */
  suspend fun tokenize(text: String): List<MonToken>
}

/** Where to find the tokenizer for a language. The app doesn't care how it is stored or reached. */
interface TokenizerProvider {
  /** The tokenizer for [language], or null if there is none at the moment. */
  suspend fun tokenizerFor(language: Language): Tokenizer?

  /**
   * Emits once when collected, for the current state, and again whenever the set of available
   * tokenizers changed, e.g. a pack was installed. The first emission means a collector never
   * misses a change that happened before it started collecting.
   */
  val changes: Flow<Unit>
}

/** A tokenizer can't answer right now. This is not "no tokens"; the text may well be fine. */
class TokenizerUnavailableException(message: String, cause: Throwable? = null) :
  Exception(message, cause)
