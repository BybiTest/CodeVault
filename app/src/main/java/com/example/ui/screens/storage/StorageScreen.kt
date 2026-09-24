package com.example.ui.screens.storage

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.repository.StorageStats
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppStrings
import com.example.ui.screens.dashboard.formatBytes

@Composable
fun StorageScreen(
  stats: StorageStats,
  onBack: () -> Unit,
  onClearCache: () -> Long
) {
  val strings = LocalAppStrings.current
  val context = LocalContext.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.storageTitle,
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(20.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "وضعیت حافظه محلی CodeVault",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      val totalVaultSize = stats.projectsSizeBytes + stats.trashSizeBytes + stats.backupSizeBytes

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
          Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(40.dp))
          Spacer(modifier = Modifier.height(10.dp))
          Text("مجموع حافظه مصرفی مخزن", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
          Text(formatBytes(totalVaultSize), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        }
      }

      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          StorageRow(label = strings.totalProjects, count = "${stats.projectCount} پروژه", size = formatBytes(stats.projectsSizeBytes))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          StorageRow(label = strings.totalFiles, count = "${stats.fileCount} فایل", size = "")
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          StorageRow(label = strings.totalFolders, count = "${stats.folderCount} پوشه", size = "")
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          StorageRow(label = strings.trashSize, count = "", size = formatBytes(stats.trashSizeBytes))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          StorageRow(label = strings.backupsSize, count = "", size = formatBytes(stats.backupSizeBytes))
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      OutlinedButton(
        onClick = {
          val cleared = onClearCache()
          Toast.makeText(context, "${strings.cacheCleared} (${formatBytes(cleared)})", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.CleaningServices, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(strings.clearCache)
      }
    }
  }
}

@Composable
private fun StorageRow(label: String, count: String, size: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (count.isNotEmpty()) {
        Text(text = count, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (size.isNotEmpty()) Spacer(modifier = Modifier.width(8.dp))
      }
      if (size.isNotEmpty()) {
        Text(text = size, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
      }
    }
  }
}
