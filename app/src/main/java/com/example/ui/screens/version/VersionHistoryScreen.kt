package com.example.ui.screens.version

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FileEntity
import com.example.data.model.VersionHistoryEntity
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.components.ConfirmDialog
import com.example.ui.localization.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun VersionHistoryScreen(
  file: FileEntity?,
  versions: List<VersionHistoryEntity>,
  onBack: () -> Unit,
  onRestoreVersion: (String) -> Unit
) {
  val strings = LocalAppStrings.current
  var previewVersion by remember { mutableStateOf<VersionHistoryEntity?>(null) }
  var versionToRestore by remember { mutableStateOf<VersionHistoryEntity?>(null) }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.versionHistoryTitle,
        subtitle = file?.name ?: "",
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    if (versions.isEmpty()) {
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
            text = strings.noVersions,
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
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(versions, key = { it.id }) { version ->
          val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(version.createdAt))

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = version.versionName,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = dateFormatted,
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = "${version.characterCount} ${strings.chars}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = { previewVersion = version },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(strings.previewVersion)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                  onClick = { versionToRestore = version },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(strings.restore)
                }
              }
            }
          }
        }
      }
    }
  }

  // Preview Dialog
  previewVersion?.let { ver ->
    AlertDialog(
      onDismissRequest = { previewVersion = null },
      title = { Text("${strings.previewVersion}: ${ver.versionName}") },
      text = {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 350.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
        ) {
          Text(
            text = ver.content,
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      confirmButton = {
        Button(onClick = {
          previewVersion = null
          versionToRestore = ver
        }) {
          Text(strings.restore)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { previewVersion = null }) {
          Text(strings.close)
        }
      }
    )
  }

  // Restore confirm dialog
  versionToRestore?.let { ver ->
    ConfirmDialog(
      title = strings.restoreVersion,
      message = "${strings.restoreVersionConfirm}\n(${ver.versionName})",
      confirmButtonText = strings.restore,
      dismissButtonText = strings.cancel,
      onConfirm = {
        onRestoreVersion(ver.id)
        versionToRestore = null
      },
      onDismiss = { versionToRestore = null }
    )
  }
}
