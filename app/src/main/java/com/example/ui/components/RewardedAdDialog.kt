package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.CodeVaultApplication

@Composable
fun RewardedAdDialog(
    isVip: Boolean,
    title: String = "تماشای ویدیو برای ادامه",
    message: String = "برای دسترسی به این قابلیت، لطفاً یک ویدیو کوتاه تماشا کنید.",
    onRewardEarned: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val app = context.applicationContext as CodeVaultApplication
    var isLoading by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        icon = { Icon(Icons.Default.PlayCircle, contentDescription = null, modifier = Modifier.size(48.dp)) },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                enabled = !isLoading,
                onClick = {
                    if (isVip) {
                        onRewardEarned()
                        return@Button
                    }
                    if (activity == null) return@Button
                    isLoading = true
                    app.tapsellAdManager.requestRewardedAd(
                        isVip = false,
                        onAdAvailable = {
                            app.tapsellAdManager.showRewardedAd(
                                activity = activity,
                                isVip = false,
                                onRewardEarned = {
                                    isLoading = false
                                    onRewardEarned()
                                },
                                onError = {
                                    isLoading = false
                                    onDismiss()
                                }
                            )
                        },
                        onAdNotAvailable = {
                            isLoading = false
                            onDismiss()
                        }
                    )
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text("تماشای ویدیو")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isLoading) {
                Text("بعداً")
            }
        }
    )
}
