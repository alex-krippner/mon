package com.akrippner.mon.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable sealed interface MonNavKey : NavKey

@Serializable data object Library : MonNavKey

@Serializable data object AddReading : MonNavKey

@Serializable data object Dictionaries : MonNavKey

/** One dictionary pack: its details, licence and Remove. Part of the Dictionaries tab. */
@Serializable data class DictionaryDetail(val packId: String) : MonNavKey

@Serializable data class Reader(val readingId: String) : MonNavKey
