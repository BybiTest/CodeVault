package com.example.ui.screens.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppStrings
import com.example.ui.theme.MetallicCyanPrimary
import com.example.ui.theme.MetallicSilver

@Composable
fun AboutScreen(
  onBack: () -> Unit
) {
  val strings = LocalAppStrings.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.aboutApp,
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
      verticalArrangement = Arrangement.spacedBy(18.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      Image(
        painter = painterResource(id = R.drawable.codevault_icon_1790264756402),
        contentDescription = strings.appName,
        modifier = Modifier
          .size(88.dp)
          .clip(RoundedCornerShape(20.dp))
      )

      Text(
        text = strings.appName,
        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = strings.appSubtitle,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary
      )

      Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
      ) {
        Text(
          text = strings.appVersion,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "توسعه‌دهنده و طراح:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = strings.appDeveloper,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Text(
            text = strings.aboutDesc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }

      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("تکنولوژی‌های پایه:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
          Text("• معماری بومی Android Native با Kotlin", style = MaterialTheme.typography.bodySmall)
          Text("• رابط کاربری مدرن Jetpack Compose & Material 3", style = MaterialTheme.typography.bodySmall)
          Text("• پایگاه داده محلی SQLite با Room Database", style = MaterialTheme.typography.bodySmall)
          Text("• ذخیره‌سازی ترتیبی و بومی Local-first", style = MaterialTheme.typography.bodySmall)
          Text("• استخراج و فشرده‌سازی استاندارد ZIP", style = MaterialTheme.typography.bodySmall)
        }
      }

      Text(
        text = strings.copyright,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
fun PrivacyPolicyScreen(
  onBack: () -> Unit
) {
  val strings = LocalAppStrings.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.privacyPolicy,
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "حفظ کامل حریم خصوصی کدهای شما",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          PolicySection(
            title = "۱. معماری ۱۰۰٪ محلی (Local-first):",
            content = "تمام پروژه‌ها، پوشه‌ها، فایل‌های کد، نسخه‌های تاریخی و پشتیبان‌ها به طور کامل روی حافظه داخلی دستگاه شما نگهداری می‌شوند. کدهای منبع شما هرگز بدون درخواست صریح شما به هیچ سرور خارجی منتقل نمی‌شوند."
          )
          PolicySection(
            title = "۲. امنیت و دسترسی‌ها:",
            content = "CodeVault تنها از حداقل دسترسی‌های ضروری اندروید (مانند دسترسی به اینترنت جهت استعلام خریدهای اشتراک و تبلیغات در صورت عدم عضویت VIP) استفاده می‌کند. هیچ دسترسی مخفی یا تله‌متری روی محتوای کدهای شما وجود ندارد."
          )
          PolicySection(
            title = "۳. هوش مصنوعی:",
            content = "برنامه CodeVault کاملاً مستقل، آفلاین و فاقد هرگونه بات یا دستیار هوش مصنوعی خارجی است که داده‌های کدنویسی شما را بخواند."
          )
        }
      }
    }
  }
}

@Composable
fun TermsOfServiceScreen(
  onBack: () -> Unit
) {
  val strings = LocalAppStrings.current

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.termsOfService,
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
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "قوانین و شرایط استفاده از CodeVault",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          PolicySection(
            title = "۱. مالکیت کدها:",
            content = "تمام کدهای ایجادشده یا واردشده توسط کاربر به طور کامل به خود کاربر تعلق دارد و توسعه‌دهنده برنامه هیچ‌گونه ادعای مالکیتی نسبت به فایل‌های کاربران ندارد."
          )
          PolicySection(
            title = "۲. پشتیبان‌گیری:",
            content = "با وجود تعبیه سیستم سطل زباله و پشتیبان‌گیری محلی در برنامه، مسئولیت نگهداری و ذخیره فایل‌های خروجی ZIP در فضاهای امن بر عهده کاربر است."
          )
          PolicySection(
            title = "۳. اشتراک ویژه VIP:",
            content = "اشتراک‌های ماهانه، سالانه و مادام‌العمر امکاناتی از قبیل حذف تبلیغات، تاریخچه نسخه‌ها و پروژه‌های نامحدود را فراهم می‌کنند و از طریق دکمه بازیابی خرید روی دستگاه‌های کاربر قابل فعال‌سازی مجدد هستند."
          )
        }
      }
    }
  }
}

@Composable
private fun PolicySection(title: String, content: String) {
  Column {
    Text(
      text = title,
      style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
      color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
      text = content,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}
