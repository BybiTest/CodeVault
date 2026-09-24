package com.example.ui.screens.projects

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ProjectEntity
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.components.ConfirmDialog
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.formatBytes
import com.example.ui.theme.StarGold
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProjectDetailsScreen(
  project: ProjectEntity?,
  onBack: () -> Unit,
  onNavigate: (String) -> Unit,
  onToggleStar: (String) -> Unit,
  onDeleteProject: (String) -> Unit
) {
  val strings = LocalAppStrings.current
  var showDeleteDialog by remember { mutableStateOf(false) }

  if (project == null) {
    Scaffold(
      topBar = { CodeVaultTopBar(title = strings.projectDetails, onBackClick = onBack) }
    ) { p ->
      Box(modifier = Modifier.fillMaxSize().padding(p), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
    }
    return
  }

  val dateUpdated = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(project.updatedAt))
  val dateCreated = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(project.createdAt))

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = project.name,
        subtitle = project.primaryLanguage,
        onBackClick = onBack,
        actions = {
          IconButton(onClick = { onToggleStar(project.id) }) {
            Icon(
              imageVector = if (project.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
              contentDescription = strings.star,
              tint = if (project.isStarred) StarGold else MaterialTheme.colorScheme.onSurface
            )
          }
          IconButton(onClick = { onNavigate(Screen.Search.createRoute(project.id)) }) {
            Icon(Icons.Default.Search, contentDescription = strings.search)
          }
        }
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(20.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // Primary Action Card: Open File Explorer
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = project.name,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
          if (project.description.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = project.description,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
          Button(
            onClick = { onNavigate(Screen.FileExplorer.createRoute(project.id)) },
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .testTag("open_project_explorer_button"),
            shape = RoundedCornerShape(10.dp)
          ) {
            Icon(Icons.Default.Code, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = strings.openProjectExplorer,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      // Metadata Stats Card
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          DetailRow(label = "نوع پروژه / زبان:", value = project.primaryLanguage)
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          DetailRow(label = strings.totalFiles, value = "${project.fileCount} فایل")
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          DetailRow(label = strings.totalFolders, value = "${project.folderCount} پوشه")
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          DetailRow(label = "حجم کل محتوا:", value = formatBytes(project.totalSizeBytes))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          DetailRow(label = "تاریخ ایجاد:", value = dateCreated)
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          DetailRow(label = strings.lastModified, value = dateUpdated)
        }
      }

      // Actions Column
      Text(
        text = "عملیات پروژه",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      OutlinedButton(
        onClick = { onNavigate(Screen.ExportProject.createRoute(project.id)) },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.FolderZip, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(strings.exportAsZip)
      }

      OutlinedButton(
        onClick = { onNavigate(Screen.Backup.createRoute(project.id)) },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Backup, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(strings.backupSingleProject)
      }

      OutlinedButton(
        onClick = { onNavigate(Screen.Search.createRoute(project.id)) },
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.Search, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("جستجو در کدهای این پروژه")
      }

      Button(
        onClick = { showDeleteDialog = true },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        modifier = Modifier
          .fillMaxWidth()
          .height(48.dp),
        shape = RoundedCornerShape(10.dp)
      ) {
        Icon(Icons.Default.DeleteOutline, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(strings.delete)
      }
    }
  }

  if (showDeleteDialog) {
    ConfirmDialog(
      title = strings.delete,
      message = strings.deleteProjectConfirm,
      confirmButtonText = strings.delete,
      dismissButtonText = strings.cancel,
      isDestructive = true,
      onConfirm = {
        showDeleteDialog = false
        onDeleteProject(project.id)
        onBack()
      },
      onDismiss = { showDeleteDialog = false }
    )
  }
}

@Composable
private fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
