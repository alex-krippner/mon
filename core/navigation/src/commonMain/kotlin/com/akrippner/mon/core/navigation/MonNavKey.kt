package com.akrippner.mon.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable sealed interface MonNavKey : NavKey

@Serializable data object Library : MonNavKey

@Serializable data object AddReading : MonNavKey

@Serializable data object Dictionaries : MonNavKey

/** One dictionary pack: its details, licence and Remove. Part of the Dictionaries tab. */
@Serializable data class DictionaryDetail(val packId: String) : MonNavKey

/** The Vocabulary tab: the saved words of all readings, grouped by reading. */
@Serializable data object Vocabulary : MonNavKey

/** The saved words of one reading. Pushed from the Reader and from a group in [Vocabulary]. */
@Serializable data class ReadingVocabulary(val readingId: String) : MonNavKey

/**
 * One reading. With [focusWordId] (a saved word of the reading) the Reader opens at that word's
 * sentence and outlines the word.
 */
@Serializable data class Reader(val readingId: String, val focusWordId: String? = null) : MonNavKey
