package com.akrippner.mon.tools.jmdict

import java.io.File
import kotlin.system.exitProcess

private const val DEFAULT_REPO = "alex-krippner/mon-data"

private val SUPPORTED_FILES =
  GlossLanguage.entries.joinToString(", ") { "jmdict-${it.code}-X.json" }

private const val USAGE_HEAD =
  "Usage: jmdict-converter [--repo owner/repo] <outDir> <jmdict-X.json>..."

private val USAGE =
  """$USAGE_HEAD

  <outDir>             receives one jmdict-<lang>-<dictDate>.db.gz per input and one catalog.json
  <jmdict-X.json>...   full single-language files from
                       https://github.com/scriptin/jmdict-simplified/releases
                       (unpack the .json.tgz or .json.zip first; not the 'common' or 'all'
                       variants). Supported: $SUPPORTED_FILES.
                       All files must come from the same release.
  --repo owner/repo    GitHub repository that will hold the release (default: $DEFAULT_REPO)"""

fun main(args: Array<String>) {
  val repo: String
  val rest: List<String>
  if (args.firstOrNull() == "--repo") {
    if (args.size < 2) usageError()
    repo = args[1]
    rest = args.drop(2)
  } else {
    repo = DEFAULT_REPO
    rest = args.toList()
  }
  if (rest.size < 2) usageError()

  val outDir = File(rest.first())
  val inputs = rest.drop(1).map(::File)
  inputs
    .firstOrNull { !it.isFile }
    ?.let {
      System.err.println("Not a file: $it")
      exitProcess(2)
    }

  val result =
    try {
      convert(inputs, outDir, repo)
    } catch (e: IllegalArgumentException) {
      System.err.println(e.message)
      exitProcess(1)
    }

  for (packResult in result.packs) {
    val pack = packResult.pack
    println(
      "Converted ${packResult.entries} entries (${packResult.skipped} skipped) into ${pack.id} " +
        "from JMdict ${pack.version}"
    )
    println(
      "  ${packResult.archive}  ${pack.sizeBytes / 1024 / 1024} MB, " +
        "unpacked ${pack.unpackedSizeBytes / 1024 / 1024} MB"
    )
  }
  println("  ${result.catalog}")
  println()
  val files = (result.packs.map { it.archive } + result.catalog).joinToString(" ") { "'$it'" }
  println("Publish (from the mon-data repository, with the GitHub CLI):")
  println(
    "  gh release create ${result.releaseTag} $files " +
      "--repo $repo --title 'JMdict ${result.packs.first().pack.version}'"
  )
}

private fun usageError(): Nothing {
  System.err.println(USAGE)
  exitProcess(2)
}
