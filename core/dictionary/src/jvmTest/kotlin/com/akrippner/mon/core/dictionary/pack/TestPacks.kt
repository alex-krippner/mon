package com.akrippner.mon.core.dictionary.pack

import com.akrippner.mon.core.dictionary.jmdict.JmdictRecord
import com.akrippner.mon.core.dictionary.jmdict.JmdictSchema
import com.akrippner.mon.core.dictionary.jmdict.JmdictWriter
import com.akrippner.mon.core.model.data.Headword
import com.akrippner.mon.core.model.data.Sense
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.security.MessageDigest
import java.util.zip.GZIPOutputStream

internal class TestPack(val info: CatalogPack, val archive: ByteArray)

internal fun sha256(bytes: ByteArray): String =
  MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

internal fun testPack(
  id: String = "jmdict-eng",
  version: String = "1",
  word: String = "開けて",
  format: String = JmdictSchema.FORMAT,
  schema: Int = JmdictSchema.VERSION,
  url: String = "https://example.test/$id-$version.db.gz",
): TestPack {
  val dir = Files.createTempDirectory("pack-build")
  try {
    val db = dir.resolve("dictionary.db")
    JmdictWriter(db.toString()).use { writer ->
      writer.insert(
        JmdictRecord(1, true, listOf(Sense(listOf("gloss"))), listOf(Headword(word, "あけて")))
      )
      writer.finish()
    }
    val raw = Files.readAllBytes(db)
    val archive =
      ByteArrayOutputStream()
        .also { out -> GZIPOutputStream(out).use { it.write(raw) } }
        .toByteArray()
    val info =
      CatalogPack(
        id = id,
        name = "JMdict",
        sourceLanguage = "ja",
        glossLanguage = "en",
        format = format,
        schema = schema,
        version = version,
        url = url,
        sizeBytes = archive.size.toLong(),
        unpackedSizeBytes = raw.size.toLong(),
        sha256 = sha256(archive),
        attribution = "test",
      )
    return TestPack(info, archive)
  } finally {
    dir.toFile().deleteRecursively()
  }
}
