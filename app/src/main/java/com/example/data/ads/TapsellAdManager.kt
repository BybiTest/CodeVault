package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import ir.tapsell.mediation.ad.show.AdStateListener
import ir.tapsell.mediation.ad.views.banner.BannerContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TapsellAdManager(private val context: Context) {

    companion object {
        private const val TAG = "TapsellAdManager"
    }

    private var lastRewardedResponseId: String? = null

    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    fun initialize() {
        Log.i(TAG, "Tapsell Mediation SDK initialized")
        preloadRewardedVideo()
    }

    // ============ بنر استاندارد ============
    fun createStandardBannerContainer(): BannerContainer = BannerContainer(context)

    fun loadStandardBanner(
        container: BannerContainer,
        onSuccess: (String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        try {
            Tapsell.requestBannerAd(
                TapsellConfig.ZONE_STANDARD_BANNER,
                BannerSize.BANNER_320_50,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) {
                        container.loadAd(adId)
                        onSuccess(adId)
                    }
                    override fun onFailure(message: String) {
                        onFailure(message)
                    }
                }
            )
        } catch (e: Exception) {
            onFailure(e.message ?: "Unknown error")
        }
    }

    // ============ بنر آنی ============
    fun createInstantBannerContainer(): BannerContainer = BannerContainer(context)

    fun loadInstantBanner(
        container: BannerContainer,
        onSuccess: (String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        try {
            Tapsell.requestBannerAd(
                TapsellConfig.ZONE_INSTANT_BANNER,
                BannerSize.BANNER_320_50,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) {
                        container.loadAd(adId)
                        onSuccess(adId)
                    }
                    override fun onFailure(message: String) {
                        onFailure(message)
                    }
                }
            )
        } catch (e: Exception) {
            onFailure(e.message ?: "Unknown error")
        }
    }

    // ============ ویدیو جایزه‌ای ============
    fun preloadRewardedVideo() {
        try {
            Tapsell.requestRewardedAd(
                TapsellConfig.ZONE_REWARDED_VIDEO,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) {
                        lastRewardedResponseId = adId
                        _isAdReady.value = true
                        Log.d(TAG, "Rewarded ad ready. adId=$adId")
                    }
                    override fun onFailure(message: String) {
                        _isAdReady.value = false
                        Log.e(TAG, "Rewarded video error: $message")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "preloadRewardedVideo exception: ${e.message}", e)
        }
    }

    fun requestRewardedAd(
        isVip: Boolean,
        onAdAvailable: () -> Unit,
        onAdNotAvailable: (reason: String) -> Unit
    ) {
        if (isVip) { onAdNotAvailable("VIP users do not receive ads."); return }

        if (lastRewardedResponseId != null && _isAdReady.value) {
            onAdAvailable(); return
        }

        try {
            Tapsell.requestRewardedAd(
                TapsellConfig.ZONE_REWARDED_VIDEO,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) {
                        lastRewardedResponseId = adId
                        _isAdReady.value = true
                        onAdAvailable()
                    }
                    override fun onFailure(message: String) {
                        _isAdReady.value = false
                        onAdNotAvailable(message)
                    }
                }
            )
        } catch (e: Exception) {
            onAdNotAvailable("خطا: ${e.message}")
        }
    }

    fun showRewardedAd(
        activity: Activity,
        isVip: Boolean,
        onRewardEarned: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (isVip) { onError("کاربران VIP نیازی به مشاهده تبلیغ ندارند"); return }

        val adId = lastRewardedResponseId
        if (adId.isNullOrBlank()) {
            onError("تبلیغ هنوز آماده نیست. لطفاً دوباره تلاش کن."); return
        }

        try {
            Tapsell.showRewardedAd(
                adId,
                activity,
                object : AdStateListener.Rewarded {
                    override fun onRewarded() {
                        Log.d(TAG, "onRewarded")
                        onRewardEarned()
                    }
                    override fun onAdClosed(completionState: AdShowCompletionState) {
                        lastRewardedResponseId = null
                        _isAdReady.value = false
                        preloadRewardedVideo()
                    }
                    override fun onAdFailed(message: String) {
                        Log.e(TAG, "onAdFailed: $message")
                        onError(message)
                    }
                }
            )
        } catch (e: Exception) {
            onError("خطا در نمایش تبلیغ: ${e.message}")
        }
    }
}
