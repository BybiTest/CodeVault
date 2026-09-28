package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import ir.tapsell.mediation.MediationInitializationListener
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.BannerSize
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import ir.tapsell.mediation.ad.views.banner.BannerContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TapsellAdManager private constructor() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())

    companion object {
        @Volatile
        private var instance: TapsellAdManager? = null

        fun getInstance(): TapsellAdManager {
            return instance ?: synchronized(this) {
                instance ?: TapsellAdManager().also { instance = it }
            }
        }
    }

    fun initialize(context: Context) {
        AppLogger.log("=== Tapsell initialize CALLED ===")
        try {
            Tapsell.setInitializationListener(object : MediationInitializationListener {
                override fun onInitializationComplete() {
                    _isInitialized.value = true
                    AppLogger.log("=== Tapsell initialized OK ===")
                    try {
                        Tapsell.setUserConsent(true)
                    } catch (e: Throwable) {
                        AppLogger.log("Tapsell setUserConsent note: ${e.message}")
                    }
                }
            })
        } catch (e: Throwable) {
            AppLogger.log("Error calling setInitializationListener: ${e.message}")
        }
    }

    fun requestBannerAd(
        zoneId: String = TapsellConfig.ZONE_STANDARD_BANNER,
        bannerSize: BannerSize = BannerSize.BANNER_320_50,
        activity: Activity? = null,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        AppLogger.log("requestBannerAd CALLED for zone: $zoneId, size: ${bannerSize.name}")
        val listener = object : RequestResultListener {
            override fun onSuccess(adId: String) {
                AppLogger.log("requestBannerAd SUCCESS: adId=$adId")
                mainHandler.post { onSuccess(adId) }
            }

            override fun onFailure(message: String) {
                AppLogger.log("requestBannerAd FAILED: $message")
                mainHandler.post { onFailure(message) }
            }
        }

        try {
            if (activity != null) {
                Tapsell.requestBannerAd(zoneId, bannerSize, activity, listener)
            } else {
                Tapsell.requestBannerAd(zoneId, bannerSize, listener)
            }
        } catch (e: Throwable) {
            AppLogger.log("Exception requesting banner ad: ${e.message}")
            mainHandler.post { onFailure(e.message ?: "Unknown error") }
        }
    }

    fun showBannerAd(
        adId: String,
        container: BannerContainer,
        activity: Activity,
        onImpression: () -> Unit = {},
        onClicked: () -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        AppLogger.log("showBannerAd CALLED: adId=$adId")
        try {
            Tapsell.showBannerAd(
                adId,
                container,
                activity,
                object : AdStateListener.Banner {
                    override fun onAdImpression() {
                        AppLogger.log("showBannerAd IMPRESSION: adId=$adId")
                        mainHandler.post { onImpression() }
                    }

                    override fun onAdClicked() {
                        AppLogger.log("showBannerAd CLICKED: adId=$adId")
                        mainHandler.post { onClicked() }
                    }

                    override fun onAdFailed(message: String) {
                        AppLogger.log("showBannerAd FAILED: $message")
                        mainHandler.post { onFailed(message) }
                    }
                }
            )
        } catch (e: Throwable) {
            AppLogger.log("Exception showing banner ad: ${e.message}")
            mainHandler.post { onFailed(e.message ?: "Unknown error") }
        }
    }

    fun destroyBannerAd(adId: String?) {
        if (!adId.isNullOrEmpty()) {
            AppLogger.log("destroyBannerAd CALLED: adId=$adId")
            try {
                Tapsell.destroyBannerAd(adId)
            } catch (e: Throwable) {
                AppLogger.log("Exception destroying banner ad: ${e.message}")
            }
        }
    }

    fun requestRewardedAd(
        zoneId: String = TapsellConfig.ZONE_REWARDED_VIDEO,
        activity: Activity? = null,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        AppLogger.log("requestRewardedAd CALLED for zone: $zoneId")
        val listener = object : RequestResultListener {
            override fun onSuccess(adId: String) {
                AppLogger.log("requestRewardedAd SUCCESS: adId=$adId")
                mainHandler.post { onSuccess(adId) }
            }

            override fun onFailure(message: String) {
                AppLogger.log("requestRewardedAd FAILED: $message")
                mainHandler.post { onFailure(message) }
            }
        }

        try {
            if (activity != null) {
                Tapsell.requestRewardedAd(zoneId, activity, listener)
            } else {
                Tapsell.requestRewardedAd(zoneId, listener)
            }
        } catch (e: Throwable) {
            AppLogger.log("Exception requesting rewarded ad: ${e.message}")
            mainHandler.post { onFailure(e.message ?: "Unknown error") }
        }
    }

    fun showRewardedAd(
        adId: String,
        activity: Activity,
        onRewarded: () -> Unit = {},
        onClosed: (AdShowCompletionState) -> Unit = {},
        onFailed: (String) -> Unit = {}
    ) {
        AppLogger.log("showRewardedAd CALLED: adId=$adId")
        try {
            Tapsell.showRewardedAd(
                adId,
                activity,
                object : AdStateListener.Rewarded {
                    override fun onAdImpression() {
                        AppLogger.log("showRewardedAd IMPRESSION: adId=$adId")
                    }

                    override fun onAdClicked() {
                        AppLogger.log("showRewardedAd CLICKED: adId=$adId")
                    }

                    override fun onRewarded() {
                        AppLogger.log("showRewardedAd REWARDED: adId=$adId")
                        mainHandler.post { onRewarded() }
                    }

                    override fun onAdClosed(completionState: AdShowCompletionState) {
                        AppLogger.log("showRewardedAd CLOSED: state=${completionState.name}")
                        mainHandler.post { onClosed(completionState) }
                    }

                    override fun onAdFailed(message: String) {
                        AppLogger.log("showRewardedAd FAILED: $message")
                        mainHandler.post { onFailed(message) }
                    }
                }
            )
        } catch (e: Throwable) {
            AppLogger.log("Exception showing rewarded ad: ${e.message}")
            mainHandler.post { onFailed(e.message ?: "Unknown error") }
        }
    }
}
