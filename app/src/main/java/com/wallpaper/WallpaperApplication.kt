package com.wallpaper

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.wallpaper.service.WallpaperWorker
import com.wallpaper.util.TestDataInitializer
import java.util.concurrent.TimeUnit

class WallpaperApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialiser les données de test
        TestDataInitializer.initializeTestDataIfNeeded(this)
        setupWorkManager()
    }
    
    private fun setupWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<WallpaperWorker>(
            15, TimeUnit.MINUTES
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "WallpaperChange",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
