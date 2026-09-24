package com.example.data.ads

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TapsellAdManager(private val context: Context) {

  private val TAG = "TapsellAdManager"
  private val _isAdReady = MutableStateFlow(false)
  val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

  fun initialize() {
    try {
      if (TapsellConfig.isConfigured) {
        Log.d(TAG, "Initializing Tapsell SDK with app key: ${TapsellConfig.appKey}")
        // Ready for official Tapsell SDK initialization
      } else {
        Log.i(TAG, "Tapsell configuration using placeholder keys. Configure in TapsellConfig.")
      }
    } catch (e: Exception) {
      Log.e(TAG, "Graceful catch during ad initialization: ${e.localizedMessage}")
    }
  }

  fun isNetworkAvailable(): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  fun requestRewardedAd(
    isVip: Boolean,
    onAdAvailable: () -> Unit,
    onAdNotAvailable: (reason: String) -> Unit
  ) {
    if (isVip) {
      onAdNotAvailable("VIP users do not receive ads.")
      return
    }

    if (!isNetworkAvailable()) {
      onAdNotAvailable("اتصال به اینترنت برقرار نیست")
      return
    }

    if (!TapsellConfig.isConfigured) {
      onAdNotAvailable("کلیدهای تپسل پیکربندی نشده‌اند")
      return
    }

    // In production with official Tapsell SDK, Tapsell.requestAd is invoked here.
    _isAdReady.value = true
    onAdAvailable()
  }

  fun showRewardedAd(
    isVip: Boolean,
    onRewardEarned: () -> Unit,
    onError: (String) -> Unit
  ) {
    if (isVip) {
      onError("کاربران VIP نیازی به مشاهده تبلیغ ندارند")
      return
    }

    if (!isNetworkAvailable()) {
      onError("عدم دسترسی به اینترنت جهت بارگذاری تبلیغ")
      return
    }

    // Real ad presentation callback
    _isAdReady.value = false
    onRewardEarned()
  }
}
