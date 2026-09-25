package com.example.ui.components

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ads.TapsellConfig
import com.example.ui.theme.VipGold
import ir.tapsell.plus.AdHolder
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType

/**
 * بنر تبلیغاتی تپسل با دکمه‌ی حذف تبلیغ
 * @param isVip اگه کاربر VIP باشه، هیچی نشون داده نمیشه
 * @param onUpgradeClick کلیک روی دکمه‌ی VIP
 */
@Composable
fun AdBannerView(
  isVip: Boolean,
  onUpgradeClick: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  if (isVip) return

  val context = LocalContext.current
  val activity = context as? Activity ?: return

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column {
      // بنر تبلیغاتی تپسل
      AndroidView(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        factory = { ctx ->
          FrameLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
            )
          }
        },
        update = { frameLayout ->
          if (frameLayout.tag != TapsellConfig.ZONE_STANDARD_BANNER) {
            frameLayout.tag = TapsellConfig.ZONE_STANDARD_BANNER
            frameLayout.removeAllViews()

            try {
              val adHolder = AdHolder(frameLayout)

              TapsellPlus.requestStandardBannerAd(
                activity,
                TapsellConfig.ZONE_STANDARD_BANNER,
                TapsellPlusBannerType.BANNER_320x50,
                object : AdRequestCallback() {
                  override fun response(responseId: String?) {
                    responseId?.let {
                      TapsellPlus.showStandardBannerAd(
                        activity,
                        it,
                        TapsellPlusBannerType.BANNER_320x50,
                        adHolder
                      )
                    }
                  }

                  override fun error(message: String?) {
                    // اگه خطا داد، چیزی نشون داده نمیشه
                  }
                }
              )
            } catch (e: Exception) {
              // اگه خطا داد، چیزی نشون داده نمیشه
            }
          }
        }
      )

      // دکمه‌ی حذف تبلیغات با خرید VIP
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onUpgradeClick() }
          .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.WorkspacePremium,
          contentDescription = null,
          tint = VipGold,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "حذف تمام تبلیغات با خرید اشتراک VIP",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "VIP",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = VipGold
          )
        }
        Icon(
          imageVector = Icons.Default.Campaign,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(22.dp)
        )
      }
    }
  }
}
