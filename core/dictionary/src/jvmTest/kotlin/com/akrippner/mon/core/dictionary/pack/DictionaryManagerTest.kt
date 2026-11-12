package com.akrippner.mon.core.dictionary.pack

import com.akrippner.mon.core.dictionary.DictionaryClosedException
import com.akrippner.mon.core.dictionary.jmdict.JmdictFormat
import com.akrippner.mon.core.model.data.LookupQuery
import java.io.File
import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json

class DictionaryManagerTest {
  private val dir = Files.createTempDirectory("manager-test").toFile()
  private val downloader = FakePackDownloader()
  private val catalogUrl = "https://example.test/catalog.json"
  // Installs run in the manager's own scope, as in the app; tests cancel it to simulate shutdown.
  private val scope = CoroutineScope(SupervisorJob())
  private val manager = newManager()

  private fun newManager() =
    FileDictionaryManager(
      downloader,
      PackInstaller(downloader, dir),
      DictionaryFactory(listOf(JmdictFormat)),
      catalogUrl,
      scope,
    )

  @AfterTest
  fun cleanUp() {
    scope.cancel()
    dir.deleteRecursively()
  }

  /** Makes [packs] the whole catalog and serves their files. */
  private fun publish(vararg packs: TestPack) {
    downloader.texts[catalogUrl] = Json.encodeToString(Catalog(packs.map { it.info }))
    packs.forEach { downloader.files[it.info.url] = it.archive }
  }

  private fun leftovers() = dir.walk().filter { it.isFile && it.name.endsWith(".tmp") }.toList()

  private suspend fun awaitState(test: (PackState) -> Boolean) =
    manager.packs.first { packs -> packs.any { test(it.state) } }

  private fun stateOf(manager: DictionaryManager = this.manager) =
    manager.packs.value.single().state

  @Test
  fun installsAndServesLookups() = runTest {
    val pack = testPack()
    publish(pack)

    manager.refreshCatalog()
    assertEquals(PackState.NotInstalled, stateOf())

    manager.install(pack.info.id)
    assertEquals(PackState.Installed, stateOf())

    val results = manager.installed().single().lookup(LookupQuery("開けて"))
    assertEquals(1, results.size)
    assertTrue(leftovers().isEmpty())
  }

  @Test
  fun unsupportedPacksAreNotOffered() = runTest {
    publish(testPack(schema = 99), testPack(id = "other-format", format = "something-else"))

    manager.refreshCatalog()

    assertTrue(manager.packs.value.isEmpty())
  }

  @Test
  fun failedRefreshKeepsTheOldListAndReportsWhy() = runTest {
    publish(testPack())
    assertNull(manager.refreshCatalog())
    val before = manager.packs.value

    downloader.failWith = PackError.NETWORK
    assertEquals(CatalogError.NETWORK, manager.refreshCatalog())
    assertEquals(before, manager.packs.value)

    downloader.failWith = null
    downloader.texts[catalogUrl] = "not json"
    assertEquals(CatalogError.INVALID, manager.refreshCatalog())
    assertEquals(before, manager.packs.value)

    // One pack with a missing field makes the whole catalog unreadable.
    downloader.texts[catalogUrl] = """{"packs":[{"id":"jmdict-eng"}]}"""
    assertEquals(CatalogError.INVALID, manager.refreshCatalog())
    assertEquals(before, manager.packs.value)
  }

  @Test
  fun aPackIdListedTwiceIsOfferedOnce() = runTest {
    val first = testPack(version = "1")
    publish(first, testPack(version = "2"))

    assertNull(manager.refreshCatalog())

    assertEquals(listOf(first.info), manager.packs.value.map { it.info })
  }

  @Test
  fun firstScanDeletesTheLeftoversOfAKilledInstall() = runTest {
    val pack = testPack()
    publish(pack)
    val packDir = File(dir, pack.info.id).also { it.mkdirs() }
    File(packDir, "dictionary.db.gz.tmp").writeText("x")
    File(packDir, "dictionary.db.tmp").writeText("x")

    manager.refreshCatalog()

    assertEquals(PackState.NotInstalled, stateOf())
    assertFalse(packDir.exists())
  }

  @Test
  fun failedRemovalIsReportedAndKeepsThePackUsable() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    manager.install(pack.info.id)
    val packDir = File(dir, pack.info.id)
    // Nothing can be deleted from a read-only folder.
    packDir.setWritable(false)
    try {
      // Unless the tests run as root, who may write anywhere: then there is nothing to test.
      if (runCatching { File(packDir, "probe").createNewFile() }.getOrDefault(false)) return@runTest

      manager.remove(pack.info.id)

      val state = stateOf()
      assertIs<PackState.Failed>(state)
      assertEquals(PackError.STORAGE, state.error)
      assertEquals(pack.info, state.installed)
      assertEquals(1, manager.installed().single().lookup(LookupQuery("開けて")).size)
    } finally {
      packDir.setWritable(true)
    }
  }

  @Test
  fun unknownPackIdChangesNothing() = runTest {
    publish(testPack())
    manager.refreshCatalog()
    val before = manager.packs.value

    manager.install("does-not-exist")

    assertEquals(before, manager.packs.value)
    assertEquals(0, downloader.downloads.get())
  }

  @Test
  fun networkFailureDuringInstallShowsAsFailed() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()

    downloader.failWith = PackError.NETWORK
    manager.install(pack.info.id)

    assertEquals(PackState.Failed(PackError.NETWORK), stateOf())
    assertTrue(manager.installed().isEmpty())
  }

  @Test
  fun checksumMismatchInstallsNothingAndCanBeRetried() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    downloader.files[pack.info.url] = pack.archive.copyOf().also { it[0] = (it[0] + 1).toByte() }

    manager.install(pack.info.id)

    assertEquals(PackState.Failed(PackError.CHECKSUM), stateOf())
    assertTrue(manager.installed().isEmpty())
    assertTrue(leftovers().isEmpty())

    downloader.files[pack.info.url] = pack.archive
    manager.install(pack.info.id)

    assertEquals(PackState.Installed, stateOf())
    assertEquals(1, manager.installed().size)
  }

  @Test
  fun secondInstallWhileDownloadingIsIgnored() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    val gate = CompletableDeferred<Unit>()
    downloader.gate = gate

    val first = launch { manager.install(pack.info.id) }
    awaitState { it is PackState.Downloading }
    manager.install(pack.info.id) // must return at once, not start a second download
    gate.complete(Unit)
    first.join()

    assertEquals(1, downloader.downloads.get())
    assertEquals(PackState.Installed, stateOf())
  }

  @Test
  fun removeIsRefusedWhileDownloading() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    val gate = CompletableDeferred<Unit>()
    downloader.gate = gate

    val install = launch { manager.install(pack.info.id) }
    awaitState { it is PackState.Downloading }
    manager.remove(pack.info.id)
    assertIs<PackState.Downloading>(stateOf())

    gate.complete(Unit)
    install.join()
    assertEquals(PackState.Installed, stateOf())
  }

  @Test
  fun cancellingTheCallerDoesNotCancelTheDownload() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    val gate = CompletableDeferred<Unit>()
    downloader.gate = gate

    val caller = launch { manager.install(pack.info.id) }
    awaitState { it is PackState.Downloading }
    caller.cancel() // e.g. the user leaves the screen
    caller.join()
    assertIs<PackState.Downloading>(stateOf())

    gate.complete(Unit)
    awaitState { it == PackState.Installed }
    assertEquals(1, manager.installed().size)
  }

  @Test
  fun shuttingDownTheManagerClearsTheDownloadingState() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    downloader.gate = CompletableDeferred() // never completed

    val caller = launch { manager.install(pack.info.id) }
    awaitState { it is PackState.Downloading }
    scope.cancel()
    caller.join()

    assertEquals(PackState.NotInstalled, stateOf())
    assertTrue(leftovers().isEmpty())
    assertTrue(manager.installed().isEmpty())
  }

  @Test
  fun installAfterShutdownDoesNotLeaveTheDownloadingState() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    scope.cancel()

    manager.install(pack.info.id)

    assertEquals(PackState.NotInstalled, stateOf())
    assertEquals(0, downloader.downloads.get())
  }

  @Test
  fun failedUpdateKeepsTheOldVersionInstalledAndUsable() = runTest {
    val v1 = testPack(version = "1", word = "開けて")
    val v2 = testPack(version = "2", word = "飲んで")
    publish(v1)
    manager.refreshCatalog()
    manager.install(v1.info.id)
    publish(v2)
    downloader.files[v2.info.url] = v2.archive.copyOf().also { it[0] = (it[0] + 1).toByte() }
    manager.refreshCatalog()

    manager.install(v2.info.id)

    assertEquals(PackState.Failed(PackError.CHECKSUM, installed = v1.info), stateOf())
    assertEquals(1, manager.installed().single().lookup(LookupQuery("開けて")).size)
  }

  @Test
  fun removeCalledTwiceKeepsThePackMarkedAsBeingRemovedUntilTheFirstEnds() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    manager.install(pack.info.id)

    manager.remove(pack.info.id)
    manager.remove(pack.info.id) // already removed: harmless

    assertEquals(PackState.NotInstalled, stateOf())
    // And the pack can be installed again afterwards.
    manager.install(pack.info.id)
    assertEquals(PackState.Installed, stateOf())
  }

  @Test
  fun updateReplacesTheFileAndClosesTheOldReader() = runTest {
    val v1 = testPack(version = "1", word = "開けて")
    val v2 = testPack(version = "2", word = "飲んで")
    publish(v1)
    manager.refreshCatalog()
    manager.install(v1.info.id)
    val old = manager.installed().single()
    assertEquals(1, old.lookup(LookupQuery("開けて")).size) // opens the file

    publish(v2)
    manager.refreshCatalog()
    val state = stateOf()
    assertIs<PackState.UpdateAvailable>(state)
    assertEquals("1", state.installed.version)

    manager.install(v2.info.id)

    assertEquals(PackState.Installed, stateOf())
    assertEquals("2", manager.packs.value.single().info.version)
    // The old reader must stay closed, and say so instead of "not found".
    assertFailsWith<DictionaryClosedException> { old.lookup(LookupQuery("開けて")) }
    assertEquals(1, manager.installed().single().lookup(LookupQuery("飲んで")).size)
  }

  @Test
  fun installedPacksSurviveARestartWhileOffline() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    manager.install(pack.info.id)

    val restarted = newManager()
    downloader.failWith = PackError.NETWORK
    restarted.refreshCatalog()

    assertEquals(PackState.Installed, stateOf(restarted))
    assertEquals(pack.info, restarted.packs.value.single().info)
    assertEquals(1, restarted.installed().single().lookup(LookupQuery("開けて")).size)
  }

  @Test
  fun removeUninstallsThePack() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    manager.install(pack.info.id)
    manager.installed().single().lookup(LookupQuery("開けて")) // opens the reader

    manager.remove(pack.info.id)

    assertEquals(PackState.NotInstalled, stateOf())
    assertTrue(manager.installed().isEmpty())
    assertFalse(File(dir, pack.info.id).exists())
  }

  @Test
  fun removedPackCanBeInstalledAgain() = runTest {
    val pack = testPack()
    publish(pack)
    manager.refreshCatalog()
    manager.install(pack.info.id)
    manager.remove(pack.info.id)

    manager.install(pack.info.id)

    assertEquals(PackState.Installed, stateOf())
    assertEquals(1, manager.installed().single().lookup(LookupQuery("開けて")).size)
  }
}
