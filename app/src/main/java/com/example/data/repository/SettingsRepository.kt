package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "codevault_settings")

class SettingsRepository(private val context: Context) {

  companion object {
    val KEY_THEME = stringPreferencesKey("app_theme")
    val KEY_LANGUAGE = stringPreferencesKey("app_language")
    val KEY_EDITOR_FONT_SIZE = intPreferencesKey("editor_font_size")
    val KEY_LINE_NUMBERS = booleanPreferencesKey("editor_line_numbers")
    val KEY_WORD_WRAP = booleanPreferencesKey("editor_word_wrap")
    val KEY_TAB_SIZE = intPreferencesKey("editor_tab_size")
    val KEY_SYNTAX_HIGHLIGHTING = booleanPreferencesKey("editor_syntax_highlighting")
    val KEY_AUTO_SAVE = booleanPreferencesKey("editor_auto_save")
    val KEY_VIP_ACTIVE = booleanPreferencesKey("vip_active")
    val KEY_FIRST_LAUNCH = booleanPreferencesKey("first_launch")
  }

  val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
    val name = prefs[KEY_THEME] ?: AppThemeMode.METALLIC_BLACK.name
    try {
      AppThemeMode.valueOf(name)
    } catch (_: Exception) {
      AppThemeMode.METALLIC_BLACK
    }
  }

  suspend fun setThemeMode(mode: AppThemeMode) {
    context.dataStore.edit { prefs ->
      prefs[KEY_THEME] = mode.name
    }
  }

  val language: Flow<AppLanguage> = context.dataStore.data.map { prefs ->
    val lang = prefs[KEY_LANGUAGE] ?: AppLanguage.FA.name
    try {
      AppLanguage.valueOf(lang)
    } catch (_: Exception) {
      AppLanguage.FA
    }
  }

  suspend fun setLanguage(lang: AppLanguage) {
    context.dataStore.edit { prefs ->
      prefs[KEY_LANGUAGE] = lang.name
    }
  }

  val editorFontSize: Flow<Int> = context.dataStore.data.map { prefs ->
    prefs[KEY_EDITOR_FONT_SIZE] ?: 14
  }

  suspend fun setEditorFontSize(size: Int) {
    context.dataStore.edit { prefs ->
      prefs[KEY_EDITOR_FONT_SIZE] = size
    }
  }

  val lineNumbersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_LINE_NUMBERS] ?: true
  }

  suspend fun setLineNumbersEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_LINE_NUMBERS] = enabled
    }
  }

  val wordWrapEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_WORD_WRAP] ?: false
  }

  suspend fun setWordWrapEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_WORD_WRAP] = enabled
    }
  }

  val tabSize: Flow<Int> = context.dataStore.data.map { prefs ->
    prefs[KEY_TAB_SIZE] ?: 4
  }

  suspend fun setTabSize(size: Int) {
    context.dataStore.edit { prefs ->
      prefs[KEY_TAB_SIZE] = size
    }
  }

  val syntaxHighlightingEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_SYNTAX_HIGHLIGHTING] ?: true
  }

  suspend fun setSyntaxHighlightingEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_SYNTAX_HIGHLIGHTING] = enabled
    }
  }

  val autoSaveEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_AUTO_SAVE] ?: true
  }

  suspend fun setAutoSaveEnabled(enabled: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_AUTO_SAVE] = enabled
    }
  }

  val isVipActive: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_VIP_ACTIVE] ?: false
  }

  suspend fun setVipActive(active: Boolean) {
    context.dataStore.edit { prefs ->
      prefs[KEY_VIP_ACTIVE] = active
    }
  }

  val isFirstLaunch: Flow<Boolean> = context.dataStore.data.map { prefs ->
    prefs[KEY_FIRST_LAUNCH] ?: true
  }

  suspend fun setFirstLaunchCompleted() {
    context.dataStore.edit { prefs ->
      prefs[KEY_FIRST_LAUNCH] = false
    }
  }
}
