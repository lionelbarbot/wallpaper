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

class UnlockReceiver : BroadcastReceiver() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            Log.d("UnlockReceiver", "Device unlocked: rotating wallpaper")
            
            scope.launch {
                try {
                    val database = WallpaperDatabase.getDatabase(context)
                    val dao = database.wallpaperDao()
                    val recurrenceCalculator = RecurrenceCalculator()
                    val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
                    val imageRepository = WallpaperImageRepository(dao)
                    val serviceRepository = WallpaperServiceRepository(folderRepository, imageRepository, context)
                    val wallpaperService = WallpaperService(context)

                    val activeFolder = folderRepository.getActiveFolder()
                    if (activeFolder != null) {
                        val nextImage = serviceRepository.getNextImage(activeFolder.id)
                        if (nextImage != null) {
                            wallpaperService.applyWallpaper(activeFolder, nextImage)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("UnlockReceiver", "Unlock rotation failed", e)
                }
            }
        }
    }
}
