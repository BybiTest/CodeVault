package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.AdShowListener
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType
import ir.tapsell.plus.TapsellPlusInitListener
import ir.tapsell.plus.model.AdNetworks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TapsellAdManager(private val context: Context) {

  companion object {
    private const val TAG = "TapsellAdManager"
    @Volatile private var isInitialized = false
    @Volatile private var isInitializing = false
  }

  private val _isAdReady = MutableStateFlow(false)
  val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

  private var lastRewardedResponseId: String? = null
  private var lastStandardBannerResponseId: String? = null
  private var lastInstantBannerResponseId: String? = null

  fun initialize() {
    if (isInitialized || isInitializing) return

    if (!TapsellConfig.isConfigured) {
      Log.w(TAG, "Tapsell config not set. Skipping initialization.")
      return
    }

    isInitializing = true
    try {
      TapsellPlus.initialize(context, TapsellConfig.APP_KEY, object : TapsellPlusInitListener {
        override fun onInitializeSuccess(adNetworks: AdNetworks) {
          isInitialized = true
          isInitializing = false
          Log.i(TAG, "Tapsell initialized. Networks: $adNetworks")
          preloadRewardedVideo()
        }

        override fun onInitializeFailed(adNetworks: AdNetworks, errorMessage: String?) {
          isInitializing = false
          Log.e(TAG, "Tapsell init failed: $errorMessage")
        }
      })
    } catch (e: Exception) {
      isInitializing = false
      Log.e(TAG, "Exception during Tapsell init: ${e.message}", e)
    }
  }

  fun isNetworkAvailable(): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  fun preloadRewardedVideo() {
    if (!isInitialized) return
    try {
      TapsellPlus.requestRewardedVideoAd(
        context,
        TapsellConfig.ZONE_REWARDED_VIDEO,
        object : AdRequestCallback() {
          override fun response(responseId: String?) {
            lastRewardedResponseId = responseId
            _isAdReady.value = true
            Log.d(TAG, "Rewarded video ready. responseId=$responseId")
          }
          override fun error(message: String?) {
            _isAdReady.value = false
            Log.e(TAG, "Rewarded video error: $message")
          }
        }
      )
    } catch (e: Exception) {
      Log.e(TAG, "preloadRewardedVideo exception: ${e.message}", e)
    }
  }

  fun preloadStandardBanner() {
    if (!isInitialized) return
    try {
      TapsellPlus.requestStandardBannerAd(
        context,
        TapsellConfig.ZONE_STANDARD_BANNER,
        TapsellPlusBannerType.BANNER_320x50,
        object : AdRequestCallback() {
          override fun response(responseId: String?) {
            lastStandardBannerResponseId = responseId
            Log.d(TAG, "Standard banner ready. responseId=$responseId")
          }
          override fun error(message: String?) {
            Log.e(TAG, "Standard banner error: $message")
          }
        }
      )
    } catch (e: Exception) {
      Log.e(TAG, "preloadStandardBanner exception: ${e.message}", e)
    }
  }

  fun preloadInstantBanner() {
    if (!isInitialized) return
    try {
      TapsellPlus.requestInstantBannerAd(
        context,
        TapsellConfig.ZONE_INSTANT_BANNER,
        TapsellPlusBannerType.BANNER_320x50,
        object : AdRequestCallback() {
          override fun response(responseId: String?) {
            lastInstantBannerResponseId = responseId
            Log.d(TAG, "Instant banner ready. responseId=$responseId")
          }
          override fun error(message: String?) {
            Log.e(TAG, "Instant banner error: $message")
          }
        }
      )
    } catch (e: Exception) {
      Log.e(TAG, "preloadInstantBanner exception: ${e.message}", e)
    }
  }

  fun requestRewardedAd(
    isVip: Boolean,
    onAdAvailable: () -> Unit,
    onAdNotAvailable: (reason: String) -> Unit
  ) {
    if (isVip) { onAdNotAvailable("VIP users do not receive ads."); return }
    if (!isNetworkAvailable()) { onAdNotAvailable("اتصال به اینترنت برقرار نیست"); return }
    if (!TapsellConfig.isConfigured) { onAdNotAvailable("کلیدهای تپسل پیکربندی نشده‌اند"); return }
    if (!isInitialized) { onAdNotAvailable("تپسل هنوز آماده نشده. لطفاً چند لحظه بعد امتحان کن."); return }

    if (lastRewardedResponseId != null && _isAdReady.value) {
      onAdAvailable(); return
    }

    try {
      TapsellPlus.requestRewardedVideoAd(
        context,
        TapsellConfig.ZONE_REWARDED_VIDEO,
        object : AdRequestCallback() {
          override fun response(responseId: String?) {
            lastRewardedResponseId = responseId
            _isAdReady.value = true
            onAdAvailable()
          }
          override fun error(message: String?) {
            _isAdReady.value = false
            onAdNotAvailable(message ?: "خطا در دریافت تبلیغ")
          }
        }
      )
    } catch (e: Exception) {
      onAdNotAvailable("خطای غیرمنتظره: ${e.message}")
    }
  }

  fun showRewardedAd(
    activity: Activity,
    isVip: Boolean,
    onRewardEarned: () -> Unit,
    onError: (String) -> Unit
  ) {
    if (isVip) { onError("کاربران VIP نیازی به مشاهده تبلیغ ندارند"); return }

    val responseId = lastRewardedResponseId
    if (responseId.isNullOrBlank()) {
      onError("تبلیغ هنوز آماده نیست. لطفاً دوباره تلاش کن."); return
    }

    try {
      TapsellPlus.showRewardedVideoAd(
        activity,
        responseId,
        object : AdShowListener() {
          override fun onRewarded(adNetwork: AdNetworks, responseId: String?) {
            Log.d(TAG, "onRewarded: $adNetwork")
            onRewardEarned()
          }

          override fun onClosed(adNetwork: AdNetworks, responseId: String?) {
            lastRewardedResponseId = null
            _isAdReady.value = false
            preloadRewardedVideo()
          }

          override fun onError(adNetwork: AdNetworks, errorMessage: String?) {
            Log.e(TAG, "Ad show error: $errorMessage")
            onError(errorMessage ?: "خطا در نمایش تبلیغ")
          }
        }
      )
    } catch (e: Exception) {
      onError("خطا در نمایش تبلیغ: ${e.message}")
    }
  }
}
