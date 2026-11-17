package com.akrippner.mon.tools.pack

import java.io.File

/**
 * One data source the converter publishes, e.g. JMdict. A source only knows how to turn its input
 * files into pack files; compressing them, describing them in the catalog and naming the release is
 * the same for every source and done by [publish].
 */
internal interface PackSource {
  /**
   * The subcommand, the release-tag prefix and the prefix of every pack id, e.g. `jmdict`. A source
   * owns all pack ids that start with `<id>-`: each run replaces them in the catalog.
   */
  val id: String

  /** The name in the release title, e.g. `JMdict`. */
  val title: String

  /** Describes the input files for the usage text. */
  val inputHelp: String

  /**
   * Builds the uncompressed pack files into [workDir], one input at a time so only one parsed input
   * is in memory. The caller owns [workDir] and deletes it, also when this fails half-way.
   *
   * @throws IllegalArgumentException if an input can't be turned into a pack.
   */
  fun build(inputs: List<File>, workDir: File): List<BuiltPack>
}

/**
 * Checks that no source could take another source's packs for its own. A source owns the pack ids
 * that start with `<id>-`, so no id may be equal to another one or start with `<another id>-`:
 * otherwise a run of the shorter one would drop the longer one's packs from the catalog.
 *
 * @throws IllegalArgumentException if two ids overlap, or an id is not made of a-z, 0-9 and '-'.
 */
internal fun requireSeparateIds(sources: List<PackSource>) {
  val pattern = Regex("[a-z0-9]+(-[a-z0-9]+)*")
  for (source in sources) {
    require(pattern.matches(source.id)) { "Source id '${source.id}' must be like 'jmdict'" }
    for (other in sources) {
      require(other === source || !"${other.id}-".startsWith("${source.id}-")) {
        "Source '${source.id}' would own the packs of source '${other.id}'"
      }
    }
  }
}

/** A pack before packaging: the catalog fields its source knows, plus the uncompressed [file]. */
internal class BuiltPack(
  val id: String,
  val name: String,
  val sourceLanguage: String,
  val glossLanguage: String,
  val format: String,
  val schema: Int,
  val version: String,
  val attribution: String,
  val file: File,
  val entries: Int,
  val skipped: Int,
)
