package com.example.ui.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen

@Composable
fun SearchScreen(
  projectId: String?,
  onBack: () -> Unit,
  onPerformSearch: (query: String, scope: String, projectId: String?) -> Unit
) {
  val strings = LocalAppStrings.current
  var searchQuery by remember { mutableStateOf("") }
  var selectedScope by remember { mutableStateOf(if (projectId != null) "PROJECT" else "ALL") }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.searchTitle,
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
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text(strings.searchTitle) },
        placeholder = { Text("مثال: fun main یا import یا class...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_input_field"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      Column {
        Text(
          text = strings.searchScope,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = selectedScope == "ALL",
            onClick = { selectedScope = "ALL" },
            label = { Text(strings.scopeAllProjects) }
          )
          if (projectId != null) {
            FilterChip(
              selected = selectedScope == "PROJECT",
              onClick = { selectedScope = "PROJECT" },
              label = { Text(strings.scopeCurrentProject) }
            )
          }
          FilterChip(
            selected = selectedScope == "NAMES",
            onClick = { selectedScope = "NAMES" },
            label = { Text(strings.scopeFileNames) }
          )
          FilterChip(
            selected = selectedScope == "CONTENT",
            onClick = { selectedScope = "CONTENT" },
            label = { Text(strings.scopeCodeContent) }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = {
          if (searchQuery.isNotBlank()) {
            onPerformSearch(searchQuery.trim(), selectedScope, if (selectedScope == "PROJECT") projectId else null)
          }
        },
        enabled = searchQuery.isNotBlank(),
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("execute_search_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        Icon(Icons.Default.Search, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(strings.search, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
      }
    }
  }
}
