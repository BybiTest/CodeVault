package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.ui.localization.*
import com.example.ui.navigation.CodeVaultNavGraph
import com.example.ui.theme.CodeVaultTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val currentTheme by viewModel.themeMode.collectAsStateWithLifecycle()
      val currentLanguage by viewModel.language.collectAsStateWithLifecycle()
      val appStrings = getStrings(currentLanguage)
      val layoutDirection = getLayoutDirection(currentLanguage)
      val navController = rememberNavController()

      CompositionLocalProvider(
        LocalAppStrings provides appStrings,
        LocalLayoutDirection provides layoutDirection,
        LocalAppLanguage provides currentLanguage
      ) {
        CodeVaultTheme(themeMode = currentTheme) {
          Surface(modifier = Modifier.fillMaxSize()) {
            CodeVaultNavGraph(
              navController = navController,
              viewModel = viewModel
            )
          }
        }
      }
    }
  }
}
