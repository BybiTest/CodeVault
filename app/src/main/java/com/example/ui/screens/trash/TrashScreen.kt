package com.example.ui.screens.trash

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.TrashItemEntity
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.components.ConfirmDialog
import com.example.ui.localization.LocalAppStrings
import com.example.ui.screens.dashboard.formatBytes
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TrashScreen(
  trashItems: List<TrashItemEntity>,
  onBack: () -> Unit,
  onRestoreItem: (String) -> Unit,
  onDeletePermanently: (String) -> Unit,
  onEmptyTrash: () -> Unit
) {
  val strings = LocalAppStrings.current
  var itemToDeletePermanently by remember { mutableStateOf<TrashItemEntity?>(null) }
  var showEmptyTrashDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.trashTitle,
        subtitle = "${trashItems.size} ${strings.items}",
        onBackClick = onBack,
        actions = {
          if (trashItems.isNotEmpty()) {
            TextButton(
              onClick = { showEmptyTrashDialog = true },
              colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
              Text(strings.emptyTrash, fontWeight = FontWeight.Bold)
            }
          }
        }
      )
    }
  ) { paddingValues ->
    if (trashItems.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(64.dp)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = strings.trashEmpty,
            style = MaterialTheme.typography.titleMedium,
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
        items(trashItems, key = { it.id }) { item ->
          val dateFormatted = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(item.trashedAt))
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
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                  ) {
                    Text(
                      text = item.originalType,
                      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                  text = "${strings.trashedOn} $dateFormatted",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.sizeBytes > 0) {
                  Text(
                    text = formatBytes(item.sizeBytes),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = { onRestoreItem(item.id) }) {
                  Icon(
                    Icons.Default.Restore,
                    contentDescription = strings.restoreItem,
                    tint = MaterialTheme.colorScheme.primary
                  )
                }
                IconButton(onClick = { itemToDeletePermanently = item }) {
                  Icon(
                    Icons.Default.DeleteForever,
                    contentDescription = strings.deletePermanently,
                    tint = MaterialTheme.colorScheme.error
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  // Delete single item permanently confirm
  itemToDeletePermanently?.let { item ->
    ConfirmDialog(
      title = strings.deletePermanently,
      message = "${strings.deletePermanentConfirm}\n(${item.name})",
      confirmButtonText = strings.delete,
      dismissButtonText = strings.cancel,
      isDestructive = true,
      onConfirm = {
        onDeletePermanently(item.id)
        itemToDeletePermanently = null
      },
      onDismiss = { itemToDeletePermanently = null }
    )
  }

  // Empty entire trash confirm
  if (showEmptyTrashDialog) {
    ConfirmDialog(
      title = strings.emptyTrash,
      message = strings.emptyTrashConfirm,
      confirmButtonText = strings.emptyTrash,
      dismissButtonText = strings.cancel,
      isDestructive = true,
      onConfirm = {
        showEmptyTrashDialog = false
        onEmptyTrash()
      },
      onDismiss = { showEmptyTrashDialog = false }
    )
  }
}
