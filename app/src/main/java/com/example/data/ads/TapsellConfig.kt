package com.example.data.ads

/**
 * Tapsell Advertising Configuration.
 * Replace with your actual credentials from the Tapsell dashboard before publishing to production.
 */
object TapsellConfig {
  // App Key provided in the Tapsell Dashboard
  var appKey: String = "YOUR_TAPSELL_APP_KEY"

  // Standard banner ad zone ID (shown in Dashboard, Projects list, etc. Never in Code Editor!)
  var bannerZoneId: String = "YOUR_BANNER_ZONE_ID"

  // Rewarded video zone ID
  var rewardedZoneId: String = "YOUR_REWARDED_ZONE_ID"

  val isConfigured: Boolean
    get() = appKey != "YOUR_TAPSELL_APP_KEY" && appKey.isNotBlank()
}
