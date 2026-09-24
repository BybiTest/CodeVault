package com.example.ui.screens.importexport

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.data.model.ProjectEntity
import com.example.ui.components.CodeVaultTopBar
import com.example.ui.localization.LocalAppStrings
import com.example.ui.screens.dashboard.formatBytes
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Composable
fun ImportProjectScreen(
  onBack: () -> Unit,
  onImportFromStream: (name: String, stream: InputStream) -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  var projectName by remember { mutableStateOf("") }
  var isImporting by remember { mutableStateOf(false) }

  val zipPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val stream = context.contentResolver.openInputStream(uri)
        if (stream != null) {
          isImporting = true
          val derivedName = projectName.ifBlank {
            uri.lastPathSegment?.substringBeforeLast('.') ?: "ImportedProject"
          }
          onImportFromStream(derivedName, stream)
        }
      } catch (e: Exception) {
        Toast.makeText(context, "خطا در خواندن فایل ZIP: ${e.message}", Toast.LENGTH_LONG).show()
      }
    }
  }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.importZip,
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
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      Text(
        text = "وارد کردن پروژه از فایل ZIP",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = "فایل ZIP شامل پوشه‌ها و کدهای پروژه را انتخاب کنید. CodeVault ساختار درختی پوشه‌ها و محتوای فایل‌ها را به طور دقیق استخراج و ثبت می‌کند.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      OutlinedTextField(
        value = projectName,
        onValueChange = { projectName = it },
        label = { Text("نام پروژه در CodeVault (اختیاری)") },
        placeholder = { Text("نام پیش‌فرض از فایل ZIP خوانده می‌شود") },
        leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("import_project_name_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(16.dp))
          .clickable { zipPickerLauncher.launch("application/zip") }
          .border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Icon(
            imageVector = Icons.Default.CloudUpload,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(52.dp)
          )
          Spacer(modifier = Modifier.height(14.dp))
          Text(
            text = "انتخاب فایل ZIP از حافظه گوشی",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "لمس کنید تا فایل منیجر باز شود",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      // Quick Import Sample Projects
      Text(
        text = "یا وارد کردن پروژه‌های نمونه آماده:",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      SampleProjectCard(
        name = "Arena Clash Game",
        desc = "موتور بازی کاتلین شامل کلاس‌های Entity, System, و GameLoop",
        onSelect = {
          isImporting = true
          val stream = generateSampleZipStream("Arena Clash", "kotlin")
          onImportFromStream("Arena Clash", stream)
        }
      )

      SampleProjectCard(
        name = "CreatorFlow App",
        desc = "اپلیکیشن کامل با مدل‌های داده، ViewModel و کدهای رابط کاربری",
        onSelect = {
          isImporting = true
          val stream = generateSampleZipStream("CreatorFlow", "android")
          onImportFromStream("CreatorFlow", stream)
        }
      )

      SampleProjectCard(
        name = "آنسو (Ansoo REST API)",
        desc = "سرویس وب کامل شامل سرور، روترها و کدهای اتصال به دیتابیس",
        onSelect = {
          isImporting = true
          val stream = generateSampleZipStream("آنسو", "python")
          onImportFromStream("آنسو", stream)
        }
      )

      if (isImporting) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator()
        }
      }
    }
  }
}

@Composable
private fun SampleProjectCard(
  name: String,
  desc: String,
  onSelect: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .clickable { onSelect() }
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(Icons.Default.FolderZip, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(text = name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Icon(Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
    }
  }
}

private fun generateSampleZipStream(projectName: String, type: String): InputStream {
  val baos = java.io.ByteArrayOutputStream()
  ZipOutputStream(baos).use { zos ->
    zos.putNextEntry(ZipEntry("README.md"))
    zos.write("# $projectName\n\nGenerated for CodeVault demo testing.\n".toByteArray(Charsets.UTF_8))
    zos.closeEntry()

    zos.putNextEntry(ZipEntry("src/main.kt"))
    zos.write("// Main entry for $projectName\nfun main() {\n    println(\"Running $projectName\")\n}\n".toByteArray(Charsets.UTF_8))
    zos.closeEntry()

    zos.putNextEntry(ZipEntry("config/settings.json"))
    zos.write("{\n  \"appName\": \"$projectName\",\n  \"version\": \"1.0.0\"\n}\n".toByteArray(Charsets.UTF_8))
    zos.closeEntry()
  }
  return ByteArrayInputStream(baos.toByteArray())
}

@Composable
fun ExportProjectScreen(
  project: ProjectEntity?,
  onBack: () -> Unit,
  onExportZip: (destinationFile: File, onComplete: (File?) -> Unit) -> Unit
) {
  val context = LocalContext.current
  val strings = LocalAppStrings.current
  var isExporting by remember { mutableStateOf(false) }
  var exportedFile by remember { mutableStateOf<File?>(null) }

  Scaffold(
    topBar = {
      CodeVaultTopBar(
        title = strings.exportAsZip,
        subtitle = project?.name ?: "",
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
      if (project == null) {
        CircularProgressIndicator()
        return@Column
      }

      Text(
        text = "خروجی فایل فشرده استاندار (ZIP)",
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )

      Text(
        text = "این عملیات کل پوشه‌ها و فایلهای پروژه «${project.name}» را به یک فایل ZIP استاندارد تبدیل می‌کند که در هر کامپیوتر یا ابزاری قابل باز شدن و استفاده است.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("نام پروژه: ${project.name}", fontWeight = FontWeight.Bold)
          Text("تعداد فایل‌ها: ${project.fileCount} فایل")
          Text("تعداد پوشه‌ها: ${project.folderCount} پوشه")
          Text("حجم پروژه: ${formatBytes(project.totalSizeBytes)}")
        }
      }

      Button(
        onClick = {
          isExporting = true
          val exportFile = File(context.cacheDir, "${project.name.replace(" ", "_")}.zip")
          onExportZip(exportFile) { result ->
            isExporting = false
            exportedFile = result
            if (result != null) {
              Toast.makeText(context, "فایل ZIP در ${result.name} آماده شد", Toast.LENGTH_SHORT).show()
            }
          }
        },
        enabled = !isExporting,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("start_export_zip_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isExporting) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
          Icon(Icons.Default.FolderZip, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(strings.exportAsZip, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
      }

      if (exportedFile != null) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
          shape = RoundedCornerShape(12.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "فایل ZIP با موفقیت آماده شد:",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = exportedFile!!.name,
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = formatBytes(exportedFile!!.length()),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
              onClick = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "application/zip"
                  putExtra(Intent.EXTRA_SUBJECT, project.name)
                  putExtra(Intent.EXTRA_TEXT, "Project ZIP exported from CodeVault")
                }
                context.startActivity(Intent.createChooser(shareIntent, "ارسال فایل ZIP پروژه"))
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(Icons.Default.Share, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("ارسال و اشتراک‌گذاری فایل ZIP")
            }
          }
        }
      }
    }
  }
}
