package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
  entities = [
    ProjectEntity::class,
    FolderEntity::class,
    FileEntity::class,
    RecentFileEntity::class,
    TrashItemEntity::class,
    VersionHistoryEntity::class,
    BackupRecordEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class CodeVaultDatabase : RoomDatabase() {
  abstract fun projectDao(): ProjectDao
  abstract fun folderDao(): FolderDao
  abstract fun fileDao(): FileDao
  abstract fun recentFileDao(): RecentFileDao
  abstract fun trashDao(): TrashDao
  abstract fun versionHistoryDao(): VersionHistoryDao
  abstract fun backupDao(): BackupDao

  companion object {
    @Volatile
    private var INSTANCE: CodeVaultDatabase? = null

    fun getDatabase(context: Context): CodeVaultDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          CodeVaultDatabase::class.java,
          "codevault_database"
        ).fallbackToDestructiveMigration().build()
        INSTANCE = instance
        instance
      }
    }
  }
}
