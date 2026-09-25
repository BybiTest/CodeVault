package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.data.repository.SettingsRepository
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
import ir.cafebazaar.poolakey.PurchaseResult
import ir.cafebazaar.poolakey.PurchaseQueryResult
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

    // ⚠️ این کلید توی چت ثبت شده. حتماً از پنل بازار کلید جدید بگیر و اینجا جایگزین کن.
    private const val RSA_PUBLIC_KEY =
      "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDpr/BV/39/MeA7yljz8WILCmJzPxyDmf3e/J+7yaPKhbRbqZ6aerg0Dl46fwpl1vu6JmKrdN51uDm6oAcq1ZBK4HPlVeIdNpfgJEyTcv6B0fetx9qEpQkFNW60txzgfVDTQEO1PQs1+OcTn7MXPn9qd7WcyiTMlGezZ2+aWd5T4MkJxMq8sh6UIoZKqP+a7TCVi1PLRxHwWMIG0PxUwigyoZWJTliXIEDsRuLVY9UCAwEAAQ=="

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

  @Volatile private var isConnected = false

  fun connect(activity: Activity, onReady: () -> Unit = {}) {
    if (isConnected) { onReady(); return }

    try {
      val config = PaymentConfiguration(
        localSecurityCheck = SecurityCheck.Disable,
        remoteSecurityCheck = SecurityCheck.Enable(RSA_PUBLIC_KEY)
      )

      payment = Payment(context, config)
      connection = payment!!.connect { state ->
        when (state) {
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
            Log.e(TAG, "Connection failed: ${state.message}")
          }
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "connect exception: ${e.message}", e)
    }
  }

  fun disconnect() {
    try {
      connection?.disconnect()
    } catch (_: Exception) {}
    connection = null
    payment = null
    isConnected = false
  }

  fun purchasePlan(activity: Activity, planId: String, isPersian: Boolean) {
    val p = payment
    if (p == null || !isConnected) {
      _billingResult.value = BillingResult.Error(
        if (isPersian) "اتصال به بازار برقرار نیست" else "Not connected to Bazaar"
      )
      return
    }

    _billingResult.value = BillingResult.Loading

    try {
      val request = PurchaseRequest(
        productId = planId,
        payload = "vip_${System.currentTimeMillis()}"
      )

      p.purchaseProduct(activity, request) { result ->
        when (result) {
          is PurchaseResult.Succeed -> {
            Log.i(TAG, "Purchase succeed: ${result.purchaseInfo.productId}")
            activateVip(isPersian)
          }
          is PurchaseResult.Failed -> {
            Log.e(TAG, "Purchase failed: ${result.message}")
            _billingResult.value = BillingResult.Error(
              if (isPersian) "خرید ناموفق: ${result.message}" else "Purchase failed: ${result.message}"
            )
          }
          is PurchaseResult.Canceled -> {
            _billingResult.value = BillingResult.Error(
              if (isPersian) "خرید لغو شد" else "Purchase canceled"
            )
          }
        }
      }
    } catch (e: Exception) {
      _billingResult.value = BillingResult.Error(
        if (isPersian) "خطای غیرمنتظره: ${e.message}" else "Unexpected error: ${e.message}"
      )
    }
  }

  fun restorePurchases(activity: Activity, isPersian: Boolean) {
    val p = payment
    if (p == null || !isConnected) {
      _billingResult.value = BillingResult.Error(
        if (isPersian) "اتصال به بازار برقرار نیست" else "Not connected to Bazaar"
      )
      return
    }

    _billingResult.value = BillingResult.Loading
    queryPurchasesForVip(isPersian)
  }

  private fun queryPurchasesForVip(isPersian: Boolean = true) {
    val p = payment ?: return
    try {
      p.getPurchasedProducts { result ->
        when (result) {
          is PurchaseQueryResult.Succeed -> {
            val ownedVip = result.purchasedProducts.any {
              it.productId in listOf(SKU_VIP_MONTHLY, SKU_VIP_YEARLY, SKU_VIP_LIFETIME)
            }
            if (ownedVip) {
              activateVip(isPersian)
            } else {
              scope.launch(Dispatchers.Main) {
                settingsRepository.setVipActive(false)
                _billingResult.value = BillingResult.Error(
                  if (isPersian) "خریدی یافت نشد" else "No purchase found"
                )
              }
            }
          }
          is PurchaseQueryResult.Failed -> {
            scope.launch(Dispatchers.Main) {
              _billingResult.value = BillingResult.Error(
                if (isPersian) "خطا در استعلام: ${result.message}" else "Query failed: ${result.message}"
              )
            }
          }
        }
      }
    } catch (e: Exception) {
      _billingResult.value = BillingResult.Error(
        if (isPersian) "خطای غیرمنتظره: ${e.message}" else "Unexpected error: ${e.message}"
      )
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
