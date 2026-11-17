package com.akrippner.mon.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.akrippner.mon.core.navigation.AppNavigator
import com.akrippner.mon.core.navigation.Library
import com.akrippner.mon.core.navigation.MonNavKey

internal class MonAppNavigator(private val backStack: NavBackStack<NavKey>) : AppNavigator {
  override fun navigateTo(key: MonNavKey) {
    if (backStack.lastOrNull() != key) {
      backStack.add(key)
    }
  }

  /**
   * Shows a bottom-bar tab. Library, the start destination, stays at the bottom of the stack, so
   * back from another tab returns to it and back from Library leaves the app.
   */
  fun switchTab(tab: MonNavKey) {
    if (backStack.lastOrNull() == tab) return
    // Add before removing: the back stack must never be empty.
    if (tab != Library) backStack.add(tab)
    val keep = if (tab == Library) 1 else 2
    while (backStack.size > keep) backStack.removeAt(1)
  }

  override fun back() {
    backStack.removeLastOrNull()
  }

  /**
   * Pops [key] only if it is on top, so a late or repeated close cannot pop the screen below it.
   */
  fun close(key: NavKey) {
    if (backStack.lastOrNull() == key) backStack.removeLastOrNull()
  }
}
