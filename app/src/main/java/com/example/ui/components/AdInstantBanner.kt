package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.CodeVaultApplication

@Composable
fun AdInstantBanner(
    isVip: Boolean,
    modifier: Modifier = Modifier
) {
    if (isVip) return

    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val app = context.applicationContext as CodeVaultApplication
    val container = remember { app.tapsellAdManager.createInstantBannerContainer() }
    var adId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        app.tapsellAdManager.loadInstantBanner(
            container = container,
            activity = activity,
            onSuccess = { adId = it },
            onFailure = { /* Ad failed to load */ }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            adId?.let {
                try {
                    ir.tapsell.mediation.Tapsell.destroyBannerAd(it)
                } catch (_: Exception) {}
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { container }
        )
    }
}
