package com.akrippner.mon.navigation

import androidx.compose.runtime.MutableState
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.akrippner.mon.core.navigation.AppNavigator
import com.akrippner.mon.core.navigation.Library
import com.akrippner.mon.core.navigation.MonNavKey

/**
 * Navigation with one back stack per bottom-bar tab. A tab keeps its stack while another tab is
 * shown, so switching back finds the screens as they were.
 *
 * [stacks] has one stack for each tab, keyed by the tab's root screen. Library, the start tab, is
 * the tab that back from the root of another tab goes to; back from the root of Library leaves the
 * app.
 */
internal class MonAppNavigator(
  private val stacks: Map<MonNavKey, NavBackStack<NavKey>>,
  private val selectedTab: MutableState<MonNavKey>,
) : AppNavigator {

  private val current: NavBackStack<NavKey>
    get() = stacks.getValue(selectedTab.value)

  /**
   * Pushes [key] on the current tab's stack. If the stack already holds it (for example the list of
   * a reading, reached again from the Reader that was opened from it), goes back to it instead, so
   * the stack does not grow in a loop.
   */
  override fun navigateTo(key: MonNavKey) {
    val stack = current
    val existing = stack.lastIndexOf(key)
    if (existing < 0) {
      stack.add(key)
    } else {
      while (stack.lastIndex > existing) stack.removeAt(stack.lastIndex)
    }
  }

  /** Shows a tab. Tapping the tab that is already shown pops it back to its root screen. */
  fun switchTab(tab: MonNavKey) {
    if (selectedTab.value == tab) {
      val stack = current
      while (stack.size > 1) stack.removeAt(stack.lastIndex)
    } else {
      selectedTab.value = tab
    }
  }

  override fun back() {
    val stack = current
    when {
      stack.size > 1 -> stack.removeAt(stack.lastIndex)
      selectedTab.value != Library -> selectedTab.value = Library
    }
  }

  /**
   * Pops [key] only if it is on top of a stack, so a late or repeated close cannot pop the screen
   * below it.
   */
  fun close(key: NavKey) {
    stacks.values
      .firstOrNull { it.lastOrNull() == key && it.size > 1 }
      ?.let { it.removeAt(it.lastIndex) }
  }
}
