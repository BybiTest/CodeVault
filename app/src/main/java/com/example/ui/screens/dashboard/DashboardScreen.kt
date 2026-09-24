package com.example.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProjectEntity
import com.example.ui.components.AdBannerView
import com.example.ui.components.CodeVaultBottomNav
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
  projects: List<ProjectEntity>,
  starredProjects: List<ProjectEntity>,
  isVip: Boolean,
  onNavigate: (String) -> Unit,
  onToggleStar: (String) -> Unit
) {
  val strings = LocalAppStrings.current
  val totalFiles = projects.sumOf { it.fileCount }
  val totalFolders = projects.sumOf { it.folderCount }
  val totalBytes = projects.sumOf { it.totalSizeBytes }

  Scaffold(
    bottomBar = {
      CodeVaultBottomNav(
        currentRoute = Screen.Dashboard.route,
        onNavigate = onNavigate
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { onNavigate(Screen.CreateProject.route) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("dashboard_fab_create_project")
      ) {
        Icon(Icons.Default.Add, contentDescription = strings.newProject)
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .testTag("dashboard_scroll_content"),
      contentPadding = PaddingValues(bottom = 80.dp)
    ) {
      // 1. Dashboard Header
      item {
        DashboardHeader(
          isVip = isVip,
          onSearchClick = { onNavigate(Screen.Search.createRoute()) },
          onVipClick = { onNavigate(Screen.Vip.route) },
          onSettingsClick = { onNavigate(Screen.Settings.route) }
        )
      }

      // 2. Storage & Vault Stats Cards
      item {
        StatsOverviewSection(
          projectCount = projects.size,
          fileCount = totalFiles,
          folderCount = totalFolders,
          totalBytes = totalBytes,
          onStorageClick = { onNavigate(Screen.Storage.route) }
        )
      }

      // 3. Quick Actions
      item {
        QuickActionsSection(
          onNewProject = { onNavigate(Screen.CreateProject.route) },
          onImportZip = { onNavigate(Screen.ImportProject.route) },
          onBackup = { onNavigate(Screen.Backup.createRoute()) },
          onTrash = { onNavigate(Screen.Trash.route) }
        )
      }

      // 4. Ad Banner (Non-VIP only)
      item {
        AdBannerView(
          isVip = isVip,
          onUpgradeClick = { onNavigate(Screen.VipPurchase.route) }
        )
      }

      // 5. Recent Projects or Empty State
      if (projects.isEmpty()) {
        item {
          EmptyProjectsCard(
            onCreateClick = { onNavigate(Screen.CreateProject.route) },
            onImportClick = { onNavigate(Screen.ImportProject.route) }
          )
        }
      } else {
        item {
          SectionHeader(
            title = strings.recentProjects,
            actionTitle = strings.viewAll,
            onActionClick = { onNavigate(Screen.Projects.route) }
          )
        }

        items(projects.take(4), key = { it.id }) { project ->
          ProjectCardItem(
            project = project,
            onClick = { onNavigate(Screen.ProjectDetails.createRoute(project.id)) },
            onToggleStar = { onToggleStar(project.id) }
          )
        }

        if (starredProjects.isNotEmpty()) {
          item {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader(
              title = strings.starredProjects,
              actionTitle = strings.viewAll,
              onActionClick = { onNavigate(Screen.StarredProjects.route) }
            )
          }

          items(starredProjects.take(3), key = { "starred_${it.id}" }) { project ->
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
}

@Composable
private fun DashboardHeader(
  isVip: Boolean,
  onSearchClick: () -> Unit,
  onVipClick: () -> Unit,
  onSettingsClick: () -> Unit
) {
  val strings = LocalAppStrings.current

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(42.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = strings.appName,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          if (isVip) {
            Spacer(modifier = Modifier.width(6.dp))
            Surface(
              color = VipGold.copy(alpha = 0.2f),
              shape = RoundedCornerShape(6.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, VipGold)
            ) {
              Text(
                text = "VIP",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = VipGold
              )
            }
          }
        }
        Text(
          text = strings.appSubtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(
        onClick = onSearchClick,
        modifier = Modifier.testTag("dashboard_search_button")
      ) {
        Icon(Icons.Default.Search, contentDescription = strings.search)
      }

      if (!isVip) {
        IconButton(
          onClick = onVipClick,
          modifier = Modifier.testTag("dashboard_vip_button")
        ) {
          Icon(Icons.Default.WorkspacePremium, contentDescription = strings.vipTitle, tint = VipGold)
        }
      }

      IconButton(
        onClick = onSettingsClick,
        modifier = Modifier.testTag("dashboard_settings_button")
      ) {
        Icon(Icons.Default.Settings, contentDescription = strings.settingsTitle)
      }
    }
  }
}

@Composable
private fun StatsOverviewSection(
  projectCount: Int,
  fileCount: Int,
  folderCount: Int,
  totalBytes: Long,
  onStorageClick: () -> Unit
) {
  val strings = LocalAppStrings.current
  val formattedSize = formatBytes(totalBytes)

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(16.dp))
      .clickable { onStorageClick() }
      .testTag("dashboard_stats_card"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = strings.storageOverview,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = formattedSize,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        StatItem(title = strings.totalProjects, value = projectCount.toString(), icon = Icons.Default.Folder)
        StatItem(title = strings.totalFiles, value = fileCount.toString(), icon = Icons.Default.InsertDriveFile)
        StatItem(title = strings.totalFolders, value = folderCount.toString(), icon = Icons.Default.CreateNewFolder)
      }
    }
  }
}

@Composable
private fun StatItem(title: String, value: String, icon: ImageVector) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shape = RoundedCornerShape(8.dp),
      modifier = Modifier.size(32.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
      }
    }
    Spacer(modifier = Modifier.width(8.dp))
    Column {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun QuickActionsSection(
  onNewProject: () -> Unit,
  onImportZip: () -> Unit,
  onBackup: () -> Unit,
  onTrash: () -> Unit
) {
  val strings = LocalAppStrings.current

  Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
    Text(
      text = strings.quickActions,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.padding(bottom = 10.dp)
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      QuickActionButton(
        icon = Icons.Default.AddCircle,
        label = strings.newProject,
        onClick = onNewProject,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        icon = Icons.Default.FolderZip,
        label = strings.importZip,
        onClick = onImportZip,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        icon = Icons.Default.Backup,
        label = strings.backupNow,
        onClick = onBackup,
        modifier = Modifier.weight(1f)
      )
      QuickActionButton(
        icon = Icons.Default.DeleteOutline,
        label = strings.trashTitle,
        onClick = onTrash,
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
private fun QuickActionButton(
  icon: ImageVector,
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
    color = MaterialTheme.colorScheme.surfaceVariant,
    shape = RoundedCornerShape(12.dp)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun ProjectCardItem(
  project: ProjectEntity,
  onClick: () -> Unit,
  onToggleStar: () -> Unit,
  modifier: Modifier = Modifier
) {
  val strings = LocalAppStrings.current
  val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(project.updatedAt))

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 5.dp)
      .clip(RoundedCornerShape(14.dp))
      .clickable { onClick() }
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(14.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.size(46.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Source,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = project.name,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "${project.fileCount} ${strings.totalFiles}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = " • ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = formatBytes(project.totalSizeBytes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = " • ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = project.primaryLanguage,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      IconButton(onClick = onToggleStar) {
        Icon(
          imageVector = if (project.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
          contentDescription = strings.star,
          tint = if (project.isStarred) StarGold else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
private fun EmptyProjectsCard(
  onCreateClick: () -> Unit,
  onImportClick: () -> Unit
) {
  val strings = LocalAppStrings.current

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .padding(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(16.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(28.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = CircleShape,
        modifier = Modifier.size(64.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(34.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = strings.noProjectsYet,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = strings.emptyStateDesc,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(20.dp))

      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
          onClick = onCreateClick,
          modifier = Modifier.testTag("empty_state_create_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(strings.createFirstProject)
        }

        OutlinedButton(onClick = onImportClick) {
          Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(strings.importZip)
        }
      }
    }
  }
}

@Composable
fun SectionHeader(
  title: String,
  actionTitle: String? = null,
  onActionClick: (() -> Unit)? = null
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(1f)
    )
    if (actionTitle != null && onActionClick != null) {
      TextButton(onClick = onActionClick) {
        Text(
          text = actionTitle,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

fun formatBytes(bytes: Long): String {
  if (bytes < 1024) return "$bytes B"
  val kb = bytes / 1024.0
  if (kb < 1024) return String.format(Locale.US, "%.1f KB", kb)
  val mb = kb / 1024.0
  return String.format(Locale.US, "%.1f MB", mb)
}
