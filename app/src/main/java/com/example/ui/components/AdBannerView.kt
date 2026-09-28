package com.example.ui.components

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ads.AppLogger
import com.example.data.ads.TapsellAdManager
import com.example.data.ads.TapsellConfig
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.views.banner.BannerContainer

@Composable
fun AdBannerView(
    modifier: Modifier = Modifier,
    zoneId: String = TapsellConfig.ZONE_STANDARD_BANNER,
    bannerSize: BannerSize = BannerSize.BANNER_320_50
) {
    val activity = LocalActivity.current as? Activity
    var adId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var bannerContainer by remember { mutableStateOf<BannerContainer?>(null) }
    var isAdShown by remember { mutableStateOf(false) }

    val adManager = remember { TapsellAdManager.getInstance() }

    LaunchedEffect(zoneId) {
        isLoading = true
        errorMessage = null
        adManager.requestBannerAd(
            zoneId = zoneId,
            bannerSize = bannerSize,
            activity = activity,
            onSuccess = { newAdId ->
                adId = newAdId
                isLoading = false
                errorMessage = null
                AppLogger.log("AdBannerView received adId: $newAdId")
            },
            onFailure = { error ->
                isLoading = false
                errorMessage = error
                AppLogger.log("AdBannerView request failed: $error")
            }
        )
    }

    LaunchedEffect(adId, bannerContainer) {
        val currentAdId = adId
        val currentContainer = bannerContainer
        if (currentAdId != null && currentContainer != null && activity != null && !isAdShown) {
            adManager.showBannerAd(
                adId = currentAdId,
                container = currentContainer,
                activity = activity,
                onImpression = {
                    isAdShown = true
                    AppLogger.log("AdBannerView impression confirmed")
                },
                onClicked = {
                    AppLogger.log("AdBannerView clicked")
                },
                onFailed = { error ->
                    errorMessage = error
                    AppLogger.log("AdBannerView show failed: $error")
                }
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            adId?.let {
                adManager.destroyBannerAd(it)
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ad_banner_view"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("tapsell_banner_container"),
                factory = { ctx ->
                    BannerContainer(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        bannerContainer = this
                    }
                },
                update = { container ->
                    bannerContainer = container
                }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .testTag("ad_banner_loading"),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (errorMessage != null && !isAdShown) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tapsell Ad (${errorMessage})",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.testTag("ad_banner_error")
                    )
                }
            }
        }
    }
}
