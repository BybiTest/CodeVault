package com.example.data.ads

/**
 * تنظیمات تپسل — از پنل tapsell.ir گرفته شده
 * ⚠️ اگه کلیدها رو تغییر دادی، فقط اینجا عوض کن
 */
object TapsellConfig {

  const val APP_KEY = "papdggdngktkkhloocmbmngenrjirdgrfhcrfjksqcgfatmrfrmfgmtngfqgcepmqirmhb"

  const val ZONE_REWARDED_VIDEO = "6ab553dae237e15c69fbac2d"
  const val ZONE_STANDARD_BANNER = "6ab553eff9c3d5797ba49cda"
  const val ZONE_INSTANT_BANNER = "6ab55455f9c3d5797ba49cdb"

  val isConfigured: Boolean
    get() = APP_KEY.isNotBlank() && !APP_KEY.contains("YOUR_") &&
            ZONE_REWARDED_VIDEO.isNotBlank() &&
            ZONE_STANDARD_BANNER.isNotBlank() &&
            ZONE_INSTANT_BANNER.isNotBlank()
}
