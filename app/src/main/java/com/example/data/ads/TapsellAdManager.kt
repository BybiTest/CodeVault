package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.AdStateListener
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowCompletionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TapsellAdManager(private val context: Context) {

    companion object {
        private const val TAG = "TapsellAdManager"
        @Volatile private var isInitialized = false
    }

    private val _isAdReady = MutableStateFlow(false)
    val isAdReady: StateFlow<Boolean> = _isAdReady.asStateFlow()

    private var lastRewardedResponseId: String? = null

    fun initialize() {
        if (isInitialized) return
        try {
            isInitialized = true
            Log.i(TAG, "Tapsell Mediation SDK ready")
            preloadRewardedVideo()
        } catch (e: Exception) {
            Log.e(TAG, "Tapsell init failed: ${e.message}", e)
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
                        Log.w(TAG, "No rewarded ad available: $message")
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
        if (!isNetworkAvailable()) { onAdNotAvailable("اتصال به اینترنت برقرار نیست"); return }
        if (!isInitialized) { onAdNotAvailable("تپسل هنوز آماده نشده"); return }

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

        val adId = lastRewardedResponseId
        if (adId.isNullOrBlank()) {
            onError("تبلیغ هنوز آماده نیست. لطفاً دوباره تلاش کن."); return
        }

        try {
            Tapsell.showRewardedAd(
                activity,
                adId,
                object : AdStateListener.Rewarded {
                    override fun onAdImpression() {
                        Log.d(TAG, "onAdImpression")
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "onAdClicked")
                    }

                    override fun onRewarded() {
                        Log.d(TAG, "onRewarded - user earned reward")
                        onRewardEarned()
                    }

                    override fun onAdClosed(completionState: AdShowCompletionState) {
                        Log.d(TAG, "onAdClosed: $completionState")
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
