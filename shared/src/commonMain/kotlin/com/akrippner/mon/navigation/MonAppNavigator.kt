package com.akrippner.mon.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.akrippner.mon.core.navigation.AppNavigator
import com.akrippner.mon.core.navigation.MonNavKey

internal class MonAppNavigator(private val backStack: NavBackStack<NavKey>) : AppNavigator {
  override fun navigateTo(key: MonNavKey) {
    if (backStack.lastOrNull() != key) {
      backStack.add(key)
    }
  }

  override fun back() {
    backStack.removeLastOrNull()
  }
}
