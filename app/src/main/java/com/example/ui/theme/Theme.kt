package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
  METALLIC_BLACK,
  LIGHT,
  AMOLED,
  MIDNIGHT,
  GRAPHITE,
  CYBER
}

private val MetallicBlackColorScheme = darkColorScheme(
  primary = MetallicCyanPrimary,
  onPrimary = Color(0xFF001A2C),
  primaryContainer = MetallicBlackContainer,
  onPrimaryContainer = MetallicCyanPrimary,
  secondary = MetallicSilver,
  onSecondary = Color(0xFF0F172A),
  tertiary = MetallicCyanDark,
  background = MetallicBlackBg,
  onBackground = MetallicTextPrimary,
  surface = MetallicBlackSurface,
  onSurface = MetallicTextPrimary,
  surfaceVariant = MetallicBlackSurfaceVariant,
  onSurfaceVariant = MetallicTextSecondary,
  outline = MetallicBorder,
  error = ErrorRed,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = LightPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0F2FE),
  onPrimaryContainer = Color(0xFF0369A1),
  secondary = Color(0xFF475569),
  onSecondary = Color.White,
  tertiary = Color(0xFF0284C7),
  background = LightBg,
  onBackground = LightTextPrimary,
  surface = LightSurface,
  onSurface = LightTextPrimary,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = LightTextSecondary,
  outline = Color(0xFFCBD5E1),
  error = ErrorRed,
  onError = Color.White
)

private val AmoledColorScheme = darkColorScheme(
  primary = MetallicCyanPrimary,
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF141414),
  onPrimaryContainer = MetallicCyanPrimary,
  secondary = Color(0xFFD4D4D4),
  onSecondary = Color.Black,
  background = AmoledBg,
  onBackground = Color(0xFFFAFAFA),
  surface = AmoledSurface,
  onSurface = Color(0xFFFAFAFA),
  surfaceVariant = AmoledSurfaceVariant,
  onSurfaceVariant = Color(0xFFA3A3A3),
  outline = Color(0xFF262626),
  error = ErrorRed,
  onError = Color.White
)

private val MidnightColorScheme = darkColorScheme(
  primary = MidnightPrimary,
  onPrimary = Color.White,
  primaryContainer = Color(0xFF312E81),
  onPrimaryContainer = Color(0xFFC7D2FE),
  secondary = Color(0xFF94A3B8),
  onSecondary = Color.White,
  background = MidnightBg,
  onBackground = Color(0xFFF1F5F9),
  surface = MidnightSurface,
  onSurface = Color(0xFFF1F5F9),
  surfaceVariant = MidnightSurfaceVariant,
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF374151),
  error = ErrorRed,
  onError = Color.White
)

private val GraphiteColorScheme = darkColorScheme(
  primary = GraphitePrimary,
  onPrimary = Color(0xFF18181B),
  primaryContainer = Color(0xFF3F3F46),
  onPrimaryContainer = Color(0xFFF4F4F5),
  secondary = Color(0xFFA1A1AA),
  onSecondary = Color(0xFF18181B),
  background = GraphiteBg,
  onBackground = Color(0xFFF4F4F5),
  surface = GraphiteSurface,
  onSurface = Color(0xFFF4F4F5),
  surfaceVariant = GraphiteSurfaceVariant,
  onSurfaceVariant = Color(0xFFA1A1AA),
  outline = Color(0xFF52525B),
  error = ErrorRed,
  onError = Color.White
)

private val CyberColorScheme = darkColorScheme(
  primary = CyberPrimary,
  onPrimary = Color(0xFF042F2E),
  primaryContainer = Color(0xFF134E4A),
  onPrimaryContainer = CyberPrimary,
  secondary = Color(0xFF2DD4BF),
  onSecondary = Color(0xFF042F2E),
  background = CyberBg,
  onBackground = Color(0xFFF0FDFA),
  surface = CyberSurface,
  onSurface = Color(0xFFF0FDFA),
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = Color(0xFF99F6E4),
  outline = Color(0xFF1E3A4C),
  error = ErrorRed,
  onError = Color.White
)

@Composable
fun CodeVaultTheme(
  themeMode: AppThemeMode = AppThemeMode.METALLIC_BLACK,
  content: @Composable () -> Unit
) {
  val colorScheme: ColorScheme = when (themeMode) {
    AppThemeMode.METALLIC_BLACK -> MetallicBlackColorScheme
    AppThemeMode.LIGHT -> LightColorScheme
    AppThemeMode.AMOLED -> AmoledColorScheme
    AppThemeMode.MIDNIGHT -> MidnightColorScheme
    AppThemeMode.GRAPHITE -> GraphiteColorScheme
    AppThemeMode.CYBER -> CyberColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
