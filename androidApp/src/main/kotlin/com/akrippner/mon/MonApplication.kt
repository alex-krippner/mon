package com.akrippner.mon

import android.app.Application
import java.io.File
import kotlin.concurrent.thread
import org.koin.android.ext.koin.androidContext

class MonApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    initKoin { androidContext(this@MonApplication) }
    deleteLegacySudachiCopy()
  }

  /**
   * Older versions copied the bundled Sudachi dictionary (about 200 MB) into the cache. The
   * dictionary is a downloadable pack now, so that copy is dead weight. Delete this once no one is
   * likely to update from a version that has it.
   */
  private fun deleteLegacySudachiCopy() {
    val legacy = File(cacheDir, "sudachi")
    if (legacy.exists()) thread { legacy.deleteRecursively() }
  }
}
