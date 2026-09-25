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
    private var isInitialized = false
    private var isInitializing = false
  }

  private val _isAdReady = MutableStateFlow(false)
  val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

  /** آخرین responseId دریافتی از تپسل برای نمایش تبلیغ */
  private var lastRewardedResponseId: String? = null
  private var lastStandardBannerResponseId: String? = null
  private var lastInstantBannerResponseId: String? = null

  /**
   * اینitialize تپسل — باید یک بار در Application.onCreate صدا زده بشه
   */
  fun initialize() {
    if (isInitialized || isInitializing) {
      Log.d(TAG, "Tapsell already initialized or initializing")
      return
    }

    if (!TapsellConfig.isConfigured) {
      Log.w(TAG, "Tapsell config not set. Skipping initialization.")
      return
    }

    isInitializing = true
    TapsellPlus.initialize(context, TapsellConfig.APP_KEY, object : TapsellPlusInitListener {
      override fun onInitializeSuccess(adNetworks: AdNetworks) {
        isInitialized = true
        isInitializing = false
        Log.i(TAG, "Tapsell initialized successfully. Networks: $adNetworks")
        // پیش‌بارگذاری تبلیغ جایزه‌ای
        preloadRewardedVideo()
      }

      override fun onInitializeFailed(adNetworks: AdNetworks, errorMessage: String?) {
        isInitializing = false
        Log.e(TAG, "Tapsell init failed: $errorMessage")
      }
    })
  }

  fun isNetworkAvailable(): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
      ?: return false
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  /** پیش‌بارگذاری تبلیغ ویدیوی جایزه‌ای */
  fun preloadRewardedVideo() {
    if (!isInitialized) return
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
          Log.e(TAG, "Rewarded video request error: $message")
        }
      }
    )
  }

  /** پیش‌بارگذاری بنر استاندارد (اگه بخوای از Compose نمایش بدی) */
  fun preloadStandardBanner() {
    if (!isInitialized) return
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
  }

  /** پیش‌بارگذاری بنر آنی */
  fun preloadInstantBanner() {
    if (!isInitialized) return
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
  }

  /**
   * درخواست تبلیغ جایزه‌ای — callback روی Main Thread اجرا میشه
   */
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

    if (!isInitialized) {
      onAdNotAvailable("تپسل هنوز آماده نشده. لطفاً چند لحظه بعد امتحان کن.")
      return
    }

    if (lastRewardedResponseId != null && _isAdReady.value) {
      onAdAvailable()
      return
    }

    // درخواست مجدد
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
  }

  /**
   * نمایش تبلیغ جایزه‌ای — باید Activity بدی چون SDK نیاز داره
   */
  fun showRewardedAd(
    activity: Activity,
    isVip: Boolean,
    onRewardEarned: () -> Unit,
    onError: (String) -> Unit
  ) {
    if (isVip) {
      onError("کاربران VIP نیازی به مشاهده تبلیغ ندارند")
      return
    }

    val responseId = lastRewardedResponseId
    if (responseId.isNullOrBlank()) {
      onError("تبلیغ هنوز آماده نیست. لطفاً دوباره تلاش کن.")
      return
    }

    TapsellPlus.showRewardedVideoAd(
      activity,
      responseId,
      object : AdShowListener() {
        override fun onRewarded(adNetwork: AdNetworks, responseId: String?) {
          Log.d(TAG, "onRewarded: $adNetwork")
          onRewardEarned()
        }

        override fun onClosed(adNetwork: AdNetworks, responseId: String?) {
          Log.d(TAG, "Ad closed")
          // آماده‌سازی تبلیغ بعدی
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
  }
}
