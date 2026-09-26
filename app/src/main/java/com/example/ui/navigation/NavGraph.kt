package com.example.ui.navigation

import android.app.Activity
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.ui.screens.about.AboutScreen
import com.example.ui.screens.about.PrivacyPolicyScreen
import com.example.ui.screens.about.TermsOfServiceScreen
import com.example.ui.screens.backup.BackupScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.editor.CodeEditorScreen
import com.example.ui.screens.explorer.CreateFileScreen
import com.example.ui.screens.explorer.CreateFolderScreen
import com.example.ui.screens.explorer.FileExplorerScreen
import com.example.ui.screens.importexport.ExportProjectScreen
import com.example.ui.screens.importexport.ImportProjectScreen
import com.example.ui.screens.projects.CreateProjectScreen
import com.example.ui.screens.projects.ProjectDetailsScreen
import com.example.ui.screens.projects.ProjectsScreen
import com.example.ui.screens.recent.RecentFilesScreen
import com.example.ui.screens.search.SearchScreen
import com.example.ui.screens.search.SearchResultsScreen
import com.example.ui.screens.settings.AppearanceScreen
import com.example.ui.screens.settings.EditorSettingsScreen
import com.example.ui.screens.settings.LanguageScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.splash.SplashScreen
import com.example.ui.screens.starred.StarredFilesScreen
import com.example.ui.screens.starred.StarredProjectsScreen
import com.example.ui.screens.storage.StorageScreen
import com.example.ui.screens.trash.TrashScreen
import com.example.ui.screens.version.VersionHistoryScreen
import com.example.ui.screens.vip.RestorePurchaseScreen
import com.example.ui.screens.vip.VipPurchaseScreen
import com.example.ui.screens.vip.VipScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CodeVaultNavGraph(
  navController: NavHostController,
  viewModel: MainViewModel
) {
  val context = LocalContext.current
  val activity = context as? Activity

  val allProjects by viewModel.allProjects.collectAsStateWithLifecycle()
  val starredProjects by viewModel.starredProjects.collectAsStateWithLifecycle()
  val starredFiles by viewModel.starredFiles.collectAsStateWithLifecycle()
  val trashItems by viewModel.trashItems.collectAsStateWithLifecycle()
  val allBackups by viewModel.allBackups.collectAsStateWithLifecycle()
  val recentFiles by viewModel.recentFilesWithEntity.collectAsStateWithLifecycle()
  val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
  val currentLanguage by viewModel.language.collectAsStateWithLifecycle()
  val isVip by viewModel.isVipActive.collectAsStateWithLifecycle()
  val fontSize by viewModel.editorFontSize.collectAsStateWithLifecycle()
  val lineNumbers by viewModel.lineNumbersEnabled.collectAsStateWithLifecycle()
  val wordWrap by viewModel.wordWrapEnabled.collectAsStateWithLifecycle()
  val tabSize by viewModel.tabSize.collectAsStateWithLifecycle()
  val syntaxHighlighting by viewModel.syntaxHighlightingEnabled.collectAsStateWithLifecycle()
  val autoSave by viewModel.autoSaveEnabled.collectAsStateWithLifecycle()
  val storageStats by viewModel.storageStats.collectAsStateWithLifecycle()
  val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
  val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()
  val billingResult by viewModel.billingResult.collectAsStateWithLifecycle()

  NavHost(
    navController = navController,
    startDestination = Screen.Splash.route
  ) {
    // 1. Splash Screen
    composable(Screen.Splash.route) {
      SplashScreen(
        onSplashFinished = {
          navController.navigate(Screen.Dashboard.route) {
            popUpTo(Screen.Splash.route) { inclusive = true }
          }
        }
      )
    }

    // 2. Dashboard Screen
    composable(Screen.Dashboard.route) {
      DashboardScreen(
        projects = allProjects,
        starredProjects = starredProjects,
        isVip = isVip,
        onNavigate = { route -> navController.navigate(route) },
        onToggleStar = { id -> viewModel.toggleStarProject(id) }
      )
    }

    // 3. Projects Screen
    composable(Screen.Projects.route) {
      ProjectsScreen(
        projects = allProjects,
        onNavigate = { route -> navController.navigate(route) },
        onToggleStar = { id -> viewModel.toggleStarProject(id) },
        onDeleteProject = { id -> viewModel.deleteProject(id) },
        isVip = isVip
      )
    }

    // 4. Create Project Screen
    composable(Screen.CreateProject.route) {
      CreateProjectScreen(
        onBack = { navController.popBackStack() },
        onCreateProject = { name, desc, template ->
          viewModel.createProject(name, desc, template) { created ->
            navController.navigate(Screen.FileExplorer.createRoute(created.id)) {
              popUpTo(Screen.CreateProject.route) { inclusive = true }
            }
          }
        }
      )
    }

    // 5. Project Details Screen
    composable(
      route = Screen.ProjectDetails.route,
      arguments = listOf(navArgument("projectId") { type = NavType.StringType })
    ) { backStackEntry ->
      val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
      val project by viewModel.observeProject(projectId).collectAsStateWithLifecycle(initialValue = null)

      ProjectDetailsScreen(
        project = project,
        onBack = { navController.popBackStack() },
        onNavigate = { route -> navController.navigate(route) },
        onToggleStar = { id -> viewModel.toggleStarProject(id) },
        onDeleteProject = { id -> viewModel.deleteProject(id) }
      )
    }

    // 6. File Explorer Screen
    composable(
      route = Screen.FileExplorer.route,
      arguments = listOf(
        navArgument("projectId") { type = NavType.StringType },
        navArgument("folderId") {
          type = NavType.StringType
          nullable = true
          defaultValue = null
        }
      )
    ) { backStackEntry ->
      val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
      val folderId = backStackEntry.arguments?.getString("folderId")
      val project by viewModel.observeProject(projectId).collectAsStateWithLifecycle(initialValue = null)
      val folders by viewModel.getFoldersInParent(projectId, folderId).collectAsStateWithLifecycle(initialValue = emptyList())
      val files by viewModel.getFilesInFolder(projectId, folderId).collectAsStateWithLifecycle(initialValue = emptyList())
      val currentFolder = remember(folders, folderId) { folders.firstOrNull { it.id == folderId } }

      FileExplorerScreen(
        project = project,
        currentFolder = currentFolder,
        folders = folders,
        files = files,
        onBack = { navController.popBackStack() },
        onNavigate = { route -> navController.navigate(route) },
        onFolderClick = { folder ->
          navController.navigate(Screen.FileExplorer.createRoute(projectId, folder.id))
        },
        onFileClick = { file ->
          viewModel.recordRecentAccess(file.id, file.projectId)
          navController.navigate(Screen.CodeEditor.createRoute(file.id))
        },
        onToggleStarFile = { id -> viewModel.toggleStarFile(id) },
        onDeleteFile = { id -> viewModel.deleteFile(id) },
        onDeleteFolder = { id -> viewModel.deleteFolder(id) },
        onDuplicateFile = { id -> viewModel.duplicateFile(id) },
        onRenameFile = { id, name -> viewModel.renameFile(id, name) },
        onRenameFolder = { id, name -> viewModel.renameFolder(id, name) },
        isVip = isVip
      )
    }

    // 7. Create File Screen
    composable(
      route = Screen.CreateFile.route,
      arguments = listOf(
        navArgument("projectId") { type = NavType.StringType },
        navArgument("folderId") {
          type = NavType.StringType
          nullable = true
          defaultValue = null
        }
      )
    ) { backStackEntry ->
      val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
      val folderId = backStackEntry.arguments?.getString("folderId")

      CreateFileScreen(
        projectId = projectId,
        folderId = folderId,
        onBack = { navController.popBackStack() },
        onCreateFile = { name, content ->
          viewModel.createFile(projectId, folderId, name, content) { createdFile ->
            navController.navigate(Screen.CodeEditor.createRoute(createdFile.id)) {
              popUpTo(Screen.CreateFile.route) { inclusive = true }
            }
          }
        }
      )
    }

    // 8. Create Folder Screen
    composable(
      route = Screen.CreateFolder.route,
      arguments = listOf(
        navArgument("projectId") { type = NavType.StringType },
        navArgument("folderId") {
          type = NavType.StringType
          nullable = true
          defaultValue = null
        }
      )
    ) { backStackEntry ->
      val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
      val folderId = backStackEntry.arguments?.getString("folderId")

      CreateFolderScreen(
        projectId = projectId,
        folderId = folderId,
        onBack = { navController.popBackStack() },
        onCreateFolder = { name ->
          viewModel.createFolder(projectId, folderId, name) {
            navController.popBackStack()
          }
        }
      )
    }

    // 9. Code Editor Screen
    composable(
      route = Screen.CodeEditor.route,
      arguments = listOf(
        navArgument("fileId") { type = NavType.StringType },
        navArgument("targetLine") {
          type = NavType.IntType
          defaultValue = 1
        }
      )
    ) { backStackEntry ->
      val fileId = backStackEntry.arguments?.getString("fileId") ?: ""
      val targetLine = backStackEntry.arguments?.getInt("targetLine") ?: 1
      val file by viewModel.observeFile(fileId).collectAsStateWithLifecycle(initialValue = null)
      var fileContent by remember { mutableStateOf<String?>(null) }

      LaunchedEffect(fileId) {
        fileContent = viewModel.readFileContent(fileId)
      }

      if (fileContent != null) {
        CodeEditorScreen(
          file = file,
          initialContent = fileContent!!,
          targetLine = targetLine,
          fontSizeSp = fontSize,
          lineNumbersEnabled = lineNumbers,
          wordWrapEnabled = wordWrap,
          syntaxHighlighting = syntaxHighlighting,
          autoSaveEnabled = autoSave,
          onBack = { navController.popBackStack() },
          onNavigate = { route -> navController.navigate(route) },
          onSaveFile = { id, content, snapshot ->
            viewModel.saveFileContent(id, content, snapshot)
          }
        )
      }
    }

    // 10. Search Screen
    composable(
      route = Screen.Search.route,
      arguments = listOf(
        navArgument("projectId") {
          type = NavType.StringType
          nullable = true
          defaultValue = null
        }
      )
    ) { backStackEntry ->
      val projectId = backStackEntry.arguments?.getString("projectId")
      SearchScreen(
        projectId = projectId,
        onBack = { navController.popBackStack() },
        onPerformSearch = { query, scope, pId ->
          viewModel.performSearch(query, scope, pId)
          navController.navigate(Screen.SearchResults.createRoute(query, scope, pId))
        }
      )
    }

    // 11. Search Results Screen
    composable(
      route = Screen.SearchResults.route,
      arguments = listOf(
        navArgument("query") { type = NavType.StringType },
        navArgument("scope") {
          type = NavType.StringType
          defaultValue = "ALL"
        },
        navArgument("projectId") {
          type = NavType.StringType
          nullable = true
          defaultValue = null
        }
      )
    ) { backStackEntry ->
      val rawQuery = backStackEntry.arguments?.getString("query") ?: ""
      val query = java.net.URLDecoder.decode(rawQuery, "UTF-8")

      SearchResultsScreen(
        query = query,
        results = searchResults,
        isLoading = isSearching,
        onBack = { navController.popBackStack() },
        onResultClick = { item ->
          viewModel.recordRecentAccess(item.fileId, item.projectId)
          navController.navigate(Screen.CodeEditor.createRoute(item.fileId, item.lineNumber))
        },
        isVip = isVip,
        onNavigate = { route -> navController.navigate(route) }
      )
    }

    // 12. Starred Files Screen
    composable(Screen.StarredFiles.route) {
      StarredFilesScreen(
        files = starredFiles,
        onBack = { navController.popBackStack() },
        onFileClick = { file ->
          viewModel.recordRecentAccess(file.id, file.projectId)
          navController.navigate(Screen.CodeEditor.createRoute(file.id))
        },
        onToggleStar = { id -> viewModel.toggleStarFile(id) },
        isVip = isVip,
        onNavigate = { route -> navController.navigate(route) }
      )
    }

    // 13. Starred Projects Screen
    composable(Screen.StarredProjects.route) {
      StarredProjectsScreen(
        projects = starredProjects,
        onBack = { navController.popBackStack() },
        onNavigate = { route -> navController.navigate(route) },
        onToggleStar = { id -> viewModel.toggleStarProject(id) },
        isVip = isVip
      )
    }

    // 14. Recent Files Screen
    composable(Screen.RecentFiles.route) {
      RecentFilesScreen(
        recentFiles = recentFiles,
        onNavigate = { route -> navController.navigate(route) },
        onFileClick = { file ->
          viewModel.recordRecentAccess(file.id, file.projectId)
          navController.navigate(Screen.CodeEditor.createRoute(file.id))
        },
        isVip = isVip
      )
    }

    // 15. Trash Screen
    composable(Screen.Trash.route) {
      TrashScreen(
        trashItems = trashItems,
        onBack = { navController.popBackStack() },
        onRestoreItem = { id -> viewModel.restoreTrashItem(id) },
        onDeletePermanently = { id -> viewModel.deleteTrashPermanently(id) },
        onEmptyTrash = { viewModel.emptyTrash() }
      )
    }

    // 16. Version History Screen
    composable(
      route = Screen.VersionHistory.route,
      arguments = listOf(navArgument("fileId") { type = NavType.StringType })
    ) { backStackEntry ->
      val fileId = backStackEntry.arguments?.getString("fileId") ?: ""
      val file by viewModel.observeFile(fileId).collectAsStateWithLifecycle(initialValue = null)
      val versions by viewModel.getVersionHistory(fileId).collectAsStateWithLifecycle(initialValue = emptyList())

      VersionHistoryScreen(
        file = file,
        versions = versions,
        onBack = { navController.popBackStack() },
        onRestoreVersion = { verId ->
          viewModel.restoreVersion(verId) {
            navController.popBackStack()
          }
        }
      )
    }

    // 17. Backup Screen
    composable(
      route = Screen.Backup.route,
      arguments = listOf(
        navArgument("projectId") {
          type = NavType.StringType
          nullable = true
          defaultValue = null
        }
      )
    ) { backStackEntry ->
      val projectId = backStackEntry.arguments?.getString("projectId")
      var isCreating by remember { mutableStateOf(false) }

      BackupScreen(
        projectId = projectId,
        backups = allBackups,
        isCreatingBackup = isCreating,
        onBack = { navController.popBackStack() },
        onCreateBackup = { pId ->
          isCreating = true
          viewModel.createBackup(pId) { isCreating = false }
        },
        onRestoreBackup = { bId ->
          viewModel.restoreBackup(bId) {}
        }
      )
    }

    // 18. Import Project Screen
    composable(Screen.ImportProject.route) {
      ImportProjectScreen(
        onBack = { navController.popBackStack() },
        onImportFromStream = { name, stream ->
          viewModel.importProjectFromZip(stream, name) { imported ->
            navController.navigate(Screen.FileExplorer.createRoute(imported.id)) {
              popUpTo(Screen.ImportProject.route) { inclusive = true }
            }
          }
        }
      )
    }

    // 19. Export Project Screen
    composable(
      route = Screen.ExportProject.route,
      arguments = listOf(navArgument("projectId") { type = NavType.StringType })
    ) { backStackEntry ->
      val projectId = backStackEntry.arguments?.getString("projectId") ?: ""
      val project by viewModel.observeProject(projectId).collectAsStateWithLifecycle(initialValue = null)

      ExportProjectScreen(
        project = project,
        onBack = { navController.popBackStack() },
        onExportZip = { dest, onComplete ->
          viewModel.exportProjectZip(projectId, dest, onComplete)
        }
      )
    }

    // 20. Storage Screen
    composable(Screen.Storage.route) {
      StorageScreen(
        stats = storageStats,
        onBack = { navController.popBackStack() },
        onClearCache = { viewModel.clearCache() }
      )
    }

    // 21. Settings Screen
    composable(Screen.Settings.route) {
      SettingsScreen(
        currentTheme = currentTheme,
        currentLanguage = currentLanguage,
        isVip = isVip,
        onNavigate = { route -> navController.navigate(route) }
      )
    }

    // 22. Appearance Screen
    composable(Screen.Appearance.route) {
      AppearanceScreen(
        currentTheme = currentTheme,
        onThemeSelect = { mode -> viewModel.setTheme(mode) },
        onBack = { navController.popBackStack() }
      )
    }

    // 23. Language Screen
    composable(Screen.Language.route) {
      LanguageScreen(
        currentLanguage = currentLanguage,
        onLanguageSelect = { lang -> viewModel.setLanguage(lang) },
        onBack = { navController.popBackStack() }
      )
    }

    // 24. Editor Settings Screen
    composable(Screen.EditorSettings.route) {
      EditorSettingsScreen(
        fontSize = fontSize,
        lineNumbers = lineNumbers,
        wordWrap = wordWrap,
        tabSize = tabSize,
        syntaxHighlighting = syntaxHighlighting,
        autoSave = autoSave,
        onUpdateFontSize = { viewModel.setEditorFontSize(it) },
        onToggleLineNumbers = { viewModel.setLineNumbers(it) },
        onToggleWordWrap = { viewModel.setWordWrap(it) },
        onUpdateTabSize = { viewModel.setTabSize(it) },
        onToggleSyntax = { viewModel.setSyntaxHighlighting(it) },
        onToggleAutoSave = { viewModel.setAutoSave(it) },
        onBack = { navController.popBackStack() }
      )
    }

    // 25. VIP Screen
    composable(Screen.Vip.route) {
      VipScreen(
        isVip = isVip,
        onBack = { navController.popBackStack() },
        onNavigate = { route -> navController.navigate(route) }
      )
    }

    // 26. VIP Purchase Screen
    composable(Screen.VipPurchase.route) {
      VipPurchaseScreen(
        plans = viewModel.availablePlans,
        billingResult = billingResult,
        onPurchasePlan = { planId ->
          if (activity != null) {
            viewModel.purchaseVipPlan(activity, planId)
          }
        },
        onBack = { navController.popBackStack() }
      )
    }

    // 27. Restore Purchase Screen
    composable(Screen.RestorePurchase.route) {
      RestorePurchaseScreen(
        billingResult = billingResult,
        onRestore = {
          if (activity != null) {
            viewModel.restoreVipPurchases(activity)
          }
        },
        onBack = { navController.popBackStack() }
      )
    }

    // 28. About Screen
    composable(Screen.About.route) {
      AboutScreen(onBack = { navController.popBackStack() })
    }

    // 29. Privacy Policy Screen
    composable(Screen.PrivacyPolicy.route) {
      PrivacyPolicyScreen(onBack = { navController.popBackStack() })
    }

    // 30. Terms of Service Screen
    composable(Screen.TermsOfService.route) {
      TermsOfServiceScreen(onBack = { navController.popBackStack() })
    }
  }
}
