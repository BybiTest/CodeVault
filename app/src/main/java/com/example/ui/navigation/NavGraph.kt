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
  val recentFiles by viewModel.recentFiles
