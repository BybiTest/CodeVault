package com.example.data.ads

import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileWriter

object AppLogger {
    private const val TAG = "TapsellMediation"

    fun log(message: String) {
        val timestamp = System.currentTimeMillis()
        val formatted = "$timestamp: $message"
        Log.i(TAG, formatted)

        try {
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadDir.exists()) {
                downloadDir.mkdirs()
            }
            val logFile = File(downloadDir, "tapsell_log.txt")
            FileWriter(logFile, true).use { writer ->
                writer.append(formatted).append("\n")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "File log error: ${e.message}")
        }
    }
}
