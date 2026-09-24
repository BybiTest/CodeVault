package com.example.ui.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CodeVaultBottomNav
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.VipGold

@Composable
fun SettingsScreen(
  currentTheme: AppThemeMode,
  currentLanguage: AppLanguage,
  isVip: Boolean,
  onNavigate: (String) -> Unit
) {
  val strings = LocalAppStrings.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.settingsTitle
      )
    },
    bottomBar = {
      CodeVaultBottomNav(
        currentRoute = Screen.Settings.route,
        onNavigate = onNavigate
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // VIP Membership Banner / Card
      item {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onNavigate(Screen.Vip.route) }
            .border(1.dp, VipGold.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              color = VipGold.copy(alpha = 0.2f),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.size(48.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = VipGold, modifier = Modifier.size(28.dp))
              }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = if (isVip) strings.vipActive else strings.vipTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isVip) "دسترسی نامحدود فعال است" else "پروژه‌های نامحدود، حذف تبلیغات و تاریخچه نسخه‌ها",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      // Settings Group: Interface & Preferences
      item {
        Text(
          text = "شخصی‌سازی و رابط کاربری",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 4.dp)
        )
      }

      item {
        SettingsContainer {
          SettingsNavRow(
            icon = Icons.Default.Palette,
            title = strings.appearance,
            subtitle = currentTheme.name,
            onClick = { onNavigate(Screen.Appearance.route) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          SettingsNavRow(
            icon = Icons.Default.Language,
            title = strings.language,
            subtitle = if (currentLanguage == AppLanguage.FA) "فارسی (پیش‌فرض)" else "English",
            onClick = { onNavigate(Screen.Language.route) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          SettingsNavRow(
            icon = Icons.Default.Code,
            title = strings.editorSettings,
            subtitle = "اندازه فونت، شکست خطوط، شماره خطوط",
            onClick = { onNavigate(Screen.EditorSettings.route) }
          )
        }
      }

      // Settings Group: Vault Data Management
      item {
        Text(
          text = "مدیریت داده‌ها و مخزن",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 4.dp)
        )
      }

      item {
        SettingsContainer {
          SettingsNavRow(
            icon = Icons.Default.Storage,
            title = strings.storageTitle,
            subtitle = "آمار حجم و پاکسازی حافظه موقت",
            onClick = { onNavigate(Screen.Storage.route) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          SettingsNavRow(
            icon = Icons.Default.Backup,
            title = strings.backupTitle,
            subtitle = "پشتیبان‌گیری محلی و بازیابی فایل ZIP",
            onClick = { onNavigate(Screen.Backup.createRoute()) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          SettingsNavRow(
            icon = Icons.Default.DeleteOutline,
            title = strings.trashTitle,
            subtitle = "مدیریت و بازیابی فایل‌های حذف‌شده",
            onClick = { onNavigate(Screen.Trash.route) }
          )
        }
      }

      // Settings Group: About & Policies
      item {
        Text(
          text = "درباره و قوانین",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.padding(horizontal = 4.dp)
        )
      }

      item {
        SettingsContainer {
          SettingsNavRow(
            icon = Icons.Default.Info,
            title = strings.aboutApp,
            subtitle = strings.appVersion,
            onClick = { onNavigate(Screen.About.route) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          SettingsNavRow(
            icon = Icons.Default.Security,
            title = strings.privacyPolicy,
            subtitle = "حفظ کامل حریم خصوصی و ذخیره‌سازی بومی",
            onClick = { onNavigate(Screen.PrivacyPolicy.route) }
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          SettingsNavRow(
            icon = Icons.Default.Gavel,
            title = strings.termsOfService,
            subtitle = "شرایط استفاده از نرم‌افزار",
            onClick = { onNavigate(Screen.TermsOfService.route) }
          )
        }
      }
    }
  }
}

@Composable
fun SettingsContainer(content: @Composable ColumnScope.() -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
  }
}

@Composable
fun SettingsNavRow(
  icon: ImageVector,
  title: String,
  subtitle: String? = null,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() }
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
    Spacer(modifier = Modifier.width(14.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.onSurface
      )
      if (!subtitle.isNullOrEmpty()) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}
