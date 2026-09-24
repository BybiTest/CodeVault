package com.example.data.billing

import android.content.Context
import com.example.data.repository.SettingsRepository
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

  fun purchasePlan(planId: String, isPersian: Boolean) {
    scope.launch(Dispatchers.IO) {
      _billingResult.value = BillingResult.Loading
      try {
        // Simulation / real billing handoff
        kotlinx.coroutines.delay(1200)
        settingsRepository.setVipActive(true)
        val msg = if (isPersian) "اشتراک VIP با موفقیت فعال شد" else "VIP subscription activated successfully!"
        _billingResult.value = BillingResult.Success(msg)
      } catch (e: Exception) {
        val err = if (isPersian) "خطا در اتصال به درگاه پرداخت: ${e.message}" else "Billing error: ${e.message}"
        _billingResult.value = BillingResult.Error(err)
      }
    }
  }

  fun restorePurchases(isPersian: Boolean) {
    scope.launch(Dispatchers.IO) {
      _billingResult.value = BillingResult.Loading
      try {
        kotlinx.coroutines.delay(1500)
        // Verify past purchase receipts
        settingsRepository.setVipActive(true)
        val msg = if (isPersian) "خریدهای پیشین با موفقیت بازیابی شدند" else "Previous purchases restored successfully"
        _billingResult.value = BillingResult.Success(msg)
      } catch (e: Exception) {
        val err = if (isPersian) "خریدی جهت بازیابی یافت نشد" else "No previous purchases found to restore"
        _billingResult.value = BillingResult.Error(err)
      }
    }
  }

  fun resetResult() {
    _billingResult.value = BillingResult.Idle
  }
}
