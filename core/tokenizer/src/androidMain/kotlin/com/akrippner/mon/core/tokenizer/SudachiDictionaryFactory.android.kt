package com.akrippner.mon.core.tokenizer

import android.content.Context
import com.worksap.nlp.sudachi.Config
import com.worksap.nlp.sudachi.Dictionary
import com.worksap.nlp.sudachi.DictionaryFactory
import java.io.File
import org.koin.java.KoinJavaComponent.get

fun createSudachiDictionary(): Dictionary {
  val context = get<Context>(Context::class.java)
  val sudachiJson = "sudachi.json"
  val systemDict = "system.dic"

  val config = Config.fromClasspath(sudachiJson)

  val sudachiDir = File(context.cacheDir, "sudachi")
  if (!sudachiDir.exists()) {
    sudachiDir.mkdirs()
  }

  val systemDictFile = File(sudachiDir, systemDict)
  if (!systemDictFile.exists()) {
    context.assets.open(systemDict).use { input ->
      systemDictFile.outputStream().use { output -> input.copyTo(output) }
    }
  }

  config.systemDictionary(systemDictFile.toPath())

  return DictionaryFactory().create(config)
}
