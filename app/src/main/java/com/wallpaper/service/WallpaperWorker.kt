package com.wallpaper.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wallpaper.WallpaperApplication
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.repository.WallpaperFolderRepository
import com.wallpaper.data.repository.WallpaperImageRepository
import com.wallpaper.data.repository.WallpaperServiceRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator

class WallpaperWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val database = WallpaperDatabase.getDatabase(applicationContext)
            val dao = database.wallpaperDao()
            val recurrenceCalculator = RecurrenceCalculator()
            val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
            val imageRepository = WallpaperImageRepository(dao)
            val serviceRepository = WallpaperServiceRepository(folderRepository, imageRepository)
            val wallpaperService = WallpaperService(applicationContext)
            
            // Mettre à jour les statuts actifs des répertoires
            folderRepository.updateActiveStatuses()
            
            // Récupérer le répertoire actif et son image
            val (activeFolder, currentImage) = serviceRepository.getActiveWallpaper()
            
            if (activeFolder != null && currentImage != null) {
                // Appliquer le fond d'écran
                val result = wallpaperService.applyWallpaper(activeFolder, currentImage)
                if (result.isSuccess) {
                    Result.success()
                } else {
                    Result.retry()
                }
            } else {
                Result.success() // Pas de répertoire actif, c'est OK
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}

