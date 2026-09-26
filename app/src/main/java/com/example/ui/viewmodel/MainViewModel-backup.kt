package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CodeVaultApplication
import com.example.data.billing.BillingResult
import com.example.data.billing.VipPlan
import com.example.data.model.*
import com.example.data.repository.SearchResultItem
import com.example.data.repository.StorageStats
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val app = application as CodeVaultApplication
  private val projectRepo = app.projectRepository
  private val settingsRepo = app.settingsRepository
  private val billingManager = app.billingManager
  val tapsellAdManager = app.tapsellAdManager

  // State flows
  val allProjects: StateFlow<List<ProjectEntity>> = projectRepo.getAllProjects()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val starredProjects: StateFlow<List<ProjectEntity>> = projectRepo.getStarredProjects()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val recentProjects: StateFlow<List<ProjectEntity>> = projectRepo.getRecentProjects()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val starredFiles: StateFlow<List<FileEntity>> = projectRepo.getStarredFiles()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val trashItems: StateFlow<List<TrashItemEntity>> = projectRepo.getAllTrashItems()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allBackups: StateFlow<List<BackupRecordEntity>> = projectRepo.getAllBackups()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Settings
  val themeMode: StateFlow<AppThemeMode> = settingsRepo.themeMode
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.METALLIC_BLACK)

  val language: StateFlow<AppLanguage> = settingsRepo.language
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.FA)

  val editorFontSize: StateFlow<Int> = settingsRepo.editorFontSize
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 14)

  val lineNumbersEnabled: StateFlow<Boolean> = settingsRepo.lineNumbersEnabled
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  val wordWrapEnabled: StateFlow<Boolean> = settingsRepo.wordWrapEnabled
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  val tabSize: StateFlow<Int> = settingsRepo.tabSize
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4)

  val syntaxHighlightingEnabled: StateFlow<Boolean> = settingsRepo.syntaxHighlightingEnabled
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  val autoSaveEnabled: StateFlow<Boolean> = settingsRepo.autoSaveEnabled
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

  val isVipActive: StateFlow<Boolean> = settingsRepo.isVipActive
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

  // Billing
  val billingResult: StateFlow<BillingResult> = billingManager.billingResult
  val availablePlans: List<VipPlan> = billingManager.availablePlans

  // Storage Stats
  private val _storageStats = MutableStateFlow(StorageStats(0, 0, 0, 0L, 0L, 0L))
  val storageStats: StateFlow<StorageStats> = _storageStats.asStateFlow()

  // Search Results
  private val _searchResults = MutableStateFlow<List<SearchResultItem>>(emptyList())
  val searchResults: StateFlow<List<SearchResultItem>> = _searchResults.asStateFlow()

  private val _isSearching = MutableStateFlow(false)
  val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

  // Recent files populated with file entity
  val recentFilesWithEntity: StateFlow<List<Pair<RecentFileEntity, FileEntity?>>> = projectRepo.getRecentFiles()
    .map { list ->
      list.map { recent ->
        Pair(recent, projectRepo.getFile(recent.fileId))
      }
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  init {
    refreshStorageStats()
  }

  fun refreshStorageStats() {
    viewModelScope.launch(Dispatchers.IO) {
      _storageStats.value = projectRepo.getStorageStats()
    }
  }

  // Projects
  fun createProject(name: String, description: String, template: String, onCreated: (ProjectEntity) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val project = projectRepo.createProject(name, description, template)
      refreshStorageStats()
      onCreated(project)
    }
  }

  fun toggleStarProject(projectId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.toggleStarProject(projectId)
    }
  }

  fun deleteProject(projectId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.deleteProject(projectId)
      refreshStorageStats()
    }
  }

  fun observeProject(id: String) = projectRepo.observeProject(id)

  // Folders & Files in Explorer
  fun getFoldersInParent(projectId: String, parentId: String?) = projectRepo.getFoldersInParent(projectId, parentId)
  fun getFilesInFolder(projectId: String, folderId: String?) = projectRepo.getFilesInFolder(projectId, folderId)

  fun createFolder(projectId: String, parentFolderId: String?, name: String, onComplete: () -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.createFolder(projectId, parentFolderId, name)
      refreshStorageStats()
      onComplete()
    }
  }

  fun deleteFolder(folderId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.deleteFolder(folderId)
      refreshStorageStats()
    }
  }

  fun renameFolder(folderId: String, newName: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.renameFolder(folderId, newName)
    }
  }

  fun createFile(projectId: String, folderId: String?, name: String, initialContent: String, onCreated: (FileEntity) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val file = projectRepo.createFile(projectId, folderId, name, initialContent)
      refreshStorageStats()
      onCreated(file)
    }
  }

  fun deleteFile(fileId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.deleteFile(fileId)
      refreshStorageStats()
    }
  }

  fun renameFile(fileId: String, newName: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.renameFile(fileId, newName)
    }
  }

  fun duplicateFile(fileId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.duplicateFile(fileId)
      refreshStorageStats()
    }
  }

  fun toggleStarFile(fileId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.toggleStarFile(fileId)
    }
  }

  fun observeFile(fileId: String) = projectRepo.observeFile(fileId)

  suspend fun readFileContent(fileId: String): String = projectRepo.readFileContent(fileId)

  fun saveFileContent(fileId: String, content: String, createSnapshot: Boolean) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.saveFileContent(fileId, content, createSnapshot)
      refreshStorageStats()
    }
  }

  fun recordRecentAccess(fileId: String, projectId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.recordRecentFileAccess(fileId, projectId)
    }
  }

  // Version History
  fun getVersionHistory(fileId: String) = projectRepo.getVersionHistory(fileId)

  fun restoreVersion(versionId: String, onComplete: () -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.restoreVersion(versionId)
      refreshStorageStats()
      onComplete()
    }
  }

  // Trash
  fun restoreTrashItem(trashId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.restoreTrashItem(trashId)
      refreshStorageStats()
    }
  }

  fun deleteTrashPermanently(trashId: String) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.deleteTrashItemPermanently(trashId)
      refreshStorageStats()
    }
  }

  fun emptyTrash() {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.emptyTrash()
      refreshStorageStats()
    }
  }

  // Backup & Restore
  fun createBackup(projectId: String?, onComplete: () -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.createBackup(projectId)
      refreshStorageStats()
      onComplete()
    }
  }

  fun restoreBackup(backupId: String, onComplete: () -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      projectRepo.restoreBackupRecord(backupId)
      refreshStorageStats()
      onComplete()
    }
  }

  // Import / Export
  fun importProjectFromZip(stream: InputStream, name: String, onComplete: (ProjectEntity) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val project = projectRepo.importProjectFromZip(stream, name)
      refreshStorageStats()
      onComplete(project)
    }
  }

  fun exportProjectZip(projectId: String, destinationFile: File, onComplete: (File?) -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
      val success = projectRepo.exportProjectZip(projectId, destinationFile)
      onComplete(if (success) destinationFile else null)
    }
  }

  // Search
  fun performSearch(query: String, scope: String, projectId: String?) {
    viewModelScope.launch(Dispatchers.IO) {
      _isSearching.value = true
      _searchResults.value = projectRepo.search(query, scope, projectId)
      _isSearching.value = false
    }
  }

  // Storage
  fun clearCache(): Long {
    val cleared = projectRepo.clearAppCache()
    refreshStorageStats()
    return cleared
  }

  // Settings
  fun setTheme(mode: AppThemeMode) {
    viewModelScope.launch { settingsRepo.setThemeMode(mode) }
  }

  fun setLanguage(lang: AppLanguage) {
    viewModelScope.launch { settingsRepo.setLanguage(lang) }
  }

  fun setEditorFontSize(size: Int) {
    viewModelScope.launch { settingsRepo.setEditorFontSize(size) }
  }

  fun setLineNumbers(enabled: Boolean) {
    viewModelScope.launch { settingsRepo.setLineNumbersEnabled(enabled) }
  }

  fun setWordWrap(enabled: Boolean) {
    viewModelScope.launch { settingsRepo.setWordWrapEnabled(enabled) }
  }

  fun setTabSize(size: Int) {
    viewModelScope.launch { settingsRepo.setTabSize(size) }
  }

  fun setSyntaxHighlighting(enabled: Boolean) {
    viewModelScope.launch { settingsRepo.setSyntaxHighlightingEnabled(enabled) }
  }

  fun setAutoSave(enabled: Boolean) {
    viewModelScope.launch { settingsRepo.setAutoSaveEnabled(enabled) }
  }

  // Billing
  fun purchaseVipPlan(planId: String) {
    billingManager.purchasePlan(planId, language.value == AppLanguage.FA)
  }

  fun restoreVipPurchases() {
    billingManager.restorePurchases(language.value == AppLanguage.FA)
  }
}
