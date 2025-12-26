package com.wallpaper.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wallpaper.service.WallpaperWorker
import java.util.concurrent.TimeUnit

object WorkManagerHelper {
    
    private const val WORK_NAME = "wallpaper_rotation_work"
    
    fun startWallpaperRotation(context: Context, intervalMinutes: Long) {
        val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(
            intervalMinutes,
            TimeUnit.MINUTES
        )
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiresBatteryNotLow(false)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    fun stopWallpaperRotation(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}

