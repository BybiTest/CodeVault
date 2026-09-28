package com.example

import android.app.Application
import android.util.Log
import android.widget.Toast
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

        Log.e("TAPSELL_DEBUG", "=== CodeVaultApplication.onCreate STARTED ===")

        try {
            database = CodeVaultDatabase.getDatabase(this)
            Log.e("TAPSELL_DEBUG", "=== Database OK ===")

            fileManager = FileManager(this)
            settingsRepository = SettingsRepository(this)
            projectRepository = ProjectRepository(database, fileManager)
            Log.e("TAPSELL_DEBUG", "=== Repos OK ===")

            tapsellAdManager = TapsellAdManager(this)
            Log.e("TAPSELL_DEBUG", "=== TapsellAdManager CREATED ===")

            tapsellAdManager.initialize()
            Log.e("TAPSELL_DEBUG", "=== Tapsell initialize CALLED ===")

            billingManager = BillingManager(this, settingsRepository, applicationScope)
            Log.e("TAPSELL_DEBUG", "=== BillingManager OK ===")

            Toast.makeText(this, "App started - Tapsell init called", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("TAPSELL_DEBUG", "=== Application onCreate ERROR: ${e.message} ===")
            Toast.makeText(this, "App error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
