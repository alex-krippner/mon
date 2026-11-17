package com.akrippner.mon.tools

import com.akrippner.mon.core.dictionary.pack.Catalog
import com.akrippner.mon.tools.jmdict.JmdictSource
import com.akrippner.mon.tools.pack.PackSource
import com.akrippner.mon.tools.pack.Release
import com.akrippner.mon.tools.pack.fetchText
import com.akrippner.mon.tools.pack.latestCatalogUrl
import com.akrippner.mon.tools.pack.publish
import com.akrippner.mon.tools.pack.readCatalog
import com.akrippner.mon.tools.pack.requireSeparateIds
import java.io.File
import java.io.IOException
import java.nio.file.Files
import kotlin.system.exitProcess
import kotlinx.serialization.SerializationException

private const val DEFAULT_REPO = "alex-krippner/mon-data"

/** `owner/name`, with the characters GitHub allows in either part. */
private val REPO_PATTERN = Regex("[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+")

private const val EXIT_FAILED = 1
private const val EXIT_USAGE = 2

/** Every source the converter knows. A new source is added here. */
private val SOURCES: List<PackSource> = listOf(JmdictSource)

private val USAGE: String
  get() {
    val inputs =
      SOURCES.joinToString("\n") { source ->
        "    ${source.id}:\n" + source.inputHelp.prependIndent("      ")
      }
    return """
      |Usage: pack-converter <source> [--repo owner/repo] [--allow-drop]
      |                      [--previous-catalog <file> | --no-previous] <outDir> <input>...
      |
      |  <source>      one of: ${SOURCES.joinToString(", ") { it.id }}
      |  <outDir>      receives one <pack id>-<version>.<ext>.gz per pack and one catalog.json
      |  <input>...    the source's input files:
      |$inputs
      |
      |  --repo owner/repo
      |      GitHub repository that will hold the release (default: $DEFAULT_REPO).
      |  --previous-catalog <file>
      |      Take the packs of the other sources from this catalog.json instead of downloading
      |      the one of the repository's latest release.
      |  --no-previous
      |      Start a catalog that lists only this run's packs. Only for the very first release:
      |      it drops every other source's packs from the catalog.
      |  --allow-drop
      |      Publish even if the previous catalog has packs of this source that this run does
      |      not build. Those packs disappear from the app. Without it such a run fails, since
      |      the usual cause is a forgotten input file.
      """
      .trimMargin()
  }

/** Where the packs of the other sources come from. */
private sealed interface Previous {
  data object Latest : Previous

  data class FromFile(val file: File) : Previous

  data object None : Previous
}

private class Arguments(
  val source: PackSource,
  val repo: String,
  val previous: Previous,
  val allowDrop: Boolean,
  val outDir: File,
  val inputs: List<File>,
)

fun main(args: Array<String>) {
  requireSeparateIds(SOURCES)
  val arguments = parse(args.toList())
  arguments.inputs.firstOrNull { !it.isFile }?.let { fail("Not a file: $it", EXIT_USAGE) }

  // Before the conversion, which takes minutes: without the other sources' packs nothing may be
  // published, so there is no point in converting first.
  val previous = previousCatalog(arguments)

  // The uncompressed pack files go here. Deleted in any case, also when a later input fails.
  val workDir = Files.createTempDirectory("pack-converter").toFile()
  val release =
    try {
      val built = arguments.source.build(arguments.inputs, workDir)
      publish(
        arguments.source,
        built,
        arguments.outDir,
        arguments.repo,
        previous,
        arguments.allowDrop,
      )
    } catch (e: IllegalArgumentException) {
      System.err.println(e.message)
      null // exit below: exitProcess here would skip the finally block
    } finally {
      workDir.deleteRecursively()
    } ?: exitProcess(EXIT_FAILED)

  report(release, arguments, previous)
}

/** `<source> [options] <outDir> <input>...`; the options may come in any order. */
private fun parse(args: List<String>): Arguments {
  val source = SOURCES.firstOrNull { it.id == args.firstOrNull() } ?: usageError()
  var repo = DEFAULT_REPO
  var previous: Previous? = null
  var allowDrop = false
  var index = 1
  fun setPrevious(value: Previous) {
    if (previous != null) usageError() // --previous-catalog and --no-previous exclude each other
    previous = value
  }
  /** The value of the option at [index]. Another option in its place means it was forgotten. */
  fun value(): String = args.getOrNull(++index)?.takeUnless { it.startsWith("--") } ?: usageError()
  while (index < args.size && args[index].startsWith("--")) {
    when (args[index]) {
      "--repo" -> repo = value()
      "--previous-catalog" -> setPrevious(Previous.FromFile(File(value())))
      "--no-previous" -> setPrevious(Previous.None)
      "--allow-drop" -> allowDrop = true
      else -> usageError()
    }
    index++
  }
  // The repository goes into URLs and into the printed release command.
  if (!REPO_PATTERN.matches(repo)) {
    fail("Not a GitHub repository (owner/name): '$repo'\n\n$USAGE", EXIT_USAGE)
  }
  val rest = args.drop(index)
  if (rest.size < 2) usageError()
  return Arguments(
    source = source,
    repo = repo,
    previous = previous ?: Previous.Latest,
    allowDrop = allowDrop,
    outDir = File(rest.first()),
    inputs = rest.drop(1).map(::File),
  )
}

/**
 * The catalog whose packs of other sources go into the new one, or null for `--no-previous`. Any
 * failure ends the run: a catalog that is missing the other sources must never be published.
 */
private fun previousCatalog(arguments: Arguments): Catalog? {
  val (text, origin) =
    when (val previous = arguments.previous) {
      Previous.None -> return null
      is Previous.FromFile ->
        try {
          previous.file.readText() to previous.file.path
        } catch (e: IOException) {
          fail("Could not read the previous catalog ${previous.file}: ${e.message}")
        }
      Previous.Latest -> {
        val url = latestCatalogUrl(arguments.repo)
        try {
          fetchText(url) to url
        } catch (e: IOException) {
          fail(
            "Could not download the previous catalog $url: ${e.message}\n" +
              "Nothing was converted. Give it with --previous-catalog <file>, or use " +
              "--no-previous if ${arguments.repo} has no release yet."
          )
        }
      }
    }
  return try {
    readCatalog(text)
  } catch (e: SerializationException) {
    fail(
      "The previous catalog $origin is not one this converter fully understands, so its packs " +
        "can't be carried over safely:\n${e.message}"
    )
  }
}

private fun report(release: Release, arguments: Arguments, previous: Catalog?) {
  val repo = arguments.repo
  for (packResult in release.packs) {
    val pack = packResult.pack
    println(
      "Converted ${packResult.entries} entries (${packResult.skipped} skipped) into ${pack.id}, " +
        "version ${pack.version}"
    )
    println(
      "  ${packResult.archive}  ${pack.sizeBytes / 1024 / 1024} MB, " +
        "unpacked ${pack.unpackedSizeBytes / 1024 / 1024} MB"
    )
  }
  println("  ${release.catalog}")
  if (previous == null) {
    println("  No previous catalog: it lists only the packs above.")
  } else {
    fun List<String>.orNothing() = ifEmpty { listOf("nothing") }.joinToString()
    println("  Kept from the previous catalog: ${release.kept.orNothing()}")
    if (release.dropped.isNotEmpty()) {
      println("  DROPPED from the catalog (--allow-drop): ${release.dropped.joinToString()}")
    }
  }
  println()
  val files = (release.packs.map { it.archive } + release.catalog).joinToString(" ") { "'$it'" }
  println("Publish (with the GitHub CLI), before converting another source:")
  println(
    "  gh release create ${release.tag} $files --repo $repo --title '${release.title}' --latest"
  )
}

private fun fail(message: String, code: Int = EXIT_FAILED): Nothing {
  System.err.println(message)
  exitProcess(code)
}

private fun usageError(): Nothing = fail(USAGE, EXIT_USAGE)
