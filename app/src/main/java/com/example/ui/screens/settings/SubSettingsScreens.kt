package com.example.ui.screens.settings

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.AppThemeMode

@Composable
fun AppearanceScreen(
  currentTheme: AppThemeMode,
  onThemeSelect: (AppThemeMode) -> Unit,
  onBack: () -> Unit
) {
  val strings = LocalAppStrings.current

  val themes = listOf(
    Pair(AppThemeMode.METALLIC_BLACK, strings.themeMetallicBlack),
    Pair(AppThemeMode.LIGHT, strings.themeLight),
    Pair(AppThemeMode.AMOLED, strings.themeAmoled),
    Pair(AppThemeMode.MIDNIGHT, strings.themeMidnight),
    Pair(AppThemeMode.GRAPHITE, strings.themeGraphite),
    Pair(AppThemeMode.CYBER, strings.themeCyber)
  )

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.appearance,
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Text(
        text = "انتخاب پوسته برنامه (Theme)",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 6.dp)
      )

      themes.forEach { (mode, name) ->
        val isSelected = currentTheme == mode
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onThemeSelect(mode) }
            .border(
              width = if (isSelected) 2.dp else 1.dp,
              color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
              shape = RoundedCornerShape(12.dp)
            ),
          colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = name,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
              color = MaterialTheme.colorScheme.onSurface
            )
            if (isSelected) {
              Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
          }
        }
      }
    }
  }
}

@Composable
fun LanguageScreen(
  currentLanguage: AppLanguage,
  onLanguageSelect: (AppLanguage) -> Unit,
  onBack: () -> Unit
) {
  val strings = LocalAppStrings.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.language,
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Text(
        text = "زبان رابط کاربری / Interface Language",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      LanguageOptionCard(
        title = "فارسی (راست‌چین - پیش‌فرض)",
        subtitle = "زبان رسمی با چیدمان کامل راست‌چین (RTL)",
        isSelected = currentLanguage == AppLanguage.FA,
        onClick = { onLanguageSelect(AppLanguage.FA) }
      )

      LanguageOptionCard(
        title = "English (Left-to-Right)",
        subtitle = "Standard English interface with LTR layout",
        isSelected = currentLanguage == AppLanguage.EN,
        onClick = { onLanguageSelect(AppLanguage.EN) }
      )
    }
  }
}

@Composable
private fun LanguageOptionCard(
  title: String,
  subtitle: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onClick() }
      .border(
        width = if (isSelected) 2.dp else 1.dp,
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
        shape = RoundedCornerShape(12.dp)
      ),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      if (isSelected) {
        Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      }
    }
  }
}

@Composable
fun EditorSettingsScreen(
  fontSize: Int,
  lineNumbers: Boolean,
  wordWrap: Boolean,
  tabSize: Int,
  syntaxHighlighting: Boolean,
  autoSave: Boolean,
  onUpdateFontSize: (Int) -> Unit,
  onToggleLineNumbers: (Boolean) -> Unit,
  onToggleWordWrap: (Boolean) -> Unit,
  onUpdateTabSize: (Int) -> Unit,
  onToggleSyntax: (Boolean) -> Unit,
  onToggleAutoSave: (Boolean) -> Unit,
  onBack: () -> Unit
) {
  val strings = LocalAppStrings.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.editorSettings,
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(16.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Font size slider
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(strings.fontSize, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Text("${fontSize}sp", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
          }
          Slider(
            value = fontSize.toFloat(),
            onValueChange = { onUpdateFontSize(it.toInt()) },
            valueRange = 10f..26f,
            steps = 7
          )
        }
      }

      // Switches
      SettingsContainer {
        ToggleRow(
          title = strings.lineNumbers,
          subtitle = "نمایش ستون شماره خطوط در سمت چپ ویرایشگر",
          checked = lineNumbers,
          onCheckedChange = onToggleLineNumbers
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ToggleRow(
          title = strings.wordWrap,
          subtitle = "شکستن خطوط طولانی به خط بعد به جای اسکرول افقی",
          checked = wordWrap,
          onCheckedChange = onToggleWordWrap
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ToggleRow(
          title = strings.syntaxHighlighting,
          subtitle = "رنگ‌آمیزی کلمات کلیدی، رشته‌ها و توابع متناسب با زبان",
          checked = syntaxHighlighting,
          onCheckedChange = onToggleSyntax
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        ToggleRow(
          title = "ذخیره خودکار (Auto-Save)",
          subtitle = "ذخیره خودکار کدها چند ثانیه پس از توقف تایپ",
          checked = autoSave,
          onCheckedChange = onToggleAutoSave
        )
      }

      // Tab size options
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(strings.tabSize, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
          Spacer(modifier = Modifier.height(10.dp))
          Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
              selected = tabSize == 2,
              onClick = { onUpdateTabSize(2) },
              label = { Text("۲ فاصله (Spaces)") }
            )
            FilterChip(
              selected = tabSize == 4,
              onClick = { onUpdateTabSize(4) },
              label = { Text("۴ فاصله (پیش‌فرض)") }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onCheckedChange(!checked) }
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
      Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange
    )
  }
}
