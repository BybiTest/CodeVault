package com.example.data.fs

import android.content.Context
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

class FileManager(private val context: Context) {

  private val projectsRoot: File
    get() = File(context.filesDir, "projects").apply { if (!exists()) mkdirs() }

  private val trashRoot: File
    get() = File(context.filesDir, "trash").apply { if (!exists()) mkdirs() }

  private val backupsRoot: File
    get() = File(context.filesDir, "backups").apply { if (!exists()) mkdirs() }

  fun getProjectDir(projectId: String): File {
    return File(projectsRoot, projectId).apply { if (!exists()) mkdirs() }
  }

  fun getDiskFile(projectId: String, relativePath: String): File {
    val cleanRelative = relativePath.trimStart('/')
    return File(getProjectDir(projectId), cleanRelative)
  }

  fun readFileContent(projectId: String, relativePath: String): String {
    val file = getDiskFile(projectId, relativePath)
    if (!file.exists()) return ""
    return file.readText(Charsets.UTF_8)
  }

  fun writeFileContent(projectId: String, relativePath: String, content: String): Long {
    val file = getDiskFile(projectId, relativePath)
    file.parentFile?.let { if (!it.exists()) it.mkdirs() }
    file.writeText(content, Charsets.UTF_8)
    return file.length()
  }

  fun createFolder(projectId: String, relativePath: String): Boolean {
    val folder = getDiskFile(projectId, relativePath)
    return folder.mkdirs() || folder.exists()
  }

  fun renameFile(projectId: String, oldRelativePath: String, newRelativePath: String): Boolean {
    val oldFile = getDiskFile(projectId, oldRelativePath)
    val newFile = getDiskFile(projectId, newRelativePath)
    if (!oldFile.exists()) return false
    newFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
    return oldFile.renameTo(newFile)
  }

  fun deleteFileOrFolder(projectId: String, relativePath: String): Boolean {
    val file = getDiskFile(projectId, relativePath)
    return if (file.isDirectory) {
      file.deleteRecursively()
    } else {
      file.delete()
    }
  }

  fun moveFile(projectId: String, sourceRelativePath: String, targetRelativePath: String): Boolean {
    return renameFile(projectId, sourceRelativePath, targetRelativePath)
  }

  fun duplicateFile(projectId: String, sourceRelativePath: String, targetRelativePath: String): Long {
    val source = getDiskFile(projectId, sourceRelativePath)
    val target = getDiskFile(projectId, targetRelativePath)
    if (!source.exists()) return 0L
    target.parentFile?.let { if (!it.exists()) it.mkdirs() }
    source.copyTo(target, overwrite = true)
    return target.length()
  }

  // Moves file/folder to trash storage
  fun stageToTrash(id: String, projectId: String, relativePath: String): String {
    val src = getDiskFile(projectId, relativePath)
    val trashTarget = File(trashRoot, id)
    if (src.exists()) {
      src.copyRecursively(trashTarget, overwrite = true)
      if (src.isDirectory) src.deleteRecursively() else src.delete()
    }
    return trashTarget.absolutePath
  }

  // Restores from trash storage back to project
  fun restoreFromTrash(id: String, projectId: String, relativePath: String): Boolean {
    val trashSource = File(trashRoot, id)
    val projectTarget = getDiskFile(projectId, relativePath)
    if (!trashSource.exists()) return false
    projectTarget.parentFile?.let { if (!it.exists()) it.mkdirs() }
    val success = trashSource.copyRecursively(projectTarget, overwrite = true)
    if (success) {
      if (trashSource.isDirectory) trashSource.deleteRecursively() else trashSource.delete()
    }
    return success
  }

  fun deleteFromTrashPermanently(id: String) {
    val trashTarget = File(trashRoot, id)
    if (trashTarget.exists()) {
      if (trashTarget.isDirectory) trashTarget.deleteRecursively() else trashTarget.delete()
    }
  }

  fun emptyTrashFolder() {
    trashRoot.listFiles()?.forEach { file ->
      if (file.isDirectory) file.deleteRecursively() else file.delete()
    }
  }

  // Export project to a ZIP file
  fun exportProjectToZip(projectId: String, projectName: String, destinationFile: File): Boolean {
    val projectDir = getProjectDir(projectId)
    if (!projectDir.exists()) return false
    destinationFile.parentFile?.let { if (!it.exists()) it.mkdirs() }

    ZipOutputStream(BufferedOutputStream(FileOutputStream(destinationFile))).use { zos ->
      zipDirectory(projectDir, projectDir, zos)
    }
    return destinationFile.exists() && destinationFile.length() > 0
  }

  private fun zipDirectory(root: File, current: File, zos: ZipOutputStream) {
    val files = current.listFiles() ?: return
    for (file in files) {
      if (file.isDirectory) {
        val relativePath = file.relativeTo(root).path.replace('\\', '/') + "/"
        zos.putNextEntry(ZipEntry(relativePath))
        zos.closeEntry()
        zipDirectory(root, file, zos)
      } else {
        val relativePath = file.relativeTo(root).path.replace('\\', '/')
        zos.putNextEntry(ZipEntry(relativePath))
        file.inputStream().use { it.copyTo(zos) }
        zos.closeEntry()
      }
    }
  }

  // Import project from ZIP InputStream, returns list of relative paths unpacked
  fun importZipToProject(inputStream: InputStream, projectId: String): List<String> {
    val projectDir = getProjectDir(projectId)
    val unpackedPaths = mutableListOf<String>()

    ZipInputStream(BufferedInputStream(inputStream)).use { zis ->
      var entry: ZipEntry? = zis.nextEntry
      while (entry != null) {
        val entryName = entry.name.replace('\\', '/')
        // Security check for Zip Slip
        val destinationFile = File(projectDir, entryName)
        if (!destinationFile.canonicalPath.startsWith(projectDir.canonicalPath)) {
          entry = zis.nextEntry
          continue
        }

        if (entry.isDirectory) {
          destinationFile.mkdirs()
        } else {
          destinationFile.parentFile?.let { if (!it.exists()) it.mkdirs() }
          FileOutputStream(destinationFile).use { fos ->
            zis.copyTo(fos)
          }
          unpackedPaths.add(entryName)
        }
        zis.closeEntry()
        entry = zis.nextEntry
      }
    }
    return unpackedPaths
  }

  fun createLocalBackup(projectId: String?, projectName: String): File {
    val timestamp = System.currentTimeMillis()
    val cleanName = projectName.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
    val backupFile = File(backupsRoot, "backup_${cleanName}_$timestamp.zip")
    if (projectId != null) {
      exportProjectToZip(projectId, projectName, backupFile)
    } else {
      // Backup all projects
      ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zos ->
        zipDirectory(projectsRoot, projectsRoot, zos)
      }
    }
    return backupFile
  }

  fun getBackupsDirectory(): File = backupsRoot

  fun getTrashDirectory(): File = trashRoot

  fun clearAppCache(): Long {
    val cacheDir = context.cacheDir
    val bytes = getFolderSize(cacheDir)
    cacheDir.deleteRecursively()
    cacheDir.mkdirs()
    return bytes
  }

  fun getFolderSize(file: File): Long {
    if (!file.exists()) return 0L
    if (file.isFile) return file.length()
    var size = 0L
    file.listFiles()?.forEach { child ->
      size += getFolderSize(child)
    }
    return size
  }

  companion object {
    fun detectLanguage(extension: String): String {
      return when (extension.lowercase()) {
        "kt", "kts" -> "kotlin"
        "java" -> "java"
        "py" -> "python"
        "js", "mjs", "cjs" -> "javascript"
        "ts", "tsx" -> "typescript"
        "html", "htm" -> "html"
        "css", "scss", "sass" -> "css"
        "json" -> "json"
        "xml" -> "xml"
        "sql" -> "sql"
        "md", "markdown" -> "markdown"
        "c", "h" -> "c"
        "cpp", "hpp", "cc" -> "cpp"
        "sh", "bash" -> "shell"
        "yaml", "yml" -> "yaml"
        "gradle" -> "gradle"
        "toml" -> "toml"
        else -> "plaintext"
      }
    }
  }
}
