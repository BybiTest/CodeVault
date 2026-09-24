package com.example.ui.navigation

sealed class Screen(val route: String) {
  data object Splash : Screen("splash")
  data object Dashboard : Screen("dashboard")
  data object Projects : Screen("projects")
  data object CreateProject : Screen("create_project")
  data object ProjectDetails : Screen("project_details/{projectId}") {
    fun createRoute(projectId: String) = "project_details/$projectId"
  }
  data object FileExplorer : Screen("file_explorer/{projectId}?folderId={folderId}") {
    fun createRoute(projectId: String, folderId: String? = null) =
      if (folderId != null) "file_explorer/$projectId?folderId=$folderId" else "file_explorer/$projectId"
  }
  data object CreateFile : Screen("create_file/{projectId}?folderId={folderId}") {
    fun createRoute(projectId: String, folderId: String? = null) =
      if (folderId != null) "create_file/$projectId?folderId=$folderId" else "create_file/$projectId"
  }
  data object CreateFolder : Screen("create_folder/{projectId}?folderId={folderId}") {
    fun createRoute(projectId: String, folderId: String? = null) =
      if (folderId != null) "create_folder/$projectId?folderId=$folderId" else "create_folder/$projectId"
  }
  data object CodeEditor : Screen("code_editor/{fileId}?targetLine={targetLine}") {
    fun createRoute(fileId: String, targetLine: Int = 1) = "code_editor/$fileId?targetLine=$targetLine"
  }
  data object Search : Screen("search?projectId={projectId}") {
    fun createRoute(projectId: String? = null) = if (projectId != null) "search?projectId=$projectId" else "search"
  }
  data object SearchResults : Screen("search_results/{query}?scope={scope}&projectId={projectId}") {
    fun createRoute(query: String, scope: String = "ALL", projectId: String? = null): String {
      val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
      return if (projectId != null) {
        "search_results/$encodedQuery?scope=$scope&projectId=$projectId"
      } else {
        "search_results/$encodedQuery?scope=$scope"
      }
    }
  }
  data object StarredFiles : Screen("starred_files")
  data object StarredProjects : Screen("starred_projects")
  data object RecentFiles : Screen("recent_files")
  data object Trash : Screen("trash")
  data object VersionHistory : Screen("version_history/{fileId}") {
    fun createRoute(fileId: String) = "version_history/$fileId"
  }
  data object Backup : Screen("backup?projectId={projectId}") {
    fun createRoute(projectId: String? = null) = if (projectId != null) "backup?projectId=$projectId" else "backup"
  }
  data object ImportProject : Screen("import_project")
  data object ExportProject : Screen("export_project/{projectId}") {
    fun createRoute(projectId: String) = "export_project/$projectId"
  }
  data object Storage : Screen("storage")
  data object Settings : Screen("settings")
  data object Appearance : Screen("settings_appearance")
  data object Language : Screen("settings_language")
  data object EditorSettings : Screen("settings_editor")
  data object Vip : Screen("vip")
  data object VipPurchase : Screen("vip_purchase")
  data object RestorePurchase : Screen("restore_purchase")
  data object About : Screen("about")
  data object PrivacyPolicy : Screen("privacy_policy")
  data object TermsOfService : Screen("terms_of_service")
}
