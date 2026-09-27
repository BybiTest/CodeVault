package com.example.ui.screens.vip

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.billing.BillingResult
import com.example.data.billing.VipPlan
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppLanguage
import com.example.ui.localization.LocalAppStrings
import com.example.ui.navigation.Screen
import com.example.ui.theme.VipGold

@Composable
fun VipScreen(
  isVip: Boolean,
  onBack: () -> Unit,
  onNavigate: (String) -> Unit,
  onConnectBilling: (Activity) -> Unit = {}
) {
  val strings = LocalAppStrings.current
  val context = LocalContext.current
  val activity = context as? Activity

  LaunchedEffect(Unit) {
    if (activity != null) onConnectBilling(activity)
  }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.vipTitle,
        onBackClick = onBack
      )
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(20.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(20.dp))
          .border(2.dp, VipGold, RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .background(
              Brush.verticalGradient(
                colors = listOf(VipGold.copy(alpha = 0.25f), Color.Transparent)
              )
            )
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
              shape = CircleShape,
              color = VipGold,
              modifier = Modifier.size(64.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color.Black, modifier = Modifier.size(38.dp))
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
              text = if (isVip) strings.vipActive else "CodeVault VIP Gold",
              style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
              color = VipGold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
              text = if (isVip) "شما به تمام امکانات پیشرفته و نامحدود دسترسی دارید" else "دسترسی نامحدود و ابزارهای توسعه حرفه‌ای",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }

      Text(
        text = "مزایای عضویت طلایی:",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      VipFeatureRow(icon = Icons.Default.AllInclusive, title = strings.vipFeature1, desc = "بدون هیچ‌گونه محدودیت در تعداد فایل‌ها و پروژه‌ها")
      VipFeatureRow(icon = Icons.Default.HistoryEdu, title = strings.vipFeature2, desc = "ثبت خودکار تغییرات کدها و امکان بازیابی نسخه‌های پیشین")
      VipFeatureRow(icon = Icons.Default.FolderZip, title = strings.vipFeature3, desc = "فشرده‌سازی پرسرعت و امکان نگهداری چندین فایل پشتیبان همزمان")
      VipFeatureRow(icon = Icons.Default.Block, title = strings.vipFeature4, desc = "محیط کاری کاملاً پاک و بدون هیچ بنر یا تبلیغ درون‌برنامه‌ای")
      VipFeatureRow(icon = Icons.Default.ColorLens, title = strings.vipFeature5, desc = "دسترسی به تمامی تم‌ها از جمله AMOLED، Cyber و Midnight")
      VipFeatureRow(icon = Icons.Default.FindInPage, title = strings.vipFeature6, desc = "جستجوی پیشرفته بر اساس عبارات باقاعده در تمام پروژه‌ها")

      Spacer(modifier = Modifier.height(10.dp))

      if (!isVip) {
        Button(
          onClick = { onNavigate(Screen.VipPurchase.route) },
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .testTag("vip_upgrade_button"),
          colors = ButtonDefaults.buttonColors(containerColor = VipGold, contentColor = Color.Black),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(Icons.Default.WorkspacePremium, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(strings.upgradeToVip, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        OutlinedButton(
          onClick = { onNavigate(Screen.RestorePurchase.route) },
          modifier = Modifier.fillMaxWidth().height(48.dp),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(strings.restorePurchase)
        }
      }
    }
  }
}

@Composable
private fun VipFeatureRow(icon: ImageVector, title: String, desc: String) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        color = VipGold.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.size(40.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(icon, contentDescription = null, tint = VipGold, modifier = Modifier.size(22.dp))
        }
      }
      Spacer(modifier = Modifier.width(14.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
  }
}

@Composable
fun VipPurchaseScreen(
  plans: List<VipPlan>,
  billingResult: BillingResult,
  onPurchasePlan: (String) -> Unit,
  onBack: () -> Unit
) {
  val strings = LocalAppStrings.current
  val isFa = LocalAppLanguage.current == com.example.ui.localization.AppLanguage.FA
  val context = LocalContext.current

  LaunchedEffect(billingResult) {
    when (billingResult) {
      is BillingResult.Success -> {
        Toast.makeText(context, billingResult.message, Toast.LENGTH_LONG).show()
        onBack()
      }
      is BillingResult.Error -> {
        Toast.makeText(context, billingResult.message, Toast.LENGTH_LONG).show()
      }
      else -> {}
    }
  }

  Scaffold(
    topBar = {
      CodeVaultTopBar(title = strings.upgradeToVip, onBackClick = onBack)
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(20.dp)
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "پلن مورد نظر خود را انتخاب کنید:",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      plans.forEach { plan ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPurchasePlan(plan.id) }
            .border(
              width = if (plan.isPopular) 2.dp else 1.dp,
              color = if (plan.isPopular) VipGold else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
              shape = RoundedCornerShape(16.dp)
            ),
          colors = CardDefaults.cardColors(
            containerColor = if (plan.isPopular) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
          )
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            if (plan.isPopular) {
              Surface(color = VipGold, shape = RoundedCornerShape(6.dp)) {
                Text(
                  text = "پیشنهاد ویژه (محبوب‌ترین)",
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.Black
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = if (isFa) plan.titleFa else plan.titleEn,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = if (isFa) plan.priceFa else plan.priceEn,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                color = if (plan.isPopular) VipGold else MaterialTheme.colorScheme.primary
              )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
              text = if (isFa) plan.periodFa else plan.periodEn,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            Button(
              onClick = { onPurchasePlan(plan.id) },
              modifier = Modifier.fillMaxWidth(),
              colors = ButtonDefaults.buttonColors(
                containerColor = if (plan.isPopular) VipGold else MaterialTheme.colorScheme.primary,
                contentColor = if (plan.isPopular) Color.Black else MaterialTheme.colorScheme.onPrimary
              )
            ) {
              Text("خرید و فعال‌سازی")
            }
          }
        }
      }

      if (billingResult is BillingResult.Loading) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
          CircularProgressIndicator(color = VipGold)
        }
      }
    }
  }
}

@Composable
fun RestorePurchaseScreen(
  billingResult: BillingResult,
  onRestore: () -> Unit,
  onBack: () -> Unit
) {
  val strings = LocalAppStrings.current
  val context = LocalContext.current

  LaunchedEffect(billingResult) {
    when (billingResult) {
      is BillingResult.Success -> {
        Toast.makeText(context, billingResult.message, Toast.LENGTH_LONG).show()
        onBack()
      }
      is BillingResult.Error -> {
        Toast.makeText(context, billingResult.message, Toast.LENGTH_LONG).show()
      }
      else -> {}
    }
  }

  Scaffold(
    topBar = {
      CodeVaultTopBar(title = strings.restorePurchase, onBackClick = onBack)
    }
  ) { paddingValues ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      Icon(
        imageVector = Icons.Default.Restore,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(64.dp)
      )

      Text(
        text = "بازیابی خریدهای پیشین",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = "اگر قبلاً اشتراک VIP را روی این دستگاه یا حساب کاربری خریداری کرده‌اید، با زدن دکمه زیر وضعیت خرید شما استعلام و اشتراک مجدداً فعال می‌شود.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      Button(
        onClick = onRestore,
        enabled = billingResult !is BillingResult.Loading,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("execute_restore_purchase_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (billingResult is BillingResult.Loading) {
          CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
        } else {
          Text(strings.restorePurchase)
        }
      }
    }
  }
}
