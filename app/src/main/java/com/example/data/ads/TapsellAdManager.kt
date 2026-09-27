package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import ir.tapsell.mediation.ad.views.banner.BannerContainer

class TapsellAdManager(private val context: Context) {

    companion object {
        private const val TAG = "TapsellAdManager"
    }

    private var lastRewardedAdId: String? = null

    fun initialize() {
        Log.i(TAG, "Tapsell Mediation SDK initialized")
        preloadRewardedVideo()
    }

    // ============ بنر استاندارد ============
    fun createStandardBannerContainer(activity: Activity): BannerContainer {
        return BannerContainer(activity)
    }

    fun loadStandardBanner(
        container: BannerContainer,
        activity: Activity,
        onSuccess: (String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        try {
            Tapsell.requestBannerAd(
                TapsellConfig.ZONE_STANDARD_BANNER,
                BannerSize.BANNER_320_50,
                activity,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) {
                        Tapsell.showBannerAd(adId, container, activity)
                        onSuccess(adId)
                    }
                    override fun onFailure(message: String) {
                        Log.e(TAG, "Banner error: $message")
                        onFailure(message)
                    }
                }
            )
        } catch (e: Exception) {
            onFailure(e.message ?: "Unknown error")
        }
    }

    fun destroyStandardBanner(adId: String) {
        try {
            Tapsell.destroyBannerAd(adId)
        } catch (e: Exception) {
            Log.e(TAG, "destroy error: ${e.message}")
        }
    }

    // ============ بنر آنی ============
    fun createInstantBannerContainer(activity: Activity): BannerContainer {
        return BannerContainer(activity)
    }

    fun loadInstantBanner(
        container: BannerContainer,
        activity: Activity,
        onSuccess: (String) -> Unit = {},
        onFailure: (String) -> Unit = {}
    ) {
        try {
            Tapsell.requestBannerAd(
                TapsellConfig.ZONE_INSTANT_BANNER,
                BannerSize.BANNER_320_50,
                activity,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) {
                        Tapsell.showBannerAd(adId, container, activity)
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
                        lastRewardedAdId = adId
                        Log.d(TAG, "Rewarded ad ready. adId=$adId")
                    }
                    override fun onFailure(message: String) {
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

        if (lastRewardedAdId != null) {
            onAdAvailable()
            return
        }

        try {
            Tapsell.requestRewardedAd(
                TapsellConfig.ZONE_REWARDED_VIDEO,
                object : RequestResultListener {
                    override fun onSuccess(adId: String) {
                        lastRewardedAdId = adId
                        onAdAvailable()
                    }
                    override fun onFailure(message: String) {
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

        val adId = lastRewardedAdId
        if (adId.isNullOrBlank()) {
            onError("تبلیغ هنوز آماده نیست. لطفاً دوباره تلاش کن."); return
        }

        try {
            Tapsell.showRewardedAd(
                adId,
                activity,
                object : AdStateListener.Rewarded {
                    override fun onAdImpression() {
                        Log.d(TAG, "onAdImpression")
                    }
                    override fun onAdClicked() {
                        Log.d(TAG, "onAdClicked")
                    }
                    override fun onRewarded() {
                        Log.d(TAG, "onRewarded")
                        onRewardEarned()
                    }
                    override fun onAdClosed(completionState: AdShowCompletionState) {
                        lastRewardedAdId = null
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
