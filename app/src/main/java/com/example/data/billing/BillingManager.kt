cat > app/src/main/java/com/example/data/billing/BillingManager.kt << 'ENDOFFILE'
package com.example.data.billing

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.data.repository.SettingsRepository
import ir.cafebazaar.poolakey.Connection
import ir.cafebazaar.poolakey.ConnectionState
import ir.cafebazaar.poolakey.Payment
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
    private const val RSA_PUBLIC_KEY = "MIHNMA0GCSqGSIb3DQEBAQUAA4G7ADCBtwKBrwDpr/BV/39/MeA7yljz8WILCmJzPxyDmf3e/J+7yaPKhbRbqZ6aerg0Dl46fwpl1vu6JmKrdN51uDm6oAcq1ZBK4HPlVeIdNpfgJEyTcv6B0fetx9qEpQkFNW60txzgfVDTQEO1PQs1+OcTn7MXPn9qd7WcyiTMlGezZ2+aWd5T4MkJxMq8sh6UIoZKqP+a7TCVi1PLRxHwWMIG0PxUwigyoZWJTliXIEDsRuLVY9UCAwEAAQ=="
    const val SKU_VIP_MONTHLY = "challengearena_vip_monthly"
    const val SKU_VIP_YEARLY = "challengearena_vip_yearly"
    const val SKU_VIP_LIFETIME = "challengearena_vip_lifetime"
  }

  val availablePlans = listOf(
    VipPlan(id = SKU_VIP_MONTHLY, titleFa = "اشتراک ماهانه", titleEn = "Monthly Pass", priceFa = "۱۹۹,۰۰۰ ریال", priceEn = "199,000 IRR", periodFa = "هر ماه تمدید خودکار", periodEn = "Billed monthly"),
    VipPlan(id = SKU_VIP_YEARLY, titleFa = "اشتراک سالانه", titleEn = "Annual VIP", priceFa = "۱,۹۹۰,۰۰۰ ریال", priceEn = "1,990,000 IRR", periodFa = "۳۵٪ تخفیف ویژه", periodEn = "Save 35%", isPopular = true),
    VipPlan(id = SKU_VIP_LIFETIME, titleFa = "اشتراک مادام‌العمر", titleEn = "Lifetime Access", priceFa = "۲,۹۹۰,۰۰۰ ریال", priceEn = "2,990,000 IRR", periodFa = "یک‌بار پرداخت برای همیشه", periodEn = "Pay once, yours forever")
  )

  private val _billingResult = MutableStateFlow<BillingResult>(BillingResult.Idle)
  val billingResult: StateFlow<BillingResult> = _billingResult.asStateFlow()

  private var payment: Payment? = null
  private var connection: Connection? = null

  @Volatile private var isConnected = false

  fun connect(activity: Activity, onReady: () -> Unit = {}) {
    if (isConnected) { onReady(); return }
    try {
      val config = PaymentConfiguration(localSecurityCheck = SecurityCheck.Disable, remoteSecurityCheck = SecurityCheck.Enable(RSA_PUBLIC_KEY))
      payment = Payment(context, config)
      connection = payment!!.connect { state ->
        when (state) {
          is ConnectionState.Connected -> {
            isConnected = true
            Log.i(TAG, "Connected to Bazaar")
            queryPurchasesForVip()
            onReady()
          }
          is ConnectionState.Disconnected -> { isConnected = false; Log.w(TAG, "Disconnected") }
          is ConnectionState.Failed -> { isConnected = false; Log.e(TAG, "Failed: ${state.message}") }
        }
      }
    } catch (e: Exception) { Log.e(TAG, "connect exception: ${e.message}", e) }
  }

  fun disconnect() {
    try { connection?.disconnect() } catch (_: Exception) {}
    connection = null; payment = null; isConnected = false
  }

  fun purchasePlan(activity: Activity, planId: String, isPersian: Boolean) {
    val p = payment
    if (p == null || !isConnected) {
      _billingResult.value = BillingResult.Error(if (isPersian) "اتصال به بازار برقرار نیست" else "Not connected")
      return
    }
    _billingResult.value = BillingResult.Loading
    try {
      val request = PurchaseRequest(productId = planId, payload = "vip_${System.currentTimeMillis()}")
      p.purchaseProduct(activity, request) { result ->
        when (result) {
          is ir.cafebazaar.poolakey.PurchaseResult.Succeed -> activateVip(isPersian)
          is ir.cafebazaar.poolakey.PurchaseResult.Failed -> _billingResult.value = BillingResult.Error("خرید ناموفق: ${result.message}")
          is ir.cafebazaar.poolakey.PurchaseResult.Canceled -> _billingResult.value = BillingResult.Error("خرید لغو شد")
        }
      }
    } catch (e: Exception) {
      _billingResult.value = BillingResult.Error("خطا: ${e.message}")
    }
  }

  fun restorePurchases(activity: Activity, isPersian: Boolean) {
    val p = payment
    if (p == null || !isConnected) {
      _billingResult.value = BillingResult.Error("اتصال به بازار برقرار نیست")
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
          is ir.cafebazaar.poolakey.PurchaseQueryResult.Succeed -> {
            val ownedVip = result.purchasedProducts.any { it.productId in listOf(SKU_VIP_MONTHLY, SKU_VIP_YEARLY, SKU_VIP_LIFETIME) }
            if (ownedVip) activateVip(isPersian)
            else scope.launch(Dispatchers.Main) {
              settingsRepository.setVipActive(false)
              _billingResult.value = BillingResult.Error("خریدی یافت نشد")
            }
          }
          is ir.cafebazaar.poolakey.PurchaseQueryResult.Failed -> scope.launch(Dispatchers.Main) {
            _billingResult.value = BillingResult.Error("خطا: ${result.message}")
          }
        }
      }
    } catch (e: Exception) {
      _billingResult.value = BillingResult.Error("خطا: ${e.message}")
    }
  }

  private fun activateVip(isPersian: Boolean) {
    scope.launch(Dispatchers.Main) {
      settingsRepository.setVipActive(true)
      _billingResult.value = BillingResult.Success(if (isPersian) "اشتراک VIP فعال شد" else "VIP activated")
    }
  }

  fun resetResult() { _billingResult.value = BillingResult.Idle }
}
ENDOFFILE
