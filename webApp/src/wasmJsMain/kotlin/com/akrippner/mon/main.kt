package com.akrippner.mon

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.window.ComposeViewport
import com.akrippner.mon.core.demo.parseDemoSeed
import com.akrippner.mon.webapp.generated.resources.NotoSansJP_Regular
import com.akrippner.mon.webapp.generated.resources.Res
import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
  MainScope().launch {
    // The readings and the dictionary entries of the demo; see :tools:demo-seed.
    val seed = parseDemoSeed(Res.readBytes("files/demo-seed.json").decodeToString())
    initKoin { modules(webModule(seed)) }

    ComposeViewport(document.body!!) { JapaneseFont { App() } }
  }
}

/**
 * Compose in the browser draws only with fonts it has loaded; it can't use the system's. Without a
 * Japanese font every kana and kanji would show as an empty box, so [content] waits for one.
 */
@OptIn(ExperimentalResourceApi::class)
@Composable
private fun JapaneseFont(content: @Composable () -> Unit) {
  val resolver = LocalFontFamilyResolver.current
  val font = preloadFont(Res.font.NotoSansJP_Regular).value
  var ready by remember { mutableStateOf(false) }
  LaunchedEffect(font) {
    if (font != null) {
      // Registers it as a fallback for the glyphs the app's own font does not have.
      resolver.preload(FontFamily(font))
      ready = true
    }
  }
  if (ready) content()
}
