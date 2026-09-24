package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
  tableName = "projects",
  indices = [Index(value = ["name"]), Index(value = ["isStarred"])]
)
data class ProjectEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val name: String,
  val description: String = "",
  val primaryLanguage: String = "Kotlin",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val isStarred: Boolean = false,
  val fileCount: Int = 0,
  val folderCount: Int = 0,
  val totalSizeBytes: Long = 0L
)

@Entity(
  tableName = "folders",
  indices = [
    Index(value = ["projectId"]),
    Index(value = ["parentFolderId"]),
    Index(value = ["projectId", "parentFolderId"])
  ]
)
data class FolderEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val projectId: String,
  val parentFolderId: String? = null,
  val name: String,
  val path: String,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val isStarred: Boolean = false
)

@Entity(
  tableName = "files",
  indices = [
    Index(value = ["projectId"]),
    Index(value = ["folderId"]),
    Index(value = ["isStarred"]),
    Index(value = ["projectId", "name"])
  ]
)
data class FileEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val projectId: String,
  val folderId: String? = null,
  val name: String,
  val extension: String,
  val relativePath: String,
  val sizeBytes: Long = 0L,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val isStarred: Boolean = false,
  val language: String = "plaintext"
)

@Entity(
  tableName = "recent_files",
  indices = [Index(value = ["fileId"], unique = true), Index(value = ["accessedAt"])]
)
data class RecentFileEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val fileId: String,
  val projectId: String,
  val accessedAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "trash_items",
  indices = [Index(value = ["projectId"]), Index(value = ["trashedAt"])]
)
data class TrashItemEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val originalType: String, // "PROJECT", "FOLDER", "FILE"
  val originalId: String,
  val projectId: String,
  val name: String,
  val originalPath: String,
  val trashedAt: Long = System.currentTimeMillis(),
  val sizeBytes: Long = 0L
)

@Entity(
  tableName = "version_history",
  indices = [Index(value = ["fileId"]), Index(value = ["createdAt"])]
)
data class VersionHistoryEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val fileId: String,
  val projectId: String,
  val versionName: String,
  val characterCount: Int,
  val content: String,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "backup_records",
  indices = [Index(value = ["projectId"]), Index(value = ["createdAt"])]
)
data class BackupRecordEntity(
  @PrimaryKey val id: String = UUID.randomUUID().toString(),
  val projectId: String?,
  val projectName: String,
  val backupFileName: String,
  val backupFilePath: String,
  val sizeBytes: Long,
  val fileCount: Int,
  val createdAt: Long = System.currentTimeMillis()
)
