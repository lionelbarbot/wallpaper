package com.wallpaper

import android.app.Application
import com.wallpaper.util.DebugDataInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class WallpaperApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialiser les données de débogage au premier lancement
        applicationScope.launch {
            DebugDataInitializer.initializeDebugDataIfNeeded(this@WallpaperApplication)
        }
    }
}

