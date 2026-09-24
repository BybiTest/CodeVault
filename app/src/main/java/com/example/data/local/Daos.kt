package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
  @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
  fun getAllProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE isStarred = 1 ORDER BY updatedAt DESC")
  fun getStarredProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects ORDER BY updatedAt DESC LIMIT 5")
  fun getRecentProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE id = :id")
  suspend fun getProjectById(id: String): ProjectEntity?

  @Query("SELECT * FROM projects WHERE id = :id")
  fun observeProjectById(id: String): Flow<ProjectEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProject(project: ProjectEntity)

  @Update
  suspend fun updateProject(project: ProjectEntity)

  @Query("DELETE FROM projects WHERE id = :id")
  suspend fun deleteProjectById(id: String)

  @Query("UPDATE projects SET isStarred = :isStarred WHERE id = :id")
  suspend fun setStarred(id: String, isStarred: Boolean)

  @Query("SELECT COUNT(*) FROM projects")
  fun getProjectCount(): Flow<Int>

  @Query("SELECT SUM(totalSizeBytes) FROM projects")
  fun getTotalProjectsSize(): Flow<Long?>

  @Query("SELECT * FROM projects WHERE name LIKE '%' || :query || '%'")
  suspend fun searchProjects(query: String): List<ProjectEntity>
}

@Dao
interface FolderDao {
  @Query("SELECT * FROM folders WHERE projectId = :projectId AND (parentFolderId = :parentId OR (:parentId IS NULL AND parentFolderId IS NULL)) ORDER BY name ASC")
  fun getFoldersInParent(projectId: String, parentId: String?): Flow<List<FolderEntity>>

  @Query("SELECT * FROM folders WHERE projectId = :projectId")
  suspend fun getAllFoldersInProject(projectId: String): List<FolderEntity>

  @Query("SELECT * FROM folders WHERE id = :id")
  suspend fun getFolderById(id: String): FolderEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFolder(folder: FolderEntity)

  @Update
  suspend fun updateFolder(folder: FolderEntity)

  @Query("DELETE FROM folders WHERE id = :id")
  suspend fun deleteFolderById(id: String)

  @Query("DELETE FROM folders WHERE projectId = :projectId")
  suspend fun deleteFoldersByProjectId(projectId: String)

  @Query("SELECT COUNT(*) FROM folders")
  fun getTotalFolderCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM folders WHERE projectId = :projectId")
  suspend fun countFoldersInProject(projectId: String): Int
}

@Dao
interface FileDao {
  @Query("SELECT * FROM files WHERE projectId = :projectId AND (folderId = :folderId OR (:folderId IS NULL AND folderId IS NULL)) ORDER BY name ASC")
  fun getFilesInFolder(projectId: String, folderId: String?): Flow<List<FileEntity>>

  @Query("SELECT * FROM files WHERE projectId = :projectId")
  suspend fun getAllFilesInProject(projectId: String): List<FileEntity>

  @Query("SELECT * FROM files WHERE isStarred = 1 ORDER BY updatedAt DESC")
  fun getStarredFiles(): Flow<List<FileEntity>>

  @Query("SELECT * FROM files WHERE id = :id")
  suspend fun getFileById(id: String): FileEntity?

  @Query("SELECT * FROM files WHERE id = :id")
  fun observeFileById(id: String): Flow<FileEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFile(file: FileEntity)

  @Update
  suspend fun updateFile(file: FileEntity)

  @Query("DELETE FROM files WHERE id = :id")
  suspend fun deleteFileById(id: String)

  @Query("DELETE FROM files WHERE projectId = :projectId")
  suspend fun deleteFilesByProjectId(projectId: String)

  @Query("UPDATE files SET isStarred = :isStarred WHERE id = :id")
  suspend fun setStarred(id: String, isStarred: Boolean)

  @Query("SELECT COUNT(*) FROM files")
  fun getTotalFileCount(): Flow<Int>

  @Query("SELECT COUNT(*) FROM files WHERE projectId = :projectId")
  suspend fun countFilesInProject(projectId: String): Int

  @Query("SELECT SUM(sizeBytes) FROM files WHERE projectId = :projectId")
  suspend fun sumFileSizeInProject(projectId: String): Long?

  @Query("SELECT * FROM files WHERE name LIKE '%' || :query || '%'")
  suspend fun searchFilesByName(query: String): List<FileEntity>
}

@Dao
interface RecentFileDao {
  @Query("SELECT * FROM recent_files ORDER BY accessedAt DESC LIMIT 20")
  fun getRecentFiles(): Flow<List<RecentFileEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertRecent(recent: RecentFileEntity)

  @Query("DELETE FROM recent_files WHERE fileId = :fileId")
  suspend fun deleteByFileId(fileId: String)

  @Query("DELETE FROM recent_files WHERE projectId = :projectId")
  suspend fun deleteByProjectId(projectId: String)
}

@Dao
interface TrashDao {
  @Query("SELECT * FROM trash_items ORDER BY trashedAt DESC")
  fun getAllTrashItems(): Flow<List<TrashItemEntity>>

  @Query("SELECT * FROM trash_items WHERE id = :id")
  suspend fun getTrashItemById(id: String): TrashItemEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrashItem(item: TrashItemEntity)

  @Query("DELETE FROM trash_items WHERE id = :id")
  suspend fun deleteTrashItemById(id: String)

  @Query("DELETE FROM trash_items")
  suspend fun emptyTrash()

  @Query("SELECT COUNT(*) FROM trash_items")
  fun getTrashCount(): Flow<Int>

  @Query("SELECT SUM(sizeBytes) FROM trash_items")
  fun getTotalTrashSize(): Flow<Long?>
}

@Dao
interface VersionHistoryDao {
  @Query("SELECT * FROM version_history WHERE fileId = :fileId ORDER BY createdAt DESC")
  fun getVersionsForFile(fileId: String): Flow<List<VersionHistoryEntity>>

  @Query("SELECT * FROM version_history WHERE id = :id")
  suspend fun getVersionById(id: String): VersionHistoryEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVersion(version: VersionHistoryEntity)

  @Query("DELETE FROM version_history WHERE fileId = :fileId")
  suspend fun deleteVersionsForFile(fileId: String)

  @Query("DELETE FROM version_history WHERE projectId = :projectId")
  suspend fun deleteVersionsForProject(projectId: String)
}

@Dao
interface BackupDao {
  @Query("SELECT * FROM backup_records ORDER BY createdAt DESC")
  fun getAllBackups(): Flow<List<BackupRecordEntity>>

  @Query("SELECT * FROM backup_records WHERE id = :id")
  suspend fun getBackupById(id: String): BackupRecordEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBackup(backup: BackupRecordEntity)

  @Query("DELETE FROM backup_records WHERE id = :id")
  suspend fun deleteBackupById(id: String)

  @Query("SELECT SUM(sizeBytes) FROM backup_records")
  fun getTotalBackupSize(): Flow<Long?>
}
