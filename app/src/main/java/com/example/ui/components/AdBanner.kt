package com.example.ui.components

import android.app.Activity
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.ads.TapsellConfig
import ir.tapsell.plus.AdHolder
import ir.tapsell.plus.AdRequestCallback
import ir.tapsell.plus.TapsellPlus
import ir.tapsell.plus.TapsellPlusBannerType

@Composable
fun AdBanner(
  isVip: Boolean,
  modifier: Modifier = Modifier
) {
  // اگه کاربر VIP باشه، تبلیغ نشون داده نمیشه
  if (isVip) return

  val context = LocalContext.current
  val activity = context as? Activity ?: return

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surfaceVariant
  ) {
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
                  // خطا رو نادیده بگیر
                }
              }
            )
          } catch (e: Exception) {
            // خطا رو نادیده بگیر
          }
        }
      }
    )
  }
}
