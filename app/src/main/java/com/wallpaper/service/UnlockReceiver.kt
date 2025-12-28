package com.wallpaper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.repository.WallpaperFolderRepository
import com.wallpaper.data.repository.WallpaperImageRepository
import com.wallpaper.data.repository.WallpaperServiceRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver pour détecter le déverrouillage de l'appareil
 * et changer le fond d'écran si l'option est activée
 */
class UnlockReceiver : BroadcastReceiver() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("UnlockReceiver", "onReceive called with action: ${intent.action}")
        
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                Log.d("UnlockReceiver", "Device unlocked, checking for wallpaper change")
                handleUnlock(context)
            }
            Intent.ACTION_SCREEN_ON -> {
                Log.d("UnlockReceiver", "Screen turned on")
                // Note: ACTION_SCREEN_ON nécessite un WakeLock, on utilise ACTION_USER_PRESENT à la place
            }
        }
    }
    
    private fun handleUnlock(context: Context) {
        serviceScope.launch {
            try {
                val database = WallpaperDatabase.getDatabase(context)
                val dao = database.wallpaperDao()
                val recurrenceCalculator = RecurrenceCalculator()
                val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
                val imageRepository = WallpaperImageRepository(dao)
                val serviceRepository = WallpaperServiceRepository(folderRepository, imageRepository)
                val wallpaperService = WallpaperService(context)
                
                // Récupérer le répertoire actif
                val activeFolder = folderRepository.getActiveFolder()
                
                if (activeFolder != null && activeFolder.changeOnUnlock) {
                    Log.d("UnlockReceiver", "changeOnUnlock is enabled for folder: ${activeFolder.name}")
                    
                    // Récupérer l'image suivante du répertoire actif
                    val currentImage = imageRepository.getFirstImageByFolderId(activeFolder.id)
                    val nextImage = if (currentImage != null) {
                        imageRepository.getNextImage(activeFolder.id, currentImage.id)
                    } else {
                        currentImage
                    }
                    
                    if (nextImage != null) {
                        Log.d("UnlockReceiver", "Applying next image: ${nextImage.filePath}")
                        val result = wallpaperService.applyWallpaper(activeFolder, nextImage)
                        if (result.isSuccess) {
                            Log.d("UnlockReceiver", "Wallpaper changed successfully on unlock")
                        } else {
                            Log.e("UnlockReceiver", "Failed to change wallpaper: ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        Log.w("UnlockReceiver", "No next image found")
                    }
                } else {
                    Log.d("UnlockReceiver", "changeOnUnlock is disabled or no active folder")
                }
            } catch (e: Exception) {
                Log.e("UnlockReceiver", "Error handling unlock", e)
            }
        }
    }
}

