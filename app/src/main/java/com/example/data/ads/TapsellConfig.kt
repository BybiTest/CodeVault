package com.example.data.ads

/**
 * تنظیمات تپسل — از پنل tapsell.ir گرفته شده
 * ⚠️ اگه کلیدها رو تغییر دادی، فقط اینجا عوض کن
 */
object TapsellConfig {

  const val APP_KEY = "papdggdngktkkhloocmbmngenrjirdgrfhcrfjksqcgfatmrfrmfgmtngfqgcepmqirmhb"

  const val ZONE_REWARDED_VIDEO = "1d710cc7-5e96-46ac-a3e9-8463300333e6"
  const val ZONE_STANDARD_BANNER = "e3d5999c-5990-4e31-8ce9-642ce040a7f4"
  const val ZONE_INSTANT_BANNER = "e3d5999c-5990-4e31-8ce9-642ce040a7f4"

  val isConfigured: Boolean
    get() = APP_KEY.isNotBlank() && !APP_KEY.contains("YOUR_") &&
            ZONE_REWARDED_VIDEO.isNotBlank() &&
            ZONE_STANDARD_BANNER.isNotBlank() &&
            ZONE_INSTANT_BANNER.isNotBlank()
}
