package com.akrippner.mon.tools.demoseed

import com.akrippner.mon.core.demo.DemoDictionaryInfo
import com.akrippner.mon.core.demo.DemoLookup
import com.akrippner.mon.core.demo.DemoReading
import com.akrippner.mon.core.demo.DemoSeed
import com.akrippner.mon.core.demo.DemoSeedJson
import com.akrippner.mon.core.demo.DemoToken
import com.akrippner.mon.core.dictionary.DictionaryProvider
import com.akrippner.mon.core.dictionary.di.dictionaryModule
import com.akrippner.mon.core.model.data.Language
import com.akrippner.mon.core.model.data.LookupQuery
import com.akrippner.mon.core.model.data.MonToken
import com.akrippner.mon.core.packs.di.packsModule
import com.akrippner.mon.core.tokenizer.TokenizerProvider
import com.akrippner.mon.core.tokenizer.di.tokenizerModule
import java.io.File
import kotlin.system.exitProcess
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import org.koin.core.context.startKoin

/**
 * One sample text, as written in `readings.json`. They are Japanese with an English translation.
 */
@Serializable
private class SampleReading(val title: String, val text: String, val translation: String)

// A word can have many entries (する has dozens); the demo shows the first few.
private const val MAX_ENTRIES_PER_LOOKUP = 8

private const val ONE_DAY_MILLIS = 24L * 60 * 60 * 1000

fun main(args: Array<String>) {
  if (args.size != 2) {
    System.err.println("Usage: demo-seed <readings.json> <demo-seed.json>")
    exitProcess(2)
  }
  val input = File(args[0])
  val output = File(args[1])
  runBlocking { createSeed(input, output) }
}

private suspend fun createSeed(input: File, output: File) {
  val samples =
    DemoSeedJson.decodeFromString(ListSerializer(SampleReading.serializer()), input.readText())

  val koin = startKoin { modules(tokenizerModule, dictionaryModule, packsModule) }.koin
  val tokenizer =
    koin.get<TokenizerProvider>().tokenizerFor(Language.JAPANESE)
      ?: error("No Japanese tokenizer: install the 'sudachi-core' pack in the desktop app first.")
  val dictionary =
    koin
      .get<DictionaryProvider>()
      .dictionariesFor(Language.JAPANESE, Language.ENGLISH)
      .firstOrNull()
      ?: error(
        "No Japanese-English dictionary: install the 'jmdict-eng' pack in the desktop app first."
      )

  val now = System.currentTimeMillis()
  val lookups = LinkedHashMap<LookupQuery, DemoLookup>()
  val readings =
    samples.mapIndexed { index, sample ->
      val tokens = tokenizer.tokenize(sample.text)
      for (token in tokens.filter { it.isWord() }) {
        val query = token.toQuery()
        if (query in lookups) continue
        val entries = dictionary.lookup(query).take(MAX_ENTRIES_PER_LOOKUP)
        if (entries.isNotEmpty()) lookups[query] = DemoLookup.from(query, entries)
      }
      DemoReading(
        id = "demo-${index + 1}",
        title = sample.title,
        text = sample.text,
        translation = sample.translation,
        sourceLanguage = Language.JAPANESE.tag,
        translationLanguage = Language.ENGLISH.tag,
        tokens = tokens.map { DemoToken.from(it) },
        // The first sample is the newest, so the library lists them in the order of the file.
        createdAt = now - index * ONE_DAY_MILLIS,
      )
    }

  val seed =
    DemoSeed(
      dictionary =
        DemoDictionaryInfo(
          id = dictionary.source.id,
          name = dictionary.source.name,
          attribution = dictionary.source.attribution,
          version = dictionary.source.version,
          sourceLanguage = dictionary.sourceLanguage.tag,
          glossLanguage = dictionary.glossLanguage.tag,
        ),
      readings = readings,
      lookups = lookups.values.toList(),
    )
  output.parentFile?.mkdirs()
  output.writeText(DemoSeedJson.encodeToString(DemoSeed.serializer(), seed))
  println(
    "Wrote ${output.path}: ${readings.size} readings, ${lookups.size} look-ups " +
      "(${tokenizer.id}, ${dictionary.source.id} ${dictionary.source.version})"
  )
}

/** Punctuation and spaces are not looked up. Sudachi names their part of speech 補助記号 / 空白. */
private fun MonToken.isWord(): Boolean =
  surface.isNotBlank() && partOfSpeech.firstOrNull() !in setOf("補助記号", "空白")

/**
 * The query the reader makes for a tapped word, see `LookupWordUseCase`. The demo dictionary falls
 * back to the lemma if this drifts from it.
 */
private fun MonToken.toQuery() =
  LookupQuery(
    lemma = dictionaryForm,
    surface = surface,
    reading = pronunciation.takeIf { surface == dictionaryForm },
  )
