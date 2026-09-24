package com.example.ui.screens.explorer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppStrings

@Composable
fun CreateFileScreen(
  projectId: String,
  folderId: String?,
  onBack: () -> Unit,
  onCreateFile: (name: String, content: String) -> Unit
) {
  val strings = LocalAppStrings.current
  var fileName by remember { mutableStateOf("") }
  var initialContent by remember { mutableStateOf("") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.newFile,
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
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      Text(
        text = strings.newFile,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      OutlinedTextField(
        value = fileName,
        onValueChange = {
          fileName = it
          if (errorMessage != null) errorMessage = null
        },
        label = { Text(strings.fileName) },
        placeholder = { Text("مثال: ApiClient.kt یا utils.py") },
        isError = errorMessage != null,
        supportingText = {
          if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
          } else {
            Text("همراه با پسوند (مثال: .kt, .py, .js, .json, .html)")
          }
        },
        leadingIcon = { Icon(Icons.Default.InsertDriveFile, contentDescription = null) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("create_file_name_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      // Quick extension chips
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf(".kt", ".py", ".js", ".html", ".css", ".json", ".md").forEach { ext ->
          AssistChip(
            onClick = {
              val base = fileName.substringBeforeLast('.')
              fileName = if (base.isEmpty()) "File$ext" else "$base$ext"
            },
            label = { Text(ext) }
          )
        }
      }

      OutlinedTextField(
        value = initialContent,
        onValueChange = { initialContent = it },
        label = { Text("کد اولیه (اختیاری)") },
        placeholder = { Text("// Write your initial code here...") },
        leadingIcon = { Icon(Icons.Default.Code, contentDescription = null) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("create_file_content_input"),
        minLines = 6,
        shape = RoundedCornerShape(12.dp)
      )

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = {
          if (fileName.isBlank()) {
            errorMessage = "لطفاً نام فایل را وارد کنید"
            return@Button
          }
          isSubmitting = true
          onCreateFile(fileName.trim(), initialContent)
        },
        enabled = !isSubmitting,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("submit_create_file_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Icon(Icons.Default.NoteAdd, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = strings.newFile,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}
