package com.example.ui.screens.backup

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BackupRecordEntity
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.components.ConfirmDialog
import com.example.ui.localization.LocalAppStrings
import com.example.ui.screens.dashboard.formatBytes
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BackupScreen(
  projectId: String?,
  backups: List<BackupRecordEntity>,
  isCreatingBackup: Boolean,
  onBack: () -> Unit,
  onCreateBackup: (projectId: String?) -> Unit,
  onRestoreBackup: (backupId: String) -> Unit
) {
  val strings = LocalAppStrings.current
  var backupToRestore by remember { mutableStateOf<BackupRecordEntity?>(null) }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.backupTitle,
        subtitle = "${backups.size} ${strings.items}",
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Backup creation actions
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "ایجاد فایل پشتیبان محلی (ZIP)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Button(
              onClick = { onCreateBackup(null) },
              enabled = !isCreatingBackup,
              modifier = Modifier.weight(1f).testTag("backup_all_button")
            ) {
              if (isCreatingBackup) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
              } else {
                Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(strings.backupAllProjects)
              }
            }

            if (projectId != null) {
              OutlinedButton(
                onClick = { onCreateBackup(projectId) },
                enabled = !isCreatingBackup,
                modifier = Modifier.weight(1f).testTag("backup_project_button")
              ) {
                Text(strings.backupSingleProject)
              }
            }
          }
        }
      }

      Text(
        text = strings.backupHistory,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      if (backups.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "هنوز فایل پشتیبانی ذخیره نشده است",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(backups, key = { it.id }) { backup ->
            val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(backup.createdAt))
            Card(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
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
                  imageVector = Icons.Default.FolderZip,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = backup.projectName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "${formatBytes(backup.sizeBytes)} • ${backup.fileCount} فایل",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                  )
                }

                Button(
                  onClick = { backupToRestore = backup },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(strings.restore)
                }
              }
            }
          }
        }
      }
    }
  }

  backupToRestore?.let { b ->
    ConfirmDialog(
      title = strings.restoreBackup,
      message = "آیا می‌خواهید پروژه از فایل پشتیبان زیر بازیابی شود؟\n(${b.backupFileName})",
      confirmButtonText = strings.restore,
      dismissButtonText = strings.cancel,
      onConfirm = {
        onRestoreBackup(b.id)
        backupToRestore = null
      },
      onDismiss = { backupToRestore = null }
    )
  }
}
