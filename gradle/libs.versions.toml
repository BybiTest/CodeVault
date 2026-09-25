package com.example.data.ads

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import ir.tapsell.mediation.Tapsell
import ir.tapsell.mediation.ad.request.RequestResultListener
import ir.tapsell.mediation.ad.show.AdShowListener
import ir.tapsell.mediation.ad.show.ShowResultListener
import ir.tapsell.mediation.ad.views.bnr.BannerAdViewContainer
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
    private var lastBannerResponseId: String? = null

    /**
     * مقداردهی اولیه تپسل. این متد باید در Application.onCreate صدا زده شود.
     * @param appKey کلید اپلیکیشن از پنل تپسل
     */
    fun initialize(appKey: String) {
        if (isInitialized) return
        try {
            // در SDK جدید، مقداردهی اولیه به این صورت انجام می‌شود
            // اگر از View استفاده می‌کنید، Tapsell.init در MainActivity هم صدا زده می‌شود
            isInitialized = true
            Log.i(TAG, "Tapsell Mediation SDK initialized")
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

    /**
     * پیش‌بارگذاری تبلیغ جایزه‌ای (Rewarded Video)
     * در SDK جدید از متد requestRewardedAd استفاده می‌شود.
     */
    fun preloadRewardedVideo() {
        if (!isInitialized) return
        try {
            Tapsell.requestRewardedAd(
                TapsellConfig.ZONE_REWARDED_VIDEO,
                object : RequestResultListener {
                    override fun onAdAvailable(adId: String) {
                        lastRewardedResponseId = adId
                        _isAdReady.value = true
                        Log.d(TAG, "Rewarded ad ready. adId=$adId")
                    }

                    override fun onNoAdAvailable() {
                        _isAdReady.value = false
                        Log.w(TAG, "No rewarded ad available")
                    }

                    override fun onError(message: String) {
                        _isAdReady.value = false
                        Log.e(TAG, "Rewarded ad error: $message")
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "preloadRewardedVideo exception: ${e.message}", e)
        }
    }

    /**
     * درخواست تبلیغ جایزه‌ای با callback
     */
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
                    override fun onAdAvailable(adId: String) {
                        lastRewardedResponseId = adId
                        _isAdReady.value = true
                        onAdAvailable()
                    }

                    override fun onNoAdAvailable() {
                        _isAdReady.value = false
                        onAdNotAvailable("تبلیغی موجود نیست")
                    }

                    override fun onError(message: String) {
                        _isAdReady.value = false
                        onAdNotAvailable(message)
                    }
                }
            )
        } catch (e: Exception) {
            onAdNotAvailable("خطای غیرمنتظره: ${e.message}")
        }
    }

    /**
     * نمایش تبلیغ جایزه‌ای. باید از Activity صدا زده شود.
     */
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
                object : AdShowListener {
                    override fun onRewarded(completed: Boolean) {
                        Log.d(TAG, "onRewarded: completed=$completed")
                        if (completed) onRewardEarned()
                    }

                    override fun onClosed() {
                        Log.d(TAG, "Rewarded ad closed")
                        lastRewardedResponseId = null
                        _isAdReady.value = false
                        preloadRewardedVideo()
                    }

                    override fun onError(message: String) {
                        Log.e(TAG, "Show error: $message")
                        onError(message)
                    }
                }
            )
        } catch (e: Exception) {
            onError("خطا در نمایش تبلیغ: ${e.message}")
        }
    }

    /**
     * پیش‌بارگذاری بنر استاندارد
     * در SDK جدید، بنرها از طریق BannerAdViewContainer در UI نمایش داده می‌شوند.
     */
    fun preloadBanner() {
        if (!isInitialized) return
        try {
            Tapsell.requestBannerAd(
                TapsellConfig.ZONE_STANDARD_BANNER,
                object : RequestResultListener {
                    override fun onAdAvailable(adId: String) {
                        lastBannerResponseId = adId
                        Log.d(TAG, "Banner ready. adId=$adId")
                    }
                    override fun onNoAdAvailable() { Log.w(TAG, "No banner available") }
                    override fun onError(message: String) { Log.e(TAG, "Banner error: $message") }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "preloadBanner exception: ${e.message}", e)
        }
    }
}
