package com.example.ui.screens.explorer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppStrings

@Composable
fun CreateFolderScreen(
  projectId: String,
  folderId: String?,
  onBack: () -> Unit,
  onCreateFolder: (name: String) -> Unit
) {
  val strings = LocalAppStrings.current
  var folderName by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.newFolder,
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      Text(
        text = strings.newFolder,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      OutlinedTextField(
        value = folderName,
        onValueChange = {
          folderName = it
          if (errorMessage != null) errorMessage = null
        },
        label = { Text(strings.folderName) },
        placeholder = { Text("مثال: components یا models یا utils") },
        isError = errorMessage != null,
        supportingText = {
          if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
          }
        },
        leadingIcon = { Icon(Icons.Default.CreateNewFolder, contentDescription = null) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("create_folder_name_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = {
          if (folderName.isBlank()) {
            errorMessage = "لطفاً نام پوشه را وارد کنید"
            return@Button
          }
          isSubmitting = true
          onCreateFolder(folderName.trim())
        },
        enabled = !isSubmitting,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("submit_create_folder_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Icon(Icons.Default.CreateNewFolder, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = strings.newFolder,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}
