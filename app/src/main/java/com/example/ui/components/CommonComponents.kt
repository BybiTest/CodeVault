package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeVaultTopBar(
  title: String,
  subtitle: String? = null,
  onBackClick: (() -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {}
) {
  val strings = LocalAppStrings.current
  TopAppBar(
    title = {
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrBlank()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }
    },
    navigationIcon = {
      if (onBackClick != null) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("topbar_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = strings.back
          )
        }
      }
    },
    actions = actions,
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface,
      titleContentColor = MaterialTheme.colorScheme.onSurface,
      navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
      actionIconContentColor = MaterialTheme.colorScheme.onSurface
    )
  )
}

@Composable
fun CodeVaultBottomNav(
  currentRoute: String?,
  onNavigate: (String) -> Unit
) {
  val strings = LocalAppStrings.current

  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp,
    modifier = Modifier.testTag("codevault_bottom_navigation")
  ) {
    val items = listOf(
      NavigationItem(
        route = Screen.Dashboard.route,
        title = strings.navDashboard,
        icon = Icons.Default.Dashboard,
        selectedIcon = Icons.Filled.Dashboard
      ),
      NavigationItem(
        route = Screen.Projects.route,
        title = strings.navProjects,
        icon = Icons.Default.FolderSpecial,
        selectedIcon = Icons.Filled.FolderSpecial
      ),
      NavigationItem(
        route = Screen.StarredProjects.route,
        title = strings.navStarred,
        icon = Icons.Default.StarBorder,
        selectedIcon = Icons.Filled.Star
      ),
      NavigationItem(
        route = Screen.RecentFiles.route,
        title = strings.navRecent,
        icon = Icons.Default.History,
        selectedIcon = Icons.Filled.History
      ),
      NavigationItem(
        route = Screen.Settings.route,
        title = strings.navSettings,
        icon = Icons.Default.Settings,
        selectedIcon = Icons.Filled.Settings
      )
    )

    items.forEach { item ->
      val selected = currentRoute == item.route
      NavigationBarItem(
        selected = selected,
        onClick = { if (!selected) onNavigate(item.route) },
        icon = {
          Icon(
            imageVector = if (selected) item.selectedIcon else item.icon,
            contentDescription = item.title
          )
        },
        label = {
          Text(
            text = item.title,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          selectedTextColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
      )
    }
  }
}

private data class NavigationItem(
  val route: String,
  val title: String,
  val icon: ImageVector,
  val selectedIcon: ImageVector
)

@Composable
fun ConfirmDialog(
  title: String,
  message: String,
  confirmButtonText: String,
  dismissButtonText: String,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit,
  isDestructive: Boolean = false
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Text(
        text = message,
        style = MaterialTheme.typography.bodyMedium
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = if (isDestructive) {
          ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        } else {
          ButtonDefaults.buttonColors()
        }
      ) {
        Text(confirmButtonText)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text(dismissButtonText)
      }
    }
  )
}
