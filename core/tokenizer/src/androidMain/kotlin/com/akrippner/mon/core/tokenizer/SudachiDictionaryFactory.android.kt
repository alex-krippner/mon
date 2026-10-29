package com.akrippner.mon.core.tokenizer

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.DictionaryFactory
import java.io.File
import java.io.IOException

private const val SUDACHI_CONFIG = "sudachi.json"
private const val SYSTEM_DICTIONARY = "system_core.dic"

/** Loads the Sudachi dictionary. This reads a large file, so do not call it on the main thread. */
fun createSudachiDictionary(context: Context): Dictionary {
  val config = Config.fromClasspath(SUDACHI_CONFIG)
  config.systemDictionary(installSystemDictionary(context).toPath())
  return DictionaryFactory().create(config)
}

/**
 * Copies the system dictionary from the assets to the cache dir, where Sudachi can read it as a
 * file. The copy is written to a temporary file and renamed, so an interrupted copy never leaves a
 * truncated dictionary behind. A version marker written after the rename makes every app install or
 * update copy the dictionary again, in case the asset changed.
 */
private fun installSystemDictionary(context: Context): File {
  val dir = File(context.cacheDir, "sudachi").apply { mkdirs() }
  val dictionary = File(dir, SYSTEM_DICTIONARY)
  val marker = File(dir, "$SYSTEM_DICTIONARY.version")
  val version = installVersion(context)

  if (dictionary.isFile && marker.isFile && marker.readText() == version) {
    return dictionary
  }

  marker.delete()
  val temp = File(dir, "$SYSTEM_DICTIONARY.tmp")
  context.assets.open(SYSTEM_DICTIONARY).use { input ->
    temp.outputStream().use { output -> input.copyTo(output) }
  }
  if (!temp.renameTo(dictionary)) {
    temp.delete()
    throw IOException("Could not move the Sudachi dictionary to $dictionary")
  }
  marker.writeText(version)
  return dictionary
}

/** Changes whenever the app is installed or updated, i.e. whenever the assets may have changed. */
private fun installVersion(context: Context): String {
  val packageManager = context.packageManager
  val info =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      packageManager.getPackageInfo(context.packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
      @Suppress("DEPRECATION") packageManager.getPackageInfo(context.packageName, 0)
    }
  return info.lastUpdateTime.toString()
}
