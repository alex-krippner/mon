package com.akrippner.mon.tools.jmdict

import java.io.File
import kotlin.system.exitProcess

private const val DEFAULT_REPO = "alex-krippner/mon-data"

private const val USAGE =
  """Usage: jmdict-converter <jmdict-eng-X.json> <outDir> [owner/repo]

  <jmdict-eng-X.json>  the full English file from https://github.com/scriptin/jmdict-simplified/releases
                       (unpack the .json.tgz or .json.zip first; not the 'common' variant)
  <outDir>             receives jmdict-eng-<dictDate>.db.gz and catalog.json
  [owner/repo]         GitHub repository that will hold the release (default: $DEFAULT_REPO)"""

fun main(args: Array<String>) {
  if (args.size !in 2..3) {
    System.err.println(USAGE)
    exitProcess(2)
  }
  val input = File(args[0])
  if (!input.isFile) {
    System.err.println("Not a file: $input")
    exitProcess(2)
  }
  val repo = args.getOrElse(2) { DEFAULT_REPO }

  val result =
    try {
      convert(input, File(args[1]), repo)
    } catch (e: IllegalArgumentException) {
      System.err.println(e.message)
      exitProcess(1)
    }

  val pack = result.pack
  println(
    "Converted ${result.entries} entries (${result.skipped} skipped) from JMdict ${pack.version}"
  )
  println(
    "  ${result.archive}  ${pack.sizeBytes / 1024 / 1024} MB, unpacked ${pack.unpackedSizeBytes / 1024 / 1024} MB"
  )
  println("  ${result.catalog}")
  println()
  println("Publish (from the mon-data repository, with the GitHub CLI):")
  println(
    "  gh release create ${result.releaseTag} '${result.archive}' '${result.catalog}' " +
      "--repo $repo --title 'JMdict ${pack.version}'"
  )
}
