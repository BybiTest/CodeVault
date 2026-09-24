package com.example.ui.screens.starred

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FileEntity
import com.example.data.model.ProjectEntity
import com.example.ui.components.CodeVaultBottomNav
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.ProjectCardItem
import com.example.ui.screens.dashboard.formatBytes
import com.example.ui.screens.explorer.getFileColor
import com.example.ui.screens.explorer.getFileIcon
import com.example.ui.theme.StarGold

@Composable
fun StarredProjectsScreen(
  projects: List<ProjectEntity>,
  onBack: () -> Unit,
  onNavigate: (String) -> Unit,
  onToggleStar: (String) -> Unit
) {
  val strings = LocalAppStrings.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.starredProjects,
        subtitle = "${projects.size} ${strings.items}",
        actions = {
          TextButton(onClick = { onNavigate(Screen.StarredFiles.route) }) {
            Text(strings.starredFiles)
          }
        }
      )
    },
    bottomBar = {
      CodeVaultBottomNav(
        currentRoute = Screen.StarredProjects.route,
        onNavigate = onNavigate
      )
    }
  ) { paddingValues ->
    if (projects.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "هیچ پروژه‌ای هنوز ستاره‌دار نشده است",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = PaddingValues(top = 12.dp, bottom = 80.dp)
      ) {
        items(projects, key = { it.id }) { project ->
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

@Composable
fun StarredFilesScreen(
  files: List<FileEntity>,
  onBack: () -> Unit,
  onFileClick: (FileEntity) -> Unit,
  onToggleStar: (String) -> Unit
) {
  val strings = LocalAppStrings.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.starredFiles,
        subtitle = "${files.size} ${strings.items}",
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    if (files.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "هیچ فایلی هنوز ستاره‌دار نشده است",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(files, key = { it.id }) { file ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .clickable { onFileClick(file) }
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = getFileIcon(file.extension),
                contentDescription = null,
                tint = getFileColor(file.extension),
                modifier = Modifier.size(24.dp)
              )
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = file.name,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = file.relativePath,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
              }
              IconButton(onClick = { onToggleStar(file.id) }) {
                Icon(
                  Icons.Filled.Star,
                  contentDescription = null,
                  tint = StarGold
                )
              }
            }
          }
        }
      }
    }
  }
}
