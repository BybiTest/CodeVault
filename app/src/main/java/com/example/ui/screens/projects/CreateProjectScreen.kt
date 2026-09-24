package com.example.ui.screens.projects

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppStrings

@Composable
fun CreateProjectScreen(
  onBack: () -> Unit,
  onCreateProject: (name: String, description: String, template: String) -> Unit
) {
  val strings = LocalAppStrings.current
  var projectName by remember { mutableStateOf("") }
  var projectDesc by remember { mutableStateOf("") }
  var selectedTemplate by remember { mutableStateOf("Kotlin") }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var isSubmitting by remember { mutableStateOf(false) }

  val templates = listOf(
    TemplateOption("Kotlin", "پروژه کاتلین / اندروید با ساختار src و build.gradle.kts", Icons.Default.Android),
    TemplateOption("Python", "اسکریپت پایتون با main.py و requirements.txt", Icons.Default.Code),
    TemplateOption("Web", "طراحی وب با فایل‌های index.html, style.css, script.js", Icons.Default.Language),
    TemplateOption("Empty", "مخزن خام فقط با فایل README.md", Icons.Default.InsertDriveFile)
  )

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.createProject,
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 20.dp, vertical = 16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      Text(
        text = strings.createProject,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      OutlinedTextField(
        value = projectName,
        onValueChange = {
          projectName = it
          if (errorMessage != null) errorMessage = null
        },
        label = { Text(strings.projectName) },
        placeholder = { Text("مثال: Arena Clash") },
        isError = errorMessage != null,
        supportingText = {
          if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
          }
        },
        leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("create_project_name_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = projectDesc,
        onValueChange = { projectDesc = it },
        label = { Text(strings.projectDesc) },
        placeholder = { Text("توضیحی مختصر درباره این پروژه...") },
        leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("create_project_desc_input"),
        minLines = 3,
        shape = RoundedCornerShape(12.dp)
      )

      Column {
        Text(
          text = strings.projectType,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(bottom = 10.dp)
        )

        templates.forEach { t ->
          val isSelected = selectedTemplate == t.id
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clip(RoundedCornerShape(12.dp))
              .clickable { selectedTemplate = t.id }
              .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                shape = RoundedCornerShape(12.dp)
              ),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = t.icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(14.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = t.id,
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = t.desc,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              RadioButton(
                selected = isSelected,
                onClick = { selectedTemplate = t.id }
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = {
          if (projectName.isBlank()) {
            errorMessage = "لطفاً نام پروژه را وارد کنید"
            return@Button
          }
          isSubmitting = true
          onCreateProject(projectName.trim(), projectDesc.trim(), selectedTemplate)
        },
        enabled = !isSubmitting,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("submit_create_project_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Icon(Icons.Default.AddCircle, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = strings.createProject,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }
    }
  }
}

private data class TemplateOption(
  val id: String,
  val desc: String,
  val icon: androidx.compose.ui.graphics.vector.ImageVector
)
