package com.example.ui.screens.explorer

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.FileEntity
import com.example.data.model.FolderEntity
import com.example.data.model.ProjectEntity
import com.example.ui.components.AdBannerView
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.components.ConfirmDialog
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.screens.dashboard.formatBytes
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
  project: ProjectEntity?,
  currentFolder: FolderEntity?,
  folders: List<FolderEntity>,
  files: List<FileEntity>,
  onBack: () -> Unit,
  onNavigate: (String) -> Unit,
  onFolderClick: (FolderEntity) -> Unit,
  onFileClick: (FileEntity) -> Unit,
  onToggleStarFile: (String) -> Unit,
  onDeleteFile: (String) -> Unit,
  onDeleteFolder: (String) -> Unit,
  onDuplicateFile: (String) -> Unit,
  onRenameFile: (fileId: String, newName: String) -> Unit,
  onRenameFolder: (folderId: String, newName: String) -> Unit,
  isVip: Boolean = false
) {
  val strings = LocalAppStrings.current

  var itemToDelete by remember { mutableStateOf<Pair<String, String>?>(null) }
  var itemToRename by remember { mutableStateOf<Triple<String, String, String>?>(null) }
  var renameInput by remember { mutableStateOf("") }

  val title = currentFolder?.name ?: (project?.name ?: strings.fileExplorer)
  val pathDisplay = currentFolder?.path ?: "/"

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = title,
        subtitle = pathDisplay,
        onBackClick = onBack,
        actions = {
          IconButton(onClick = {
            val pId = project?.id ?: return@IconButton
            onNavigate(Screen.CreateFolder.createRoute(pId, currentFolder?.id))
          }) {
            Icon(Icons.Default.CreateNewFolder, contentDescription = strings.newFolder)
          }
          IconButton(onClick = {
            val pId = project?.id ?: return@IconButton
            onNavigate(Screen.CreateFile.createRoute(pId, currentFolder?.id))
          }) {
            Icon(Icons.Default.NoteAdd, contentDescription = strings.newFile)
          }
        }
      )
    },
    floatingActionButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ExtendedFloatingActionButton(
          onClick = {
            val pId = project?.id ?: return@ExtendedFloatingActionButton
            onNavigate(Screen.CreateFolder.createRoute(pId, currentFolder?.id))
          },
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
          icon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
          text = { Text(strings.newFolder) },
          modifier = Modifier.testTag("explorer_fab_new_folder")
        )

        ExtendedFloatingActionButton(
          onClick = {
            val pId = project?.id ?: return@ExtendedFloatingActionButton
            onNavigate(Screen.CreateFile.createRoute(pId, currentFolder?.id))
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          icon = { Icon(Icons.Default.NoteAdd, contentDescription = null) },
          text = { Text(strings.newFile) },
          modifier = Modifier.testTag("explorer_fab_new_file")
        )
      }
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Path breadcrumb banner
      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${project?.name ?: ""} / $pathDisplay",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      if (folders.isEmpty() && files.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
              text = strings.emptyFolder,
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "برای ایجاد فایل یا پوشه از دکمه‌های پایین استفاده کنید",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
        ) {
          // Folders section
          items(folders, key = { "folder_${it.id}" }) { folder ->
            FolderItemRow(
              folder = folder,
              onClick = { onFolderClick(folder) },
              onRename = {
                itemToRename = Triple("FOLDER", folder.id, folder.name)
                renameInput = folder.name
              },
              onDelete = { itemToDelete = Pair("FOLDER", folder.id) }
            )
          }

          // Files section
          items(files, key = { "file_${it.id}" }) { file ->
            FileItemRow(
              file = file,
              onClick = { onFileClick(file) },
              onToggleStar = { onToggleStarFile(file.id) },
              onDuplicate = { onDuplicateFile(file.id) },
              onRename = {
                itemToRename = Triple("FILE", file.id, file.name)
                renameInput = file.name
              },
              onDelete = { itemToDelete = Pair("FILE", file.id) }
            )
          }

          // ⬇️ تبلیغ بنر استاندارد (پایین لیست)
          item {
            Spacer(modifier = Modifier.height(12.dp))
            AdBannerView(
              isVip = isVip,
              onUpgradeClick = { onNavigate(Screen.VipPurchase.route) }
            )
          }
        }
      }
    }
  }

  // Delete confirm dialog
  itemToDelete?.let { (type, id) ->
    ConfirmDialog(
      title = strings.delete,
      message = strings.deleteItemConfirm,
      confirmButtonText = strings.delete,
      dismissButtonText = strings.cancel,
      isDestructive = true,
      onConfirm = {
        if (type == "FOLDER") onDeleteFolder(id) else onDeleteFile(id)
        itemToDelete = null
      },
      onDismiss = { itemToDelete = null }
    )
  }

  // Rename dialog
  itemToRename?.let { (type, id, _) ->
    AlertDialog(
      onDismissRequest = { itemToRename = null },
      title = { Text(strings.renameItem) },
      text = {
        OutlinedTextField(
          value = renameInput,
          onValueChange = { renameInput = it },
          label = { Text(if (type == "FOLDER") strings.folderName else strings.fileName) },
          singleLine = true
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (renameInput.isNotBlank()) {
              if (type == "FOLDER") onRenameFolder(id, renameInput.trim()) else onRenameFile(id, renameInput.trim())
              itemToRename = null
            }
          }
        ) {
          Text(strings.confirm)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { itemToRename = null }) {
          Text(strings.cancel)
        }
      }
    )
  }
}

@Composable
private fun FolderItemRow(
  folder: FolderEntity,
  onClick: () -> Unit,
  onRename: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp)
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() },
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Folder,
        contentDescription = null,
        tint = WarningAmber,
        modifier = Modifier.size(28.dp)
      )

      Spacer(modifier = Modifier.width(12.dp))

      Text(
        text = folder.name,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.weight(1f),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      Box {
        IconButton(onClick = { showMenu = true }) {
          Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("تغییر نام") },
            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
            onClick = {
              showMenu = false
              onRename()
            }
          )
          DropdownMenuItem(
            text = { Text("انتقال به سطل زباله") },
            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            onClick = {
              showMenu = false
              onDelete()
            }
          )
        }
      }
    }
  }
}

@Composable
private fun FileItemRow(
  file: FileEntity,
  onClick: () -> Unit,
  onToggleStar: () -> Unit,
  onDuplicate: () -> Unit,
  onRename: () -> Unit,
  onDelete: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }
  val icon = getFileIcon(file.extension)
  val iconTint = getFileColor(file.extension)

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 14.dp, vertical = 4.dp)
      .clip(RoundedCornerShape(10.dp))
      .clickable { onClick() },
    color = MaterialTheme.colorScheme.surface
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(26.dp)
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = file.name,
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = formatBytes(file.sizeBytes),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = " • ",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = file.language.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = iconTint
          )
        }
      }

      IconButton(onClick = onToggleStar) {
        Icon(
          imageVector = if (file.isStarred) Icons.Filled.Star else Icons.Outlined.StarBorder,
          contentDescription = null,
          tint = if (file.isStarred) StarGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
          modifier = Modifier.size(20.dp)
        )
      }

      Box {
        IconButton(onClick = { showMenu = true }) {
          Icon(Icons.Default.MoreVert, contentDescription = null)
        }
        DropdownMenu(
          expanded = showMenu,
          onDismissRequest = { showMenu = false }
        ) {
          DropdownMenuItem(
            text = { Text("ویرایش کد") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            onClick = {
              showMenu = false
              onClick()
            }
          )
          DropdownMenuItem(
            text = { Text("تکثیر فایل (Duplicate)") },
            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
            onClick = {
              showMenu = false
              onDuplicate()
            }
          )
          DropdownMenuItem(
            text = { Text("تغییر نام") },
            leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
            onClick = {
              showMenu = false
              onRename()
            }
          )
          DropdownMenuItem(
            text = { Text("انتقال به سطل زباله") },
            leadingIcon = { Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            onClick = {
              showMenu = false
              onDelete()
            }
          )
        }
      }
    }
  }
}

fun getFileIcon(ext: String): ImageVector {
  return when (ext.lowercase()) {
    "kt", "kts", "java", "py", "js", "ts", "cpp", "c", "go", "rs" -> Icons.Default.Code
    "html", "htm", "xml" -> Icons.Default.Html
    "css", "scss" -> Icons.Default.Css
    "json", "yaml", "yml", "toml" -> Icons.Default.DataObject
    "md", "txt" -> Icons.Default.Description
    "png", "jpg", "jpeg", "webp", "svg" -> Icons.Default.Image
    else -> Icons.Default.InsertDriveFile
  }
}

fun getFileColor(ext: String): Color {
  return when (ext.lowercase()) {
    "kt", "kts" -> MetallicCyanPrimary
    "py" -> WarningAmber
    "js", "ts" -> StarGold
    "html", "xml" -> Color(0xFFE44D26)
    "css" -> Color(0xFF264DE4)
    "json" -> SuccessGreen
    "md" -> MetallicSilver
    else -> MetallicCyanPrimary
  }
}
