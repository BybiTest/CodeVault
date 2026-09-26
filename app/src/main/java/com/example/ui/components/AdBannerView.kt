package com.example.ui.components

import android.view.ViewGroup
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
import ir.tapsell.mediation.ad.views.bnr.BannerAdViewContainer

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

  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 6.dp)
      .clip(RoundedCornerShape(14.dp)),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    shape = RoundedCornerShape(14.dp)
  ) {
    Column {
      // بنر تبلیغاتی تپسل (Mediation SDK)
      AndroidView(
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        factory = { ctx ->
          BannerAdViewContainer(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT
            )
          }
        },
        update = { bannerView ->
          try {
            bannerView.loadAd(TapsellConfig.ZONE_STANDARD_BANNER)
          } catch (e: Exception) {
            // اگه خطا داد، چیزی نشون داده نمیشه
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
