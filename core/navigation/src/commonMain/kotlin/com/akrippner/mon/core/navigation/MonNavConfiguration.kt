package com.akrippner.mon.core.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Registers every [MonNavKey] so the back stack can be saved and restored. A new key is added here
 * and nowhere else.
 */
val MonNavConfiguration = SavedStateConfiguration {
  serializersModule = SerializersModule {
    polymorphic(NavKey::class) {
      subclass(Library::class)
      subclass(AddReading::class)
      subclass(Dictionaries::class)
      subclass(DictionaryDetail::class)
      subclass(Reader::class)
    }
  }
}
