package com.example.ui.screens.recent

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
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
import com.example.data.model.RecentFileEntity
import com.example.ui.components.CodeVaultBottomNav
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.screens.explorer.getFileColor
import com.example.ui.screens.explorer.getFileIcon
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecentFilesScreen(
  recentFiles: List<Pair<RecentFileEntity, FileEntity?>>,
  onNavigate: (String) -> Unit,
  onFileClick: (FileEntity) -> Unit
) {
  val strings = LocalAppStrings.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.recentTitle,
        subtitle = "${recentFiles.size} ${strings.items}"
      )
    },
    bottomBar = {
      CodeVaultBottomNav(
        currentRoute = Screen.RecentFiles.route,
        onNavigate = onNavigate
      )
    }
  ) { paddingValues ->
    if (recentFiles.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(56.dp)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = strings.noRecentFiles,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(recentFiles, key = { it.first.id }) { (recent, file) ->
          if (file != null) {
            val dateFormatted = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault()).format(Date(recent.accessedAt))
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
                  modifier = Modifier.size(26.dp)
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
                  Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
