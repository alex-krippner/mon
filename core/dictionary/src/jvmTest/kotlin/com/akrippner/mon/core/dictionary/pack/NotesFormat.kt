package com.akrippner.mon.core.dictionary.pack

import com.akrippner.mon.core.dictionary.CloseableReader
import com.akrippner.mon.core.model.data.DictionarySource
import com.akrippner.mon.core.model.data.Language
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.GZIPOutputStream

private const val NOTES_FORMAT = "notes-text"
private const val NOTES_MAGIC = "mon-notes 1"

/**
 * A test-only pack kind that is neither JMdict nor SQLite nor a word dictionary: a text file whose
 * first line is a magic string. It shows that the manager installs, hands out and closes readers of
 * any kind.
 */
internal class NotesFormat : DictionaryFormat {
  override val format = NOTES_FORMAT
  override val schema = 1

  /** Every reader this format has opened, oldest first. */
  val opened = mutableListOf<NotesReader>()

  override fun verify(file: File, pack: CatalogPack): Boolean =
    file.useLines { it.firstOrNull() } == NOTES_MAGIC

  override fun open(file: File, pack: CatalogPack): CloseableReader =
    NotesReader(
        pack.toSource(),
        Language.fromTag(pack.sourceLanguage),
        Language.fromTag(pack.glossLanguage),
      )
      .also { opened += it }
}

/** Not a `MonDictionary`: it has no lookup, only what every reader has. */
internal class NotesReader(
  override val source: DictionarySource,
  override val sourceLanguage: Language,
  override val glossLanguage: Language,
) : CloseableReader {
  var closed = false
    private set

  override suspend fun close() {
    closed = true
  }
}

internal fun notesPack(id: String = "notes-test", version: String = "1"): TestPack {
  val raw = "$NOTES_MAGIC\nfirst note\n".toByteArray()
  val archive =
    ByteArrayOutputStream()
      .also { out -> GZIPOutputStream(out).use { it.write(raw) } }
      .toByteArray()
  val info =
    CatalogPack(
      id = id,
      name = "Notes",
      sourceLanguage = "ja",
      glossLanguage = "en",
      format = NOTES_FORMAT,
      schema = 1,
      version = version,
      url = "https://example.test/$id-$version.txt.gz",
      sizeBytes = archive.size.toLong(),
      unpackedSizeBytes = raw.size.toLong(),
      sha256 = sha256(archive),
      attribution = "test",
    )
  return TestPack(info, archive)
}
