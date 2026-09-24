package com.example.data.repository

import com.example.data.fs.FileManager
import com.example.data.local.CodeVaultDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.util.UUID

data class SearchResultItem(
  val fileId: String,
  val fileName: String,
  val relativePath: String,
  val projectId: String,
  val projectName: String,
  val lineNumber: Int,
  val lineContent: String,
  val matchIndex: Int
)

data class StorageStats(
  val projectCount: Int,
  val fileCount: Int,
  val folderCount: Int,
  val projectsSizeBytes: Long,
  val trashSizeBytes: Long,
  val backupSizeBytes: Long
)

class ProjectRepository(
  private val database: CodeVaultDatabase,
  private val fileManager: FileManager
) {
  private val projectDao = database.projectDao()
  private val folderDao = database.folderDao()
  private val fileDao = database.fileDao()
  private val recentFileDao = database.recentFileDao()
  private val trashDao = database.trashDao()
  private val versionHistoryDao = database.versionHistoryDao()
  private val backupDao = database.backupDao()

  fun getAllProjects(): Flow<List<ProjectEntity>> = projectDao.getAllProjects()
  fun getStarredProjects(): Flow<List<ProjectEntity>> = projectDao.getStarredProjects()
  fun getRecentProjects(): Flow<List<ProjectEntity>> = projectDao.getRecentProjects()
  fun observeProject(id: String): Flow<ProjectEntity?> = projectDao.observeProjectById(id)
  suspend fun getProject(id: String): ProjectEntity? = projectDao.getProjectById(id)

  suspend fun createProject(
    name: String,
    description: String = "",
    template: String = "Kotlin"
  ): ProjectEntity = withContext(Dispatchers.IO) {
    val projectId = UUID.randomUUID().toString()
    val project = ProjectEntity(
      id = projectId,
      name = name.trim(),
      description = description.trim(),
      primaryLanguage = template,
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis(),
      fileCount = 0,
      folderCount = 0,
      totalSizeBytes = 0L
    )
    projectDao.insertProject(project)
    fileManager.getProjectDir(projectId)

    // Seed starter files based on template
    when (template.lowercase()) {
      "kotlin", "android" -> {
        val srcFolder = createFolder(projectId, null, "src")
        val mainFolder = createFolder(projectId, srcFolder.id, "main")
        val javaFolder = createFolder(projectId, mainFolder.id, "kotlin")
        createFile(
          projectId = projectId,
          folderId = javaFolder.id,
          name = "Main.kt",
          initialContent = """
            package com.example.app

            /**
             * Welcome to CodeVault!
             * Real native project & code manager.
             */
            fun main() {
                val appName = "CodeVault"
                println("Hello from ${'$'}appName on Android!")
                val features = listOf("Local-first", "Syntax Highlighter", "Version History", "ZIP Backup")
                for (feature in features) {
                    println(" - Feature: ${'$'}feature")
                }
            }
          """.trimIndent()
        )
        createFile(
          projectId = projectId,
          folderId = null,
          name = "build.gradle.kts",
          initialContent = """
            plugins {
                kotlin("jvm") version "2.2.10"
            }

            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
          """.trimIndent()
        )
        createFile(
          projectId = projectId,
          folderId = null,
          name = "README.md",
          initialContent = "# $name\n\nCreated with CodeVault on Android.\nLocal-first development environment."
        )
      }
      "python" -> {
        createFile(
          projectId = projectId,
          folderId = null,
          name = "main.py",
          initialContent = """
            # Welcome to $name
            # Managed with CodeVault

            def run_vault():
                print("CodeVault Python Engine Running...")
                print("100% Local-first code storage")

            if __name__ == "__main__":
                run_vault()
          """.trimIndent()
        )
        createFile(
          projectId = projectId,
          folderId = null,
          name = "requirements.txt",
          initialContent = "# Dependencies for $name\nrequests>=2.28.0\n"
        )
        createFile(
          projectId = projectId,
          folderId = null,
          name = "README.md",
          initialContent = "# $name\n\n$description\n"
        )
      }
      "web", "javascript", "html" -> {
        createFile(
          projectId = projectId,
          folderId = null,
          name = "index.html",
          initialContent = """
            <!DOCTYPE html>
            <html lang="fa" dir="rtl">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>$name - CodeVault</title>
                <link rel="stylesheet" href="style.css">
            </head>
            <body>
                <header>
                    <h1>$name</h1>
                    <p>پروژه ایجاد شده در CodeVault</p>
                </header>
                <main id="app">
                    <button id="btnClick">کلیک کنید</button>
                </main>
                <script src="script.js"></script>
            </body>
            </html>
          """.trimIndent()
        )
        createFile(
          projectId = projectId,
          folderId = null,
          name = "style.css",
          initialContent = """
            body {
                background-color: #0F1115;
                color: #F8FAFC;
                font-family: system-ui, sans-serif;
                margin: 0;
                padding: 24px;
            }
            button {
                background: #38BDF8;
                color: #001A2C;
                border: none;
                padding: 10px 20px;
                border-radius: 8px;
                cursor: pointer;
            }
          """.trimIndent()
        )
        createFile(
          projectId = projectId,
          folderId = null,
          name = "script.js",
          initialContent = """
            document.addEventListener('DOMContentLoaded', () => {
                console.log('$name loaded in CodeVault.');
                document.getElementById('btnClick').onclick = () => {
                    alert('سلام از CodeVault!');
                };
            });
          """.trimIndent()
        )
      }
      else -> {
        createFile(
          projectId = projectId,
          folderId = null,
          name = "README.md",
          initialContent = "# $name\n\n$description\n\nCreated with CodeVault."
        )
      }
    }

    refreshProjectStats(projectId)
    projectDao.getProjectById(projectId) ?: project
  }

  suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
    projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
  }

  suspend fun toggleStarProject(id: String) = withContext(Dispatchers.IO) {
    val project = projectDao.getProjectById(id) ?: return@withContext
    projectDao.setStarred(id, !project.isStarred)
  }

  suspend fun deleteProject(id: String) = withContext(Dispatchers.IO) {
    val project = projectDao.getProjectById(id) ?: return@withContext
    // Stage whole project directory to trash
    val trashId = UUID.randomUUID().toString()
    fileManager.stageToTrash(trashId, id, "")

    trashDao.insertTrashItem(
      TrashItemEntity(
        id = trashId,
        originalType = "PROJECT",
        originalId = id,
        projectId = id,
        name = project.name,
        originalPath = project.name,
        trashedAt = System.currentTimeMillis(),
        sizeBytes = project.totalSizeBytes
      )
    )

    projectDao.deleteProjectById(id)
    folderDao.deleteFoldersByProjectId(id)
    fileDao.deleteFilesByProjectId(id)
    recentFileDao.deleteByProjectId(id)
    versionHistoryDao.deleteVersionsForProject(id)
  }

  // Folder Operations
  fun getFoldersInParent(projectId: String, parentId: String?): Flow<List<FolderEntity>> {
    return folderDao.getFoldersInParent(projectId, parentId)
  }

  suspend fun createFolder(
    projectId: String,
    parentFolderId: String?,
    name: String
  ): FolderEntity = withContext(Dispatchers.IO) {
    val parentFolder = if (parentFolderId != null) folderDao.getFolderById(parentFolderId) else null
    val relativePath = if (parentFolder != null) "${parentFolder.path}/$name" else name
    val folderId = UUID.randomUUID().toString()

    fileManager.createFolder(projectId, relativePath)

    val folder = FolderEntity(
      id = folderId,
      projectId = projectId,
      parentFolderId = parentFolderId,
      name = name.trim(),
      path = relativePath,
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis(),
      isStarred = false
    )
    folderDao.insertFolder(folder)
    refreshProjectStats(projectId)
    folder
  }

  suspend fun deleteFolder(folderId: String) = withContext(Dispatchers.IO) {
    val folder = folderDao.getFolderById(folderId) ?: return@withContext
    val trashId = UUID.randomUUID().toString()
    fileManager.stageToTrash(trashId, folder.projectId, folder.path)

    trashDao.insertTrashItem(
      TrashItemEntity(
        id = trashId,
        originalType = "FOLDER",
        originalId = folderId,
        projectId = folder.projectId,
        name = folder.name,
        originalPath = folder.path,
        trashedAt = System.currentTimeMillis()
      )
    )
    folderDao.deleteFolderById(folderId)
    refreshProjectStats(folder.projectId)
  }

  suspend fun renameFolder(folderId: String, newName: String) = withContext(Dispatchers.IO) {
    val folder = folderDao.getFolderById(folderId) ?: return@withContext
    val parentPath = folder.path.substringBeforeLast('/', "")
    val newPath = if (parentPath.isEmpty()) newName else "$parentPath/$newName"
    if (fileManager.renameFile(folder.projectId, folder.path, newPath)) {
      folderDao.updateFolder(folder.copy(name = newName, path = newPath, updatedAt = System.currentTimeMillis()))
    }
  }

  // File Operations
  fun getFilesInFolder(projectId: String, folderId: String?): Flow<List<FileEntity>> {
    return fileDao.getFilesInFolder(projectId, folderId)
  }

  fun getStarredFiles(): Flow<List<FileEntity>> = fileDao.getStarredFiles()

  suspend fun getFile(fileId: String): FileEntity? = fileDao.getFileById(fileId)
  fun observeFile(fileId: String): Flow<FileEntity?> = fileDao.observeFileById(fileId)

  suspend fun createFile(
    projectId: String,
    folderId: String?,
    name: String,
    initialContent: String = ""
  ): FileEntity = withContext(Dispatchers.IO) {
    val parentFolder = if (folderId != null) folderDao.getFolderById(folderId) else null
    val relativePath = if (parentFolder != null) "${parentFolder.path}/$name" else name
    val extension = name.substringAfterLast('.', "")
    val language = FileManager.detectLanguage(extension)
    val fileId = UUID.randomUUID().toString()

    val sizeBytes = fileManager.writeFileContent(projectId, relativePath, initialContent)

    val file = FileEntity(
      id = fileId,
      projectId = projectId,
      folderId = folderId,
      name = name.trim(),
      extension = extension,
      relativePath = relativePath,
      sizeBytes = sizeBytes,
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis(),
      isStarred = false,
      language = language
    )
    fileDao.insertFile(file)

    // Save initial version snapshot
    if (initialContent.isNotEmpty()) {
      versionHistoryDao.insertVersion(
        VersionHistoryEntity(
          fileId = fileId,
          projectId = projectId,
          versionName = "Initial Version",
          characterCount = initialContent.length,
          content = initialContent,
          createdAt = System.currentTimeMillis()
        )
      )
    }

    refreshProjectStats(projectId)
    file
  }

  suspend fun readFileContent(fileId: String): String = withContext(Dispatchers.IO) {
    val file = fileDao.getFileById(fileId) ?: return@withContext ""
    fileManager.readFileContent(file.projectId, file.relativePath)
  }

  suspend fun saveFileContent(
    fileId: String,
    content: String,
    createSnapshot: Boolean = false,
    versionNote: String = ""
  ) = withContext(Dispatchers.IO) {
    val file = fileDao.getFileById(fileId) ?: return@withContext
    val newSizeBytes = fileManager.writeFileContent(file.projectId, file.relativePath, content)
    fileDao.updateFile(file.copy(sizeBytes = newSizeBytes, updatedAt = System.currentTimeMillis()))

    if (createSnapshot) {
      val versionName = if (versionNote.isNotBlank()) versionNote else "Saved at ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())}"
      versionHistoryDao.insertVersion(
        VersionHistoryEntity(
          fileId = fileId,
          projectId = file.projectId,
          versionName = versionName,
          characterCount = content.length,
          content = content,
          createdAt = System.currentTimeMillis()
        )
      )
    }
    refreshProjectStats(file.projectId)
  }

  suspend fun renameFile(fileId: String, newName: String) = withContext(Dispatchers.IO) {
    val file = fileDao.getFileById(fileId) ?: return@withContext
    val parentPath = file.relativePath.substringBeforeLast('/', "")
    val newRelativePath = if (parentPath.isEmpty()) newName else "$parentPath/$newName"
    val extension = newName.substringAfterLast('.', "")
    val language = FileManager.detectLanguage(extension)

    if (fileManager.renameFile(file.projectId, file.relativePath, newRelativePath)) {
      fileDao.updateFile(
        file.copy(
          name = newName,
          extension = extension,
          relativePath = newRelativePath,
          language = language,
          updatedAt = System.currentTimeMillis()
        )
      )
    }
  }

  suspend fun moveFile(fileId: String, targetFolderId: String?) = withContext(Dispatchers.IO) {
    val file = fileDao.getFileById(fileId) ?: return@withContext
    val targetFolder = if (targetFolderId != null) folderDao.getFolderById(targetFolderId) else null
    val newRelativePath = if (targetFolder != null) "${targetFolder.path}/${file.name}" else file.name

    if (fileManager.moveFile(file.projectId, file.relativePath, newRelativePath)) {
      fileDao.updateFile(
        file.copy(
          folderId = targetFolderId,
          relativePath = newRelativePath,
          updatedAt = System.currentTimeMillis()
        )
      )
    }
  }

  suspend fun duplicateFile(fileId: String): FileEntity? = withContext(Dispatchers.IO) {
    val file = fileDao.getFileById(fileId) ?: return@withContext null
    val baseName = file.name.substringBeforeLast('.')
    val ext = if (file.extension.isNotEmpty()) ".${file.extension}" else ""
    val newName = "${baseName}_copy$ext"
    val parentPath = file.relativePath.substringBeforeLast('/', "")
    val newRelativePath = if (parentPath.isEmpty()) newName else "$parentPath/$newName"

    val newSize = fileManager.duplicateFile(file.projectId, file.relativePath, newRelativePath)
    val newId = UUID.randomUUID().toString()

    val copy = FileEntity(
      id = newId,
      projectId = file.projectId,
      folderId = file.folderId,
      name = newName,
      extension = file.extension,
      relativePath = newRelativePath,
      sizeBytes = newSize,
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis(),
      isStarred = false,
      language = file.language
    )
    fileDao.insertFile(copy)
    refreshProjectStats(file.projectId)
    copy
  }

  suspend fun deleteFile(fileId: String) = withContext(Dispatchers.IO) {
    val file = fileDao.getFileById(fileId) ?: return@withContext
    val trashId = UUID.randomUUID().toString()
    fileManager.stageToTrash(trashId, file.projectId, file.relativePath)

    trashDao.insertTrashItem(
      TrashItemEntity(
        id = trashId,
        originalType = "FILE",
        originalId = fileId,
        projectId = file.projectId,
        name = file.name,
        originalPath = file.relativePath,
        trashedAt = System.currentTimeMillis(),
        sizeBytes = file.sizeBytes
      )
    )

    fileDao.deleteFileById(fileId)
    recentFileDao.deleteByFileId(fileId)
    refreshProjectStats(file.projectId)
  }

  suspend fun toggleStarFile(fileId: String) = withContext(Dispatchers.IO) {
    val file = fileDao.getFileById(fileId) ?: return@withContext
    fileDao.setStarred(fileId, !file.isStarred)
  }

  // Recent Files
  fun getRecentFiles(): Flow<List<RecentFileEntity>> = recentFileDao.getRecentFiles()

  suspend fun recordRecentFileAccess(fileId: String, projectId: String) = withContext(Dispatchers.IO) {
    recentFileDao.insertRecent(
      RecentFileEntity(
        fileId = fileId,
        projectId = projectId,
        accessedAt = System.currentTimeMillis()
      )
    )
  }

  // Trash
  fun getAllTrashItems(): Flow<List<TrashItemEntity>> = trashDao.getAllTrashItems()

  suspend fun restoreTrashItem(trashId: String) = withContext(Dispatchers.IO) {
    val item = trashDao.getTrashItemById(trashId) ?: return@withContext
    when (item.originalType) {
      "PROJECT" -> {
        fileManager.restoreFromTrash(trashId, item.projectId, "")
        // Re-read and re-insert project, folders, and files
        val project = ProjectEntity(
          id = item.projectId,
          name = item.name,
          createdAt = item.trashedAt,
          updatedAt = System.currentTimeMillis(),
          totalSizeBytes = item.sizeBytes
        )
        projectDao.insertProject(project)
        syncDirectoryStructureToDb(item.projectId)
      }
      "FOLDER" -> {
        fileManager.restoreFromTrash(trashId, item.projectId, item.originalPath)
        val folder = FolderEntity(
          id = item.originalId,
          projectId = item.projectId,
          parentFolderId = null,
          name = item.name,
          path = item.originalPath,
          updatedAt = System.currentTimeMillis()
        )
        folderDao.insertFolder(folder)
        syncDirectoryStructureToDb(item.projectId)
      }
      "FILE" -> {
        fileManager.restoreFromTrash(trashId, item.projectId, item.originalPath)
        val extension = item.name.substringAfterLast('.', "")
        val file = FileEntity(
          id = item.originalId,
          projectId = item.projectId,
          folderId = null,
          name = item.name,
          extension = extension,
          relativePath = item.originalPath,
          sizeBytes = item.sizeBytes,
          updatedAt = System.currentTimeMillis(),
          language = FileManager.detectLanguage(extension)
        )
        fileDao.insertFile(file)
      }
    }
    trashDao.deleteTrashItemById(trashId)
    refreshProjectStats(item.projectId)
  }

  suspend fun deleteTrashItemPermanently(trashId: String) = withContext(Dispatchers.IO) {
    fileManager.deleteFromTrashPermanently(trashId)
    trashDao.deleteTrashItemById(trashId)
  }

  suspend fun emptyTrash() = withContext(Dispatchers.IO) {
    fileManager.emptyTrashFolder()
    trashDao.emptyTrash()
  }

  // Version History
  fun getVersionHistory(fileId: String): Flow<List<VersionHistoryEntity>> {
    return versionHistoryDao.getVersionsForFile(fileId)
  }

  suspend fun restoreVersion(versionId: String): Boolean = withContext(Dispatchers.IO) {
    val version = versionHistoryDao.getVersionById(versionId) ?: return@withContext false
    val file = fileDao.getFileById(version.fileId) ?: return@withContext false
    // Write old content back to disk
    fileManager.writeFileContent(file.projectId, file.relativePath, version.content)
    fileDao.updateFile(file.copy(sizeBytes = version.content.toByteArray().size.toLong(), updatedAt = System.currentTimeMillis()))
    // Record this restore action as a new snapshot too
    versionHistoryDao.insertVersion(
      VersionHistoryEntity(
        fileId = file.id,
        projectId = file.projectId,
        versionName = "Restored from: ${version.versionName}",
        characterCount = version.characterCount,
        content = version.content,
        createdAt = System.currentTimeMillis()
      )
    )
    refreshProjectStats(file.projectId)
    true
  }

  // Backup & Restore
  fun getAllBackups(): Flow<List<BackupRecordEntity>> = backupDao.getAllBackups()

  suspend fun createBackup(projectId: String?): BackupRecordEntity = withContext(Dispatchers.IO) {
    val projectName = if (projectId != null) {
      projectDao.getProjectById(projectId)?.name ?: "Project"
    } else {
      "All_Projects"
    }
    val backupFile = fileManager.createLocalBackup(projectId, projectName)
    val fileCount = if (projectId != null) fileDao.countFilesInProject(projectId) else fileDao.getTotalFileCount().first()

    val record = BackupRecordEntity(
      projectId = projectId,
      projectName = projectName,
      backupFileName = backupFile.name,
      backupFilePath = backupFile.absolutePath,
      sizeBytes = backupFile.length(),
      fileCount = fileCount,
      createdAt = System.currentTimeMillis()
    )
    backupDao.insertBackup(record)
    record
  }

  suspend fun restoreBackupRecord(backupId: String): Boolean = withContext(Dispatchers.IO) {
    val backup = backupDao.getBackupById(backupId) ?: return@withContext false
    val backupFile = File(backup.backupFilePath)
    if (!backupFile.exists()) return@withContext false

    backupFile.inputStream().use { stream ->
      if (backup.projectId != null) {
        fileManager.importZipToProject(stream, backup.projectId)
        syncDirectoryStructureToDb(backup.projectId)
      } else {
        // Unzip into projects directory
        val projectsDir = fileManager.getProjectDir("temp").parentFile!!
        java.util.zip.ZipInputStream(stream).use { zis ->
          var entry = zis.nextEntry
          while (entry != null) {
            val target = File(projectsDir, entry.name)
            if (entry.isDirectory) {
              target.mkdirs()
            } else {
              target.parentFile?.mkdirs()
              target.outputStream().use { zis.copyTo(it) }
            }
            zis.closeEntry()
            entry = zis.nextEntry
          }
        }
      }
    }
    true
  }

  // Import / Export
  suspend fun importProjectFromZip(
    inputStream: InputStream,
    projectName: String
  ): ProjectEntity = withContext(Dispatchers.IO) {
    val projectId = UUID.randomUUID().toString()
    val project = ProjectEntity(
      id = projectId,
      name = projectName.ifBlank { "Imported_Project" },
      description = "Imported from ZIP archive",
      createdAt = System.currentTimeMillis(),
      updatedAt = System.currentTimeMillis()
    )
    projectDao.insertProject(project)

    fileManager.importZipToProject(inputStream, projectId)
    syncDirectoryStructureToDb(projectId)
    refreshProjectStats(projectId)
    projectDao.getProjectById(projectId) ?: project
  }

  suspend fun exportProjectZip(projectId: String, destinationFile: File): Boolean = withContext(Dispatchers.IO) {
    val project = projectDao.getProjectById(projectId) ?: return@withContext false
    fileManager.exportProjectToZip(projectId, project.name, destinationFile)
  }

  // Search
  suspend fun search(
    query: String,
    scope: String = "ALL", // "ALL", "PROJECT", "NAMES", "CONTENT"
    projectId: String? = null
  ): List<SearchResultItem> = withContext(Dispatchers.IO) {
    if (query.isBlank()) return@withContext emptyList()
    val results = mutableListOf<SearchResultItem>()
    val projects = if (projectId != null) {
      listOfNotNull(projectDao.getProjectById(projectId))
    } else {
      projectDao.getAllProjects().first()
    }

    for (p in projects) {
      val files = fileDao.getAllFilesInProject(p.id)
      for (f in files) {
        // Match name
        if (scope == "ALL" || scope == "NAMES") {
          if (f.name.contains(query, ignoreCase = true)) {
            results.add(
              SearchResultItem(
                fileId = f.id,
                fileName = f.name,
                relativePath = f.relativePath,
                projectId = p.id,
                projectName = p.name,
                lineNumber = 1,
                lineContent = f.name,
                matchIndex = f.name.indexOf(query, ignoreCase = true)
              )
            )
          }
        }

        // Match content
        if (scope == "ALL" || scope == "CONTENT") {
          val content = fileManager.readFileContent(p.id, f.relativePath)
          val lines = content.lines()
          for ((idx, line) in lines.withIndex()) {
            if (line.contains(query, ignoreCase = true)) {
              results.add(
                SearchResultItem(
                  fileId = f.id,
                  fileName = f.name,
                  relativePath = f.relativePath,
                  projectId = p.id,
                  projectName = p.name,
                  lineNumber = idx + 1,
                  lineContent = line.trim(),
                  matchIndex = line.indexOf(query, ignoreCase = true)
                )
              )
              if (results.size >= 100) break // limit results
            }
          }
        }
        if (results.size >= 100) break
      }
    }
    results
  }

  // Storage Stats
  suspend fun getStorageStats(): StorageStats = withContext(Dispatchers.IO) {
    val projectCount = projectDao.getProjectCount().first()
    val fileCount = fileDao.getTotalFileCount().first()
    val folderCount = folderDao.getTotalFolderCount().first()
    val projectsSize = projectDao.getTotalProjectsSize().first() ?: 0L
    val trashSize = trashDao.getTotalTrashSize().first() ?: 0L
    val backupSize = backupDao.getTotalBackupSize().first() ?: 0L

    StorageStats(
      projectCount = projectCount,
      fileCount = fileCount,
      folderCount = folderCount,
      projectsSizeBytes = projectsSize,
      trashSizeBytes = trashSize,
      backupSizeBytes = backupSize
    )
  }

  fun clearAppCache(): Long = fileManager.clearAppCache()

  // Helper to re-scan physical project files into Room entities
  private suspend fun syncDirectoryStructureToDb(projectId: String) {
    val projectDir = fileManager.getProjectDir(projectId)
    scanDir(projectDir, projectDir, projectId, null)
  }

  private suspend fun scanDir(root: File, current: File, projectId: String, parentFolderId: String?) {
    val children = current.listFiles() ?: return
    for (child in children) {
      val relPath = child.relativeTo(root).path.replace('\\', '/')
      if (child.isDirectory) {
        val folderId = UUID.randomUUID().toString()
        folderDao.insertFolder(
          FolderEntity(
            id = folderId,
            projectId = projectId,
            parentFolderId = parentFolderId,
            name = child.name,
            path = relPath
          )
        )
        scanDir(root, child, projectId, folderId)
      } else {
        val ext = child.extension
        val fileId = UUID.randomUUID().toString()
        fileDao.insertFile(
          FileEntity(
            id = fileId,
            projectId = projectId,
            folderId = parentFolderId,
            name = child.name,
            extension = ext,
            relativePath = relPath,
            sizeBytes = child.length(),
            language = FileManager.detectLanguage(ext)
          )
        )
      }
    }
  }

  private suspend fun refreshProjectStats(projectId: String) {
    val countF = fileDao.countFilesInProject(projectId)
    val countD = folderDao.countFoldersInProject(projectId)
    val totalSize = fileDao.sumFileSizeInProject(projectId) ?: 0L
    val p = projectDao.getProjectById(projectId) ?: return
    projectDao.updateProject(
      p.copy(
        fileCount = countF,
        folderCount = countD,
        totalSizeBytes = totalSize,
        updatedAt = System.currentTimeMillis()
      )
    )
  }
}
