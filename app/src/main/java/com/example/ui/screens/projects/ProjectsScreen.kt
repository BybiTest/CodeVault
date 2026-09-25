package com.example.ui.screens.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.ProjectEntity
import com.example.ui.components.AdBannerView
import com.example.ui.components.CodeVaultBottomNav
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.components.ConfirmDialog
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.ProjectCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen(
  projects: List<ProjectEntity>,
  onNavigate: (String) -> Unit,
  onToggleStar: (String) -> Unit,
  onDeleteProject: (String) -> Unit,
  isVip: Boolean = false
) {
  val strings = LocalAppStrings.current
  var searchQuery by remember { mutableStateOf("") }
  var sortOption by remember { mutableStateOf("DATE") } // "DATE", "NAME", "SIZE"
  var projectToDelete by remember { mutableStateOf<ProjectEntity?>(null) }

  val filteredProjects = remember(projects, searchQuery, sortOption) {
    var list = if (searchQuery.isBlank()) {
      projects
    } else {
      projects.filter { it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true) }
    }
    list = when (sortOption) {
      "NAME" -> list.sortedBy { it.name.lowercase() }
      "SIZE" -> list.sortedByDescending { it.totalSizeBytes }
      else -> list.sortedByDescending { it.updatedAt }
    }
    list
  }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.projectsTitle,
        subtitle = "${projects.size} ${strings.items}",
        actions = {
          IconButton(onClick = { onNavigate(Screen.Search.createRoute()) }) {
            Icon(Icons.Default.Search, contentDescription = strings.search)
          }
        }
      )
    },
    bottomBar = {
      CodeVaultBottomNav(
        currentRoute = Screen.Projects.route,
        onNavigate = onNavigate
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { onNavigate(Screen.CreateProject.route) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("projects_fab_create")
      ) {
        Icon(Icons.Default.Add, contentDescription = strings.newProject)
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Search Box & Sort Chips
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text(strings.searchPlaceholder) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Default.Close, contentDescription = null)
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        shape = MaterialTheme.shapes.medium
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        FilterChip(
          selected = sortOption == "DATE",
          onClick = { sortOption = "DATE" },
          label = { Text("تاریخ") }
        )
        FilterChip(
          selected = sortOption == "NAME",
          onClick = { sortOption = "NAME" },
          label = { Text("نام") }
        )
        FilterChip(
          selected = sortOption == "SIZE",
          onClick = { sortOption = "SIZE" },
          label = { Text("حجم") }
        )
      }

      // ⬇️ تبلیغ بنر استاندارد (بالای لیست)
      AdBannerView(
        isVip = isVip,
        onUpgradeClick = { onNavigate(Screen.VipPurchase.route) }
      )

      if (filteredProjects.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
          Text(
            text = strings.noResultsFound,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)
        ) {
          items(filteredProjects, key = { it.id }) { project ->
            ProjectCardItem(
              project = project,
              onClick = { onNavigate(Screen.ProjectDetails.createRoute(project.id)) },
              onToggleStar = { onToggleStar(project.id) }
            )
          }
        }
      }
    }
  }

  projectToDelete?.let { proj ->
    ConfirmDialog(
      title = strings.delete,
      message = strings.deleteProjectConfirm,
      confirmButtonText = strings.delete,
      dismissButtonText = strings.cancel,
      isDestructive = true,
      onConfirm = {
        onDeleteProject(proj.id)
        projectToDelete = null
      },
      onDismiss = { projectToDelete = null }
    )
  }
}
