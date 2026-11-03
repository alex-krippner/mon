package com.akrippner.mon.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable sealed interface MonNavKey : NavKey

@Serializable data object Library : MonNavKey

@Serializable data object AddReading : MonNavKey

@Serializable data class Reader(val readingId: String) : MonNavKey
