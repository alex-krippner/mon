package com.akrippner.mon.core.navigation

interface AppNavigator {
  fun navigateTo(key: MonNavKey)

  fun back()
}
