package com.wallpaper.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionHelper {
    
    fun hasStoragePermission(context: Context): Boolean {
        // Avec minSdk 35, nous utilisons toujours READ_MEDIA_IMAGES (Android 13+)
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasWallpaperPermission(context: Context): Boolean {
        // SET_WALLPAPER est une permission normale, pas runtime
        return true
    }
    
    fun getStoragePermission(): String {
        // Avec minSdk 35, nous utilisons toujours READ_MEDIA_IMAGES
        return Manifest.permission.READ_MEDIA_IMAGES
    }
}

