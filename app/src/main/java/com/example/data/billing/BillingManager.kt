package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.data.repository.SettingsRepository
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.config.PaymentConfiguration
import ir.cafebazaar.poolakey.config.SecurityCheck
import ir.cafebazaar.poolakey.entity.PurchaseInfo
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
  val isPopular: Boolean = false
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

    // RSA Public Key از پنل کافه‌بازار (برای verify رسید خرید)
    // ⚠️ بهتره این مقدار رو از .env یا BuildConfig بخونی، نه هاردکد
    private const val RSA_PUBLIC_KEY =
      "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDpr/BV/39/MeA7yljz8WILCmJzPxyDmf3e/J+7yaPKhbRbqZ6aerg0Dl46fwpl1vu6JmKrdN51uDm6oAcq1ZBK4HPlVeIdNpfgJEyTcv6B0fetx9qEpQkFNW60txzgfVDTQEO1PQs1+OcTn7MXPn9qd7WcyiTMlGezZ2+aWd5T4MkJxMq8sh6UIoZKqP+a7TCVi1PLRxHwWMIG0PxUwigyoZWJTliXIEDsRuLVY9UCAwEAAQ=="

    // Product IDs — باید دقیقاً مطابق پنل بازار باشن
    const val SKU_VIP_MONTHLY = "codevault_vip_monthly"
    const val SKU_VIP_YEARLY = "codevault_vip_yearly"
    const val SKU_VIP_LIFETIME = "codevault_vip_lifetime"
  }

  val availablePlans = listOf(
    VipPlan(
      id = SKU_VIP_MONTHLY,
      titleFa = "اشتراک ماهانه",
      titleEn = "Monthly Pass",
      priceFa = "۴۹,۰۰۰ تومان",
      priceEn = "$1.99",
      periodFa = "هر ماه تمدید خودکار",
      periodEn = "Billed monthly"
    ),
    VipPlan(
      id = SKU_VIP_YEARLY,
      titleFa = "اشتراک سالانه",
      titleEn = "Annual VIP",
      priceFa = "۳۸۹,۰۰۰ تومان",
      priceEn = "$14.99",
      periodFa = "۳۵٪ تخفیف ویژه",
      periodEn = "Save 35%",
      isPopular = true
    ),
    VipPlan(
      id = SKU_VIP_LIFETIME,
      titleFa = "اشتراک مادام‌العمر",
      titleEn = "Lifetime Access",
      priceFa = "۶۹۰,۰۰۰ تومان",
      priceEn = "$29.99",
      periodFa = "یک‌بار پرداخت برای همیشه",
      periodEn = "Pay once, yours forever"
    )
  )

  private val _billingResult = MutableStateFlow<BillingResult>(BillingResult.Idle)
  val billingResult: StateFlow<BillingResult> = _billingResult.asStateFlow()

  private var payment: Payment? = null
  private var connection: Connection? = null
  private var isConnected = false

  /**
   * اتصال به سرویس پرداخت بازار. باید از Activity صدا زده بشه.
   */
  fun connect(activity: Activity, onReady: () -> Unit = {}) {
    if (isConnected) {
      onReady()
      return
    }

    val config = PaymentConfiguration(
      localSecurityCheck = SecurityCheck.Disable,
      remoteSecurityCheck = SecurityCheck.Enable(RSA_PUBLIC_KEY)
    )

    payment = Payment(context, config)
    connection = payment!!.connect {
      when (it) {
        is ConnectionState.Connected -> {
          isConnected = true
          Log.i(TAG, "Connected to Bazaar")
          queryPurchasesForVip()
          onReady()
        }
        is ConnectionState.Disconnected -> {
          isConnected = false
          Log.w(TAG, "Disconnected from Bazaar")
        }
        is ConnectionState.Failed -> {
          isConnected = false
          Log.e(TAG, "Connection failed: ${it.message}")
        }
      }
    }
  }

  fun disconnect() {
    connection?.disconnect()
    connection = null
    payment = null
    isConnected = false
  }

  /**
   * خرید یک پلن VIP
   */
  fun purchasePlan(activity: Activity, planId: String, isPersian: Boolean) {
    val p = payment
    if (p == null || !isConnected) {
      scope.launch {
        _billingResult.value = BillingResult.Error(
          if (isPersian) "اتصال به بازار برقرار نیست" else "Not connected to Bazaar"
        )
      }
      return
    }

    scope.launch(Dispatchers.Main) {
      _billingResult.value = BillingResult.Loading
    }

    val request = PurchaseRequest(
      productId = planId,
      payload = "vip_${System.currentTimeMillis()}"
    )

    p.purchaseProduct(activity, request) {
      when (it) {
        is ir.cafebazaar.poolakey.PurchaseResult.Succeed -> {
          val purchaseInfo = it.purchaseInfo
          Log.i(TAG, "Purchase succeed: ${purchaseInfo.productId}")
          // بررسی اینکه آیا محصول فعلاً مالکیت داره
          verifyAndActivateVip(purchaseInfo, isPersian)
        }
        is ir.cafebazaar.poolakey.PurchaseResult.Failed -> {
          Log.e(TAG, "Purchase failed: ${it.message}")
          scope.launch(Dispatchers.Main) {
            _billingResult.value = BillingResult.Error(
              if (isPersian) "خرید ناموفق: ${it.message}" else "Purchase failed: ${it.message}"
            )
          }
        }
        is ir.cafebazaar.poolakey.PurchaseResult.Canceled -> {
          scope.launch(Dispatchers.Main) {
            _billingResult.value = BillingResult.Error(
              if (isPersian) "خرید لغو شد" else "Purchase canceled"
            )
          }
        }
      }
    }
  }

  /**
   * بازیابی خریدهای قبلی
   */
  fun restorePurchases(activity: Activity, isPersian: Boolean) {
    val p = payment
    if (p == null || !isConnected) {
      scope.launch {
        _billingResult.value = BillingResult.Error(
          if (isPersian) "اتصال به بازار برقرار نیست" else "Not connected to Bazaar"
        )
      }
      return
    }

    scope.launch(Dispatchers.Main) {
      _billingResult.value = BillingResult.Loading
    }

    queryPurchasesForVip(customMessage = isPersian)
  }

  /**
   * کوئری خریدهای فعلی و فعال‌سازی VIP در صورت وجود
   */
  private fun queryPurchasesForVip(customMessage: Boolean = true) {
    val p = payment ?: return

    p.getPurchasedProducts { result ->
      when (result) {
        is ir.cafebazaar.poolakey.PurchaseQueryResult.Succeed -> {
          val purchases = result.purchasedProducts
          val ownedVip = purchases.any { it.productId in listOf(SKU_VIP_MONTHLY, SKU_VIP_YEARLY, SKU_VIP_LIFETIME) }

          if (ownedVip) {
            scope.launch(Dispatchers.Main) {
              settingsRepository.setVipActive(true)
              _billingResult.value = BillingResult.Success(
                if (customMessage) "اشتراک VIP فعال است" else "VIP is active"
              )
            }
          } else {
            scope.launch(Dispatchers.Main) {
              settingsRepository.setVipActive(false)
              _billingResult.value = BillingResult.Error(
                if (customMessage) "خریدی یافت نشد" else "No purchase found"
              )
            }
          }
        }
        is ir.cafebazaar.poolakey.PurchaseQueryResult.Failed -> {
          scope.launch(Dispatchers.Main) {
            _billingResult.value = BillingResult.Error(
              if (customMessage) "خطا در کوئری: ${result.message}" else "Query failed: ${result.message}"
            )
          }
        }
      }
    }
  }

  /**
   * مصرف (consume) خرید — برای اشتراک‌های غیرتکرارشونده لازمه
   */
  private fun verifyAndActivateVip(purchaseInfo: PurchaseInfo, isPersian: Boolean) {
    scope.launch(Dispatchers.Main) {
      settingsRepository.setVipActive(true)
      _billingResult.value = BillingResult.Success(
        if (isPersian) "اشتراک VIP با موفقیت فعال شد" else "VIP activated successfully!"
      )
    }
    // برای اشتراک‌های مادام‌العمر، لازم نیست consume کنیم
    // اگه محصول "مصرفی" داری، از payment.consumeProduct استفاده کن
  }

  fun resetResult() {
    _billingResult.value = BillingResult.Idle
  }
}
