package com.wallpaper.util

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wallpaper.service.WallpaperWorker
import java.util.concurrent.TimeUnit

object WorkManagerHelper {
    
    private const val WORK_NAME = "wallpaper_rotation_work"
    private const val MIN_INTERVAL_MINUTES = 15L // Minimum requis par Android pour les tâches périodiques
    
    fun startWallpaperRotation(context: Context, intervalMinutes: Long) {
        // Android limite les tâches périodiques à 15 minutes minimum
        val actualInterval = if (intervalMinutes < MIN_INTERVAL_MINUTES) {
            Log.w("WorkManagerHelper", "Interval $intervalMinutes minutes is below Android minimum of $MIN_INTERVAL_MINUTES minutes. Using $MIN_INTERVAL_MINUTES minutes instead.")
            MIN_INTERVAL_MINUTES
        } else {
            intervalMinutes
        }
        
        val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(
            actualInterval,
            TimeUnit.MINUTES
        )
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        Log.d("WorkManagerHelper", "Starting wallpaper rotation with interval: $actualInterval minutes")
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    fun stopWallpaperRotation(context: Context) {
        Log.d("WorkManagerHelper", "Stopping wallpaper rotation")
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

