package com.example.ui.components

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.ads.AppLogger
import com.example.data.ads.TapsellAdManager
import com.example.data.ads.TapsellConfig

@Composable
fun RewardedAdDialog(
    isVip: Boolean = false,
    message: String = "Watch a short video sponsor to unlock export functionality.",
    title: String = "Watch Ad to Unlock",
    zoneId: String = TapsellConfig.ZONE_REWARDED_VIDEO,
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit
) {
    if (isVip) {
        onRewardEarned()
        return
    }

    val activity = LocalActivity.current as? Activity
    val adManager = remember { TapsellAdManager.getInstance() }

    var adId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isShowingAd by remember { mutableStateOf(false) }

    LaunchedEffect(zoneId) {
        isLoading = true
        errorMessage = null
        adManager.requestRewardedAd(
            zoneId = zoneId,
            activity = activity,
            onSuccess = { newAdId ->
                adId = newAdId
                isLoading = false
                errorMessage = null
                AppLogger.log("RewardedAdDialog ad loaded: $newAdId")
            },
            onFailure = { error ->
                isLoading = false
                errorMessage = error
                AppLogger.log("RewardedAdDialog request failed: $error")
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("rewarded_ad_dialog"),
        icon = {
            Icon(
                imageVector = Icons.Default.CardGiftcard,
                contentDescription = "Reward",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("rewarded_ad_loading"),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Loading sponsored video...",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else if (errorMessage != null) {
                    Text(
                        text = "Unable to load video: $errorMessage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("rewarded_ad_error")
                    )
                } else {
                    Text(
                        text = "Video ready to play!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val currentAdId = adId
                    if (currentAdId != null && activity != null) {
                        isShowingAd = true
                        adManager.showRewardedAd(
                            adId = currentAdId,
                            activity = activity,
                            onRewarded = {
                                AppLogger.log("RewardedAdDialog: Reward granted to user!")
                                onRewardEarned()
                            },
                            onClosed = { state ->
                                AppLogger.log("RewardedAdDialog: Ad closed with state $state")
                                onDismiss()
                            },
                            onFailed = { err ->
                                errorMessage = err
                                AppLogger.log("RewardedAdDialog: Ad show failed: $err")
                            }
                        )
                    }
                },
                enabled = adId != null && !isLoading && !isShowingAd,
                modifier = Modifier.testTag("watch_video_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Watch Video")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_ad_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
