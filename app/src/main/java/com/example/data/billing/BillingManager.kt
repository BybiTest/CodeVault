package com.example.data.billing

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultRegistry
import com.example.data.repository.SettingsRepository
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.callback.PurchaseCallback
import ir.cafebazaar.poolakey.callback.PurchaseQueryCallback
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.request.PurchaseRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VipPlan(
  val id: String,
  val titleFa: String,
  val titleEn: String,
  val priceFa: String,
  val priceEn: String,
  val periodFa: String,
  val periodEn: String,
  val isPopular: Boolean = false,
  val isSubscription: Boolean = true
)

sealed class BillingResult {
  data object Idle : BillingResult()
  data object Loading : BillingResult()
  data class Success(val message: String) : BillingResult()
  data class Error(val message: String) : BillingResult()
}

class BillingManager(
  private val context: Context,
  private val settingsRepository: SettingsRepository,
  private val scope: CoroutineScope
) {

  companion object {
    private const val TAG = "BillingManager"

    private const val RSA_PUBLIC_KEY =
      "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDpr/BV/39/MeA7yljz8WILCmJzPxyDmf3e/J+7yaPKhbRbqZ6aerg0Dl46fwpl1vu6JmKrdN51uDm6oAcq1ZBK4HPlVeIdNpfgJEyTcv6B0fetx9qEpQkFNW60txzgfVDTQEO1PQs1+OcTn7MXPn9qd7WcyiTMlGezZ2+aWd5T4MkJxMq8sh6UIoZKqP+a7TCVi1PLRxHwWMIG0PxUwigyoZWJTliXIEDsRuLVY9UCAwEAAQ=="

    const val SKU_VIP_MONTHLY = "challengearena_vip_monthly"
    const val SKU_VIP_YEARLY = "challengearena_vip_yearly"
    const val SKU_VIP_LIFETIME = "challengearena_vip_lifetime"
  }

  val availablePlans = listOf(
    VipPlan(
      id = SKU_VIP_MONTHLY,
      titleFa = "اشتراک ماهانه",
      titleEn = "Monthly Pass",
      priceFa = "۱۹۹,۰۰۰ ریال",
      priceEn = "199,000 IRR",
      periodFa = "هر ماه تمدید خودکار",
      periodEn = "Billed monthly",
      isSubscription = true
    ),
    VipPlan(
      id = SKU_VIP_YEARLY,
      titleFa = "اشتراک سالانه",
      titleEn = "Annual VIP",
      priceFa = "۱,۹۹۰,۰۰۰ ریال",
      priceEn = "1,990,000 IRR",
      periodFa = "۳۵٪ تخفیف ویژه",
      periodEn = "Save 35%",
      isPopular = true,
      isSubscription = true
    ),
    VipPlan(
      id = SKU_VIP_LIFETIME,
      titleFa = "اشتراک مادام‌العمر",
      titleEn = "Lifetime Access",
      priceFa = "۲,۹۹۰,۰۰۰ ریال",
      priceEn = "2,990,000 IRR",
      periodFa = "یک‌بار پرداخت برای همیشه",
      periodEn = "Pay once, yours forever",
      isSubscription = false
    )
  )

  private val _billingResult = MutableStateFlow<BillingResult>(BillingResult.Idle)
  val billingResult: StateFlow<BillingResult> = _billingResult.asStateFlow()

  private val paymentConfiguration = PaymentConfiguration(
    localSecurityCheck = SecurityCheck.Enable(rsaPublicKey = RSA_PUBLIC_KEY),
    shouldSupportSubscription = true
  )

  private val payment: Payment by lazy(LazyThreadSafetyMode.NONE) {
    Payment(context = context, config = paymentConfiguration)
  }

  private var paymentConnection: Connection? = null

  fun connect(onReady: () -> Unit = {}) {
    if (paymentConnection?.getState() == ConnectionState.Connected) {
      onReady()
      return
    }

    try {
      paymentConnection = payment.connect {
        connectionSucceed {
          Log.i(TAG, "Connected to Bazaar")
          queryPurchasesForVip()
          onReady()
        }
        connectionFailed {
          Log.e(TAG, "Connection failed")
        }
        disconnected {
          Log.w(TAG, "Disconnected from Bazaar")
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "connect exception: ${e.message}", e)
    }
  }

  fun disconnect() {
    try {
      paymentConnection?.disconnect()
    } catch (_: Exception) {}
    paymentConnection = null
  }

  fun purchasePlan(
    registry: ActivityResultRegistry,
    planId: String,
    isPersian: Boolean
  ) {
    if (paymentConnection?.getState() != ConnectionState.Connected) {
      _billingResult.value = BillingResult.Error(
        if (isPersian) "اتصال به بازار برقرار نیست" else "Not connected to Bazaar"
      )
      return
    }

    _billingResult.value = BillingResult.Loading

    val request = PurchaseRequest(
      productId = planId,
      payload = "vip_${System.currentTimeMillis()}",
      dynamicPriceToken = null
    )

    val callback: PurchaseCallback.() -> Unit = {
      purchaseFlowBegan {
        Log.d(TAG, "Purchase flow began")
      }
      failedToBeginFlow {
        Log.e(TAG, "Failed to begin flow: ${it.message}")
        _billingResult.value = BillingResult.Error(
          if (isPersian) "خطا در شروع پرداخت: ${it.message}" else "Failed: ${it.message}"
        )
      }
      purchaseSucceed { purchaseInfo ->
        Log.i(TAG, "Purchase succeed: ${purchaseInfo.productId}")
        activateVip(isPersian)
      }
      purchaseCanceled {
        _billingResult.value = BillingResult.Error(
          if (isPersian) "خرید لغو شد" else "Purchase canceled"
        )
      }
      purchaseFailed { throwable ->
        Log.e(TAG, "Purchase failed: ${throwable.message}")
        _billingResult.value = BillingResult.Error(
          if (isPersian) "خرید ناموفق: ${throwable.message}" else "Failed: ${throwable.message}"
        )
      }
    }

    try {
      val isSubscription = planId in listOf(SKU_VIP_MONTHLY, SKU_VIP_YEARLY)
      if (isSubscription) {
        payment.subscribeProduct(registry = registry, request = request, callback = callback)
      } else {
        payment.purchaseProduct(registry = registry, request = request, callback = callback)
      }
    } catch (e: Exception) {
      _billingResult.value = BillingResult.Error(
        if (isPersian) "خطای غیرمنتظره: ${e.message}" else "Unexpected: ${e.message}"
      )
    }
  }

  fun restorePurchases(isPersian: Boolean) {
    if (paymentConnection?.getState() != ConnectionState.Connected) {
      _billingResult.value = BillingResult.Error(
        if (isPersian) "اتصال به بازار برقرار نیست" else "Not connected"
      )
      return
    }
    _billingResult.value = BillingResult.Loading
    queryPurchasesForVip(isPersian)
  }

  private fun queryPurchasesForVip(isPersian: Boolean = true) {
    try {
      val callback: PurchaseQueryCallback.() -> Unit = {
        querySucceed { purchasedItems ->
          val ownedVip = purchasedItems.any {
            it.productId in listOf(SKU_VIP_MONTHLY, SKU_VIP_YEARLY, SKU_VIP_LIFETIME)
          }
          if (ownedVip) {
            activateVip(isPersian)
          } else {
            scope.launch(Dispatchers.Main) {
              settingsRepository.setVipActive(false)
            }
          }
        }
        queryFailed { throwable ->
          Log.e(TAG, "Query failed: ${throwable.message}")
        }
      }

      payment.getPurchasedProducts(callback)
      payment.getSubscribedProducts(callback)
    } catch (e: Exception) {
      Log.e(TAG, "query exception: ${e.message}", e)
    }
  }

  private fun activateVip(isPersian: Boolean) {
    scope.launch(Dispatchers.Main) {
      settingsRepository.setVipActive(true)
      _billingResult.value = BillingResult.Success(
        if (isPersian) "اشتراک VIP با موفقیت فعال شد" else "VIP activated successfully!"
      )
    }
  }

  fun resetResult() {
    _billingResult.value = BillingResult.Idle
  }
}
