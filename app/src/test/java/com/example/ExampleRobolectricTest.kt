package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.fs.FileManager
import com.example.data.local.CodeVaultDatabase
import com.example.data.model.*
import com.example.data.repository.ProjectRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.editor.SyntaxHighlighter
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var database: CodeVaultDatabase
  private lateinit var fileManager: FileManager
  private lateinit var projectRepository: ProjectRepository
  private lateinit var settingsRepository: SettingsRepository

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    database = CodeVaultDatabase.getDatabase(context)
    fileManager = FileManager(context)
    projectRepository = ProjectRepository(database, fileManager)
    settingsRepository = SettingsRepository(context)
  }

  @Test
  fun test01_appNameResource() {
    val appName = context.getString(R.string.app_name)
    assertEquals("CodeVault", appName)
  }

  @Test
  fun test02_projectDatabasePersistence() = runBlocking {
    val project = projectRepository.createProject("VaultCore", "Security Engine", "Kotlin")
    assertNotNull(project.id)
    assertEquals("VaultCore", project.name)
    assertEquals("Kotlin", project.primaryLanguage)

    // Verify persisted in Room
    val fetched = projectRepository.getProject(project.id)
    assertNotNull(fetched)
    assertEquals("VaultCore", fetched?.name)

    // Star / Unstar
    projectRepository.toggleStarProject(project.id)
    val starred = projectRepository.getProject(project.id)
    assertTrue(starred?.isStarred == true)

    val starredList = projectRepository.getStarredProjects().first()
    assertTrue(starredList.any { it.id == project.id })
  }

  @Test
  fun test03_folderAndFileManagement() = runBlocking {
    val project = projectRepository.createProject("FileSystemTest", "FS Test", "Empty")
    val pId = project.id

    // Create root folder
    val srcFolder = projectRepository.createFolder(pId, null, "src")
    assertEquals("src", srcFolder.name)
    assertEquals("src", srcFolder.path)

    // Create subfolder
    val mainFolder = projectRepository.createFolder(pId, srcFolder.id, "main")
    assertEquals("main", mainFolder.name)
    assertEquals("src/main", mainFolder.path)

    // Create file
    val rawCode = """
      class Test {
          fun main() {
              println("Hello")
              if (true) {
                  // Test
                  val x = 123
              }
          }
      }
    """.trimIndent()

    val file = projectRepository.createFile(pId, mainFolder.id, "Test.kt", rawCode)
    assertEquals("Test.kt", file.name)
    assertEquals("src/main/Test.kt", file.relativePath)
    assertEquals("kotlin", file.language)

    // Read file from disk
    val diskContent = projectRepository.readFileContent(file.id)
    assertEquals(rawCode, diskContent)

    // Rename file
    projectRepository.renameFile(file.id, "RenamedTest.kt")
    val renamedFile = projectRepository.getFile(file.id)
    assertEquals("RenamedTest.kt", renamedFile?.name)
    assertEquals("src/main/RenamedTest.kt", renamedFile?.relativePath)

    // Duplicate file
    val duplicate = projectRepository.duplicateFile(file.id)
    assertNotNull(duplicate)
    assertTrue(duplicate?.name?.contains("copy") == true)
    val dupContent = projectRepository.readFileContent(duplicate!!.id)
    assertEquals(rawCode, dupContent)

    // Star / Unstar file
    projectRepository.toggleStarFile(file.id)
    val starredFile = projectRepository.getFile(file.id)
    assertTrue(starredFile?.isStarred == true)
  }

  @Test
  fun test04_codeEditorVerbatimPreservation() = runBlocking {
    val project = projectRepository.createProject("EditorTest", "Editor", "Empty")
    val pId = project.id

    // Exact string with tabs, multiple spaces, line breaks, empty lines
    val rawCode = "class Test {\n\tfun main() {\n        println(\"Hello\")\n\n        if (true) {\n            // Test\n            val x = 123\n        }\n    }\n}\n"

    val file = projectRepository.createFile(pId, null, "Sample.kt", rawCode)
    val readBack = projectRepository.readFileContent(file.id)

    // Verify 100% byte-for-byte fidelity (CRITICAL: spaces, tabs, line breaks preserved)
    assertEquals(rawCode, readBack)
    assertEquals(rawCode.length, readBack.length)
    assertTrue(readBack.contains("\t"))
    assertTrue(readBack.contains("        println"))
    assertTrue(readBack.contains("\n\n"))

    // Save modified content
    val updatedCode = rawCode + "\n// Extra comment line\n"
    projectRepository.saveFileContent(file.id, updatedCode, createSnapshot = true, versionNote = "v2")
    val readUpdated = projectRepository.readFileContent(file.id)
    assertEquals(updatedCode, readUpdated)
  }

  @Test
  fun test05_syntaxHighlightingNonDestructive() {
    val code = """
      package com.example
      import java.util.*
      // This is a comment
      fun calculate(count: Int): String {
          val message = "Result is " + count
          return message
      }
    """.trimIndent()

    val highlighted = SyntaxHighlighter.highlight(code, "kotlin", isDark = true)
    // Verify the text content of the AnnotatedString is identical to the raw code
    assertEquals(code, highlighted.text)
    // Verify span styles exist for keywords, strings, comments
    assertTrue(highlighted.spanStyles.isNotEmpty())

    // Test other languages do not crash
    val pyCode = "def hello():\n    # python comment\n    return 42\n"
    val pyHighlighted = SyntaxHighlighter.highlight(pyCode, "python", isDark = true)
    assertEquals(pyCode, pyHighlighted.text)

    val jsCode = "const x = 'hello';\nfunction run() { return true; }"
    val jsHighlighted = SyntaxHighlighter.highlight(jsCode, "javascript", isDark = true)
    assertEquals(jsCode, jsHighlighted.text)
  }

  @Test
  fun test06_searchCapabilities() = runBlocking {
    val project = projectRepository.createProject("SearchTargetProject", "Target", "Empty")
    val pId = project.id

    projectRepository.createFile(
      pId,
      null,
      "ArenaViewModel.kt",
      "class ArenaViewModel {\n    fun triggerBattle() {\n        val score = 999\n    }\n}"
    )

    // Search by file name
    val nameResults = projectRepository.search("ArenaViewModel", "NAMES", pId)
    assertTrue(nameResults.isNotEmpty())
    assertEquals("ArenaViewModel.kt", nameResults.first().fileName)

    // Search by code content
    val contentResults = projectRepository.search("triggerBattle", "CONTENT", pId)
    assertTrue(contentResults.isNotEmpty())
    assertEquals(2, contentResults.first().lineNumber)
    assertTrue(contentResults.first().lineContent.contains("triggerBattle"))

    // Search across all projects
    val allResults = projectRepository.search("ArenaViewModel", "ALL", null)
    assertTrue(allResults.isNotEmpty())
  }

  @Test
  fun test07_trashAndRestore() = runBlocking {
    val project = projectRepository.createProject("TrashTestProject", "Trash", "Empty")
    val pId = project.id

    val file = projectRepository.createFile(pId, null, "DeleteMe.kt", "fun toDelete() {}")
    val fileId = file.id

    // Delete file -> Moves to trash
    projectRepository.deleteFile(fileId)
    val afterDelete = projectRepository.getFile(fileId)
    assertNull(afterDelete)

    // Check Trash contains the item
    val trashList = projectRepository.getAllTrashItems().first()
    val trashed = trashList.firstOrNull { it.originalId == fileId }
    assertNotNull(trashed)

    // Restore item
    projectRepository.restoreTrashItem(trashed!!.id)
    val restored = projectRepository.getFile(fileId)
    assertNotNull(restored)
    assertEquals("DeleteMe.kt", restored?.name)

    val restoredContent = projectRepository.readFileContent(fileId)
    assertEquals("fun toDelete() {}", restoredContent)
  }

  @Test
  fun test08_versionHistoryAndRestore() = runBlocking {
    val project = projectRepository.createProject("VersionTestProject", "Version", "Empty")
    val pId = project.id

    val file = projectRepository.createFile(pId, null, "History.kt", "Version 1")
    val fileId = file.id

    // Save Version 2
    projectRepository.saveFileContent(fileId, "Version 2", createSnapshot = true, versionNote = "v2")
    // Save Version 3
    projectRepository.saveFileContent(fileId, "Version 3", createSnapshot = true, versionNote = "v3")

    val versions = projectRepository.getVersionHistory(fileId).first()
    assertTrue(versions.size >= 2)

    val v1 = versions.last() // Initial Version
    val restored = projectRepository.restoreVersion(v1.id)
    assertTrue(restored)

    val currentContent = projectRepository.readFileContent(fileId)
    assertEquals("Version 1", currentContent)
  }

  @Test
  fun test09_zipExportAndImport() = runBlocking {
    val project = projectRepository.createProject("ZipProject", "ZIP Test", "Empty")
    val pId = project.id

    val srcFolder = projectRepository.createFolder(pId, null, "src")
    val mainFolder = projectRepository.createFolder(pId, srcFolder.id, "main")
    projectRepository.createFile(pId, mainFolder.id, "Test.kt", "class Test {}")

    val assetsFolder = projectRepository.createFolder(pId, null, "assets")
    projectRepository.createFile(pId, assetsFolder.id, "test.txt", "Sample asset content")

    projectRepository.createFile(pId, null, "README.md", "# Zip Project Documentation")

    // Export to ZIP
    val zipFile = File(context.cacheDir, "test_export.zip")
    val exportSuccess = projectRepository.exportProjectZip(pId, zipFile)
    assertTrue(exportSuccess)
    assertTrue(zipFile.exists())
    assertTrue(zipFile.length() > 0)

    // Validate ZIP entries
    val zip = ZipFile(zipFile)
    val entries = zip.entries().toList().map { it.name }
    assertTrue(entries.any { it.contains("README.md") })
    assertTrue(entries.any { it.contains("Test.kt") })
    assertTrue(entries.any { it.contains("test.txt") })
    zip.close()

    // Import from ZIP into a new project
    val importedProject = projectRepository.importProjectFromZip(zipFile.inputStream(), "ImportedZipProject")
    assertNotNull(importedProject.id)
    assertEquals("ImportedZipProject", importedProject.name)

    // Verify imported files in Room
    val importedFiles = projectRepository.getFilesInFolder(importedProject.id, null).first()
    assertTrue(importedFiles.any { it.name == "README.md" })
  }

  @Test
  fun test10_backupAndRestoreAll() = runBlocking {
    val project = projectRepository.createProject("BackupSourceProject", "Backup", "Empty")
    projectRepository.createFile(project.id, null, "Data.txt", "Important Data to Backup")

    val backupRecord = projectRepository.createBackup(project.id)
    assertNotNull(backupRecord.id)
    assertTrue(File(backupRecord.backupFilePath).exists())

    val backups = projectRepository.getAllBackups().first()
    assertTrue(backups.any { it.id == backupRecord.id })

    // Restore backup
    val restored = projectRepository.restoreBackupRecord(backupRecord.id)
    assertTrue(restored)
  }

  @Test
  fun test11_settingsPersistence() = runBlocking {
    settingsRepository.setThemeMode(AppThemeMode.CYBER)
    assertEquals(AppThemeMode.CYBER, settingsRepository.themeMode.first())

    settingsRepository.setLanguage(AppLanguage.EN)
    assertEquals(AppLanguage.EN, settingsRepository.language.first())

    settingsRepository.setEditorFontSize(18)
    assertEquals(18, settingsRepository.editorFontSize.first())

    settingsRepository.setLineNumbersEnabled(false)
    assertFalse(settingsRepository.lineNumbersEnabled.first())

    settingsRepository.setWordWrapEnabled(true)
    assertTrue(settingsRepository.wordWrapEnabled.first())

    settingsRepository.setTabSize(2)
    assertEquals(2, settingsRepository.tabSize.first())

    settingsRepository.setSyntaxHighlightingEnabled(true)
    assertTrue(settingsRepository.syntaxHighlightingEnabled.first())

    settingsRepository.setAutoSaveEnabled(true)
    assertTrue(settingsRepository.autoSaveEnabled.first())

    settingsRepository.setVipActive(true)
    assertTrue(settingsRepository.isVipActive.first())

    // Restore to default
    settingsRepository.setThemeMode(AppThemeMode.METALLIC_BLACK)
    assertEquals(AppThemeMode.METALLIC_BLACK, settingsRepository.themeMode.first())

    settingsRepository.setLanguage(AppLanguage.FA)
    assertEquals(AppLanguage.FA, settingsRepository.language.first())
  }
}
