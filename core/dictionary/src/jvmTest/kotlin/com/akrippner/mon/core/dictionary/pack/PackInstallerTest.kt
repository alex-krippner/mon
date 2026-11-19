package com.akrippner.mon.core.dictionary.pack

import com.akrippner.mon.core.packs.*
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class PackInstallerTest {
  private val dir = Files.createTempDirectory("installer-test").toFile()
  private val downloader = FakePackDownloader()
  private val installer = PackInstaller(downloader, dir)
  private val accept: (File, CatalogPack) -> Boolean = { _, _ -> true }

  @AfterTest
  fun cleanUp() {
    dir.deleteRecursively()
  }

  private fun serve(pack: TestPack) {
    downloader.files[pack.info.url] = pack.archive
  }

  /** Temporary files a failed or finished install must not leave behind. */
  private fun leftovers() = dir.walk().filter { it.isFile && it.name.endsWith(".tmp") }.toList()

  private suspend fun errorOf(block: suspend () -> Unit): PackError =
    assertFailsWith<PackException> { block() }.error

  @Test
  fun downloadsVerifiesAndCommits() = runTest {
    val pack = testPack()
    serve(pack)

    val file = installer.download(pack.info, accept) {}
    assertTrue(file.isFile)
    installer.commit(pack.info, file)

    assertEquals(listOf(pack.info), installer.installedPacks())
    assertTrue(installer.databaseFile(pack.info.id).isFile)
    assertTrue(leftovers().isEmpty())
  }

  @Test
  fun reportsProgress() = runTest {
    val pack = testPack()
    serve(pack)
    val progress = mutableListOf<Float>()

    installer.download(pack.info, accept) { progress += it }

    assertEquals(1f, progress.last())
  }

  @Test
  fun reportsFullProgressBeforeUnpackingEvenIfTheDownloadFellShort() = runTest {
    val pack = testPack()
    serve(pack)
    downloader.finalProgress = 0.97f
    val progress = mutableListOf<Float>()

    installer.download(pack.info, accept) { progress += it }

    assertEquals(listOf(0.97f, 1f), progress)
  }

  @Test
  fun wrongChecksumIsRejectedAndLeavesNothing() = runTest {
    val pack = testPack()
    downloader.files[pack.info.url] = pack.archive.copyOf().also { it[0] = (it[0] + 1).toByte() }

    assertEquals(PackError.CHECKSUM, errorOf { installer.download(pack.info, accept) {} })

    assertTrue(leftovers().isEmpty())
    assertTrue(installer.installedPacks().isEmpty())
  }

  @Test
  fun rejectedByVerifyLeavesNothing() = runTest {
    val pack = testPack()
    serve(pack)

    assertEquals(
      PackError.UNSUPPORTED,
      errorOf { installer.download(pack.info, { _, _ -> false }) {} },
    )

    assertTrue(leftovers().isEmpty())
    assertTrue(installer.installedPacks().isEmpty())
  }

  @Test
  fun nonHttpsUrlIsRejectedBeforeDownloading() = runTest {
    val pack = testPack()

    val error = errorOf {
      installer.download(pack.info.copy(url = "http://example.test/a"), accept) {}
    }

    assertEquals(PackError.UNSUPPORTED, error)
    assertEquals(0, downloader.downloads.get())
  }

  @Test
  fun idThatEscapesTheFolderIsRejected() = runTest {
    val pack = testPack()
    serve(pack)

    assertEquals(
      PackError.UNSUPPORTED,
      errorOf { installer.download(pack.info.copy(id = "../evil"), accept) {} },
    )
    assertEquals(
      PackError.UNSUPPORTED,
      errorOf { installer.download(pack.info.copy(id = "UPPER"), accept) {} },
    )

    assertFalse(File(dir.parentFile, "evil").exists())
    assertEquals(0, downloader.downloads.get())
  }

  @Test
  fun notEnoughSpaceIsReportedBeforeDownloading() = runTest {
    val pack = testPack()
    serve(pack)

    val error = errorOf {
      installer.download(pack.info.copy(unpackedSizeBytes = Long.MAX_VALUE / 2), accept) {}
    }

    assertEquals(PackError.NOT_ENOUGH_SPACE, error)
    assertEquals(0, downloader.downloads.get())
  }

  @Test
  fun commitReplacesAnOlderVersion() = runTest {
    val v1 = testPack(version = "1", word = "開けて")
    val v2 = testPack(version = "2", word = "飲んで")
    serve(v1)
    serve(v2)

    installer.commit(v1.info, installer.download(v1.info, accept) {})
    installer.commit(v2.info, installer.download(v2.info, accept) {})

    assertEquals(listOf(v2.info), installer.installedPacks())
  }

  @Test
  fun failedUpdateKeepsThePreviousVersion() = runTest {
    val v1 = testPack(version = "1")
    val v2 = testPack(version = "2", word = "飲んで")
    serve(v1)
    downloader.files[v2.info.url] = v2.archive.copyOf().also { it[0] = (it[0] + 1).toByte() }
    installer.commit(v1.info, installer.download(v1.info, accept) {})

    assertEquals(PackError.CHECKSUM, errorOf { installer.download(v2.info, accept) {} })

    assertEquals(listOf(v1.info), installer.installedPacks())
    assertTrue(installer.databaseFile(v1.info.id).isFile)
    assertTrue(leftovers().isEmpty())
  }

  @Test
  fun updateThatCannotWriteItsMetadataLeavesTheOldVersionUntouched() = runTest {
    val v1 = testPack(version = "1", word = "開けて")
    val v2 = testPack(version = "2", word = "飲んで")
    serve(v1)
    serve(v2)
    installer.commit(v1.info, installer.download(v1.info, accept) {})
    val oldDatabase = installer.databaseFile(v1.info.id).readBytes()
    val unpacked = installer.download(v2.info, accept) {}
    // A folder where the temporary pack.json goes makes writing it fail, like a full disk.
    File(File(dir, v2.info.id), "pack.json.tmp").mkdirs()

    assertEquals(PackError.STORAGE, errorOf { installer.commit(v2.info, unpacked) })

    assertEquals(listOf(v1.info), installer.installedPacks())
    assertTrue(oldDatabase.contentEquals(installer.databaseFile(v1.info.id).readBytes()))
  }

  @Test
  fun sweepDeletesWhatAKilledInstallLeftBehind() = runTest {
    val pack = testPack()
    serve(pack)
    installer.commit(pack.info, installer.download(pack.info, accept) {})
    val installedDir = File(dir, pack.info.id)
    // A killed update of an installed pack.
    File(installedDir, "dictionary.db.gz.tmp").writeText("x")
    File(installedDir, "dictionary.db.tmp").writeText("x")
    // A killed first download.
    val killed = File(dir, "killed").also { it.mkdirs() }
    File(killed, "dictionary.db.tmp").writeText("x")
    // An update killed between replacing the database and writing pack.json, or a removal that
    // deleted only pack.json.
    val orphan = File(dir, "orphan").also { it.mkdirs() }
    File(orphan, "dictionary.db").writeText("x")
    // A pack.json this version of the app can't read: the data is kept, only the leftovers go.
    val unreadable = File(dir, "unreadable").also { it.mkdirs() }
    File(unreadable, "dictionary.db").writeText("x")
    File(unreadable, "pack.json").writeText("not json")
    File(unreadable, "dictionary.db.tmp").writeText("x")
    // Not a pack folder: not ours to delete.
    val foreign = File(dir, "Not A Pack").also { it.mkdirs() }

    installer.sweep()

    assertTrue(leftovers().isEmpty())
    assertFalse(killed.exists())
    assertFalse(orphan.exists())
    assertTrue(File(unreadable, "dictionary.db").isFile)
    assertTrue(foreign.exists())
    assertEquals(listOf(pack.info), installer.installedPacks())
    assertTrue(installer.databaseFile(pack.info.id).isFile)
  }

  @Test
  fun removeDeletesThePack() = runTest {
    val pack = testPack()
    serve(pack)
    installer.commit(pack.info, installer.download(pack.info, accept) {})

    assertTrue(installer.remove(pack.info.id))

    assertTrue(installer.installedPacks().isEmpty())
    assertFalse(File(dir, pack.info.id).exists())
  }

  @Test
  fun installedPacksIgnoresIncompleteAndMismatchedFolders() = runTest {
    val pack = testPack()
    serve(pack)
    installer.commit(pack.info, installer.download(pack.info, accept) {})

    // No files at all.
    File(dir, "empty").mkdirs()
    // pack.json but no database: not fully installed.
    File(dir, "no-database").also {
      it.mkdirs()
      File(it, "pack.json").writeText(Json.encodeToString(pack.info.copy(id = "no-database")))
    }
    // The pack.json names a different id than the folder.
    File(dir, "other").also {
      it.mkdirs()
      File(it, "dictionary.db").writeText("x")
      File(it, "pack.json").writeText(Json.encodeToString(pack.info))
    }
    // Not valid JSON.
    File(dir, "broken").also {
      it.mkdirs()
      File(it, "dictionary.db").writeText("x")
      File(it, "pack.json").writeText("not json")
    }

    assertEquals(listOf(pack.info), installer.installedPacks())
  }
}
