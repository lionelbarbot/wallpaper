package com.wallpaper.service

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.content.ContextCompat
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.database.entities.WallpaperImage
import com.wallpaper.data.model.TargetScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WallpaperService(private val context: Context) {
    
    private val wallpaperManager: WallpaperManager = 
        WallpaperManager.getInstance(context)
    
    /**
     * Applique une image comme fond d'écran selon la configuration du répertoire
     */
    suspend fun applyWallpaper(
        folder: WallpaperFolder,
        image: WallpaperImage
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bitmap = loadBitmap(image.filePath) ?: return@withContext Result.failure(
                Exception("Impossible de charger l'image")
            )
            
            when (folder.targetScreen) {
                TargetScreen.LOCK_SCREEN -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK)
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
                TargetScreen.HOME_SCREEN -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
                TargetScreen.BOTH -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                    } else {
                        wallpaperManager.setBitmap(bitmap)
                    }
                }
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Charge un bitmap depuis un chemin de fichier
     */
    private fun loadBitmap(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(filePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Vérifie si l'application a la permission SET_WALLPAPER
     */
    fun hasWallpaperPermission(): Boolean {
        return try {
            wallpaperManager.isSetWallpaperAllowed
        } catch (e: Exception) {
            false
        }
    }
}

