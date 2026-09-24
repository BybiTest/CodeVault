# CodeVault — مدیریت حرفه‌ای پروژه و کد

CodeVault یک اپلیکیشن پیشرفته، پایدار و محلی (Local-First) برای مدیریت پروژه‌ها و ویرایش حرفه‌ای کدها در سیستم‌عامل اندروید است که با زبان **Kotlin** و فریم‌ورک مدرن **Jetpack Compose** بر پایه استانداردهای Material 3 توسعه یافته است.

## ویژگی‌های کلیدی
- **۳۰ صفحه مستقل و ماژولار** با ناوبری رسمی و منوی یکپارچه.
- **ویرایشگر کد حرفه‌ای (Code Editor)**: هایلایت هوشمند (Syntax Highlighting)، کنترل تغییرات (Undo / Redo)، جستجو و جایگزینی (Find & Replace)، پرش به شماره خط، و دکمه اختصاصی **«کپی کل کد»** بدون دستکاری در خطوط و فاصله‌ها.
- **پایگاه داده قدرتمند**: استفاده از Room Database و KSP برای سرعت و پایداری بالا.
- **سیستم فایل واقعی**: ذخیره‌سازی مستقیم روی دیسک دستگاه با پشتیبانی از پوشه‌ها و زیرپوشه‌های نامحدود.
- **ورودی و خروجی ZIP و پشتیبان‌گیری**: قابلیت Export/Import کامل به صورت فایل فشرده با حفظ دقیق ساختار.
- **سطل زباله و تاریخچه نسخه‌ها**: بازیابی امن فایل‌های حذف‌شده و بازگردانی نسخه‌های پیشین کد.
- **طراحی دو زبانه و ۶ تم اختصاصی**: تم پیش‌فرض **Metallic Black (مشکی متالیک)**، پشتیبانی کامل از چیدمان فارسی (RTL) و انگلیسی (LTR).

---

## راهنمای بیلد در گیت‌هاب (GitHub Actions CI/CD)

این مخزن به یک ورک‌فلو خودکار گیت‌هاب اکشنز (`.github/workflows/android-build.yml`) مجهز است که با هر `push` یا `pull_request` به شاخه `main`، یا از طریق منوی دستی **Actions -> Run workflow** مراحل زیر را خودکار انجام می‌دهد:

1. اجرای تمام تست‌های واحد و Robolectric
2. ساخت **Release APK**:
   ```
   app/build/outputs/apk/release/app-release.apk
   ```
3. ساخت **Release AAB (Android App Bundle)** مخصوص انتشار در گوگل پلی، کافه‌بازار و مایکت:
   ```
   app/build/outputs/bundle/release/app-release.aab
   ```
4. قرار دادن فایل‌های خروجی در بخش **Artifacts** گیت‌هاب برای دانلود مستقیم با یک کلیک.

---

## راهنمای بیلد محلی با کامند لاین (Local Build)

### ۱. اجرای تست‌ها
```bash
./gradlew testDebugUnitTest
```

### ۲. ساخت پکیج Release APK و AAB
```bash
KEYSTORE_PATH="my-upload-key.jks" STORE_PASSWORD="android" KEY_PASSWORD="android" ./gradlew assembleRelease bundleRelease
```
خروجی‌ها در مسیرهای زیر ساخته می‌شوند:
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`
