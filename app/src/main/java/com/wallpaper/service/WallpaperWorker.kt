package com.wallpaper.service

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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
            val serviceRepository = WallpaperServiceRepository(folderRepository, imageRepository, applicationContext)
            val wallpaperService = WallpaperService(applicationContext)
            
            // Ne pas réinitialiser les statuts si un répertoire a été activé manuellement
            // updateActiveStatuses() réinitialise tous les statuts selon les règles de récurrence
            // ce qui peut désactiver un répertoire activé manuellement par l'utilisateur
            // folderRepository.updateActiveStatuses()
            
            val activeFolder = folderRepository.getActiveFolder()
            if (activeFolder != null) {
                val nextImage = serviceRepository.getNextImage(activeFolder.id)
                if (nextImage != null) {
                    val result = wallpaperService.applyWallpaper(activeFolder, nextImage)
                    return if (result.isSuccess) Result.success() else Result.retry()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
