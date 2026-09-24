package com.example

import android.app.Application
import com.example.data.ads.TapsellAdManager
import com.example.data.billing.BillingManager
import com.example.data.fs.FileManager
import com.example.data.local.CodeVaultDatabase
import com.example.data.repository.ProjectRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class CodeVaultApplication : Application() {

  private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  lateinit var database: CodeVaultDatabase
    private set

  lateinit var fileManager: FileManager
    private set

  lateinit var settingsRepository: SettingsRepository
    private set

  lateinit var projectRepository: ProjectRepository
    private set

  lateinit var tapsellAdManager: TapsellAdManager
    private set

  lateinit var billingManager: BillingManager
    private set

  override fun onCreate() {
    super.onCreate()
    database = CodeVaultDatabase.getDatabase(this)
    fileManager = FileManager(this)
    settingsRepository = SettingsRepository(this)
    projectRepository = ProjectRepository(database, fileManager)
    tapsellAdManager = TapsellAdManager(this).apply { initialize() }
    billingManager = BillingManager(this, settingsRepository, applicationScope)
  }
}
