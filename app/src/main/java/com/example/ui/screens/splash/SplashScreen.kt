package com.example.ui.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onSplashFinished: () -> Unit
) {
  val strings = LocalAppStrings.current

  // Animation states
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.95f,
    targetValue = 1.05f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "scale"
  )

  LaunchedEffect(Unit) {
    // 3 seconds splash as requested
    delay(3000)
    onSplashFinished()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(
            MetallicBlackBg,
            MetallicBlackSurface,
            MetallicBlackBg
          )
        )
      )
      .testTag("splash_screen_root"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier.padding(24.dp)
    ) {
      // App Icon with metallic glow
      Box(
        modifier = Modifier
          .size(128.dp)
          .scale(pulseScale)
          .clip(RoundedCornerShape(28.dp))
          .background(
            Brush.radialGradient(
              colors = listOf(
                MetallicCyanPrimary.copy(alpha = 0.35f),
                Color.Transparent
              )
            )
          )
          .padding(4.dp),
        contentAlignment = Alignment.Center
      ) {
        Image(
          painter = painterResource(id = R.drawable.codevault_icon_1790264756402),
          contentDescription = strings.appName,
          modifier = Modifier
            .size(108.dp)
            .clip(RoundedCornerShape(24.dp))
        )
      }

      Spacer(modifier = Modifier.height(28.dp))

      // App Title
      Text(
        text = strings.appName,
        style = MaterialTheme.typography.headlineLarge.copy(
          fontWeight = FontWeight.Black,
          letterSpacing = 2.sp
        ),
        color = MetallicTextPrimary
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Subtitle
      Text(
        text = strings.appSubtitle,
        style = MaterialTheme.typography.titleMedium,
        color = MetallicCyanPrimary,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(36.dp))

      // Circular loading progress
      CircularProgressIndicator(
        modifier = Modifier.size(32.dp),
        color = MetallicCyanPrimary,
        strokeWidth = 3.dp
      )

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = strings.splashLoading,
        style = MaterialTheme.typography.bodySmall,
        color = MetallicTextSecondary
      )
    }

    // Bottom info: Version & Developer name
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 36.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = strings.appVersion,
        style = MaterialTheme.typography.labelMedium,
        color = MetallicSilver
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = strings.appDeveloper,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = MetallicTextPrimary
      )
    }
  }
}
