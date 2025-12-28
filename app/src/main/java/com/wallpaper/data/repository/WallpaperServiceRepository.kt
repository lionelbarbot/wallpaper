package com.wallpaper.data.repository

import android.content.Context
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.database.entities.WallpaperImage
import com.wallpaper.util.WallpaperPreferences
import kotlinx.coroutines.flow.first

class WallpaperServiceRepository(
    private val folderRepository: WallpaperFolderRepository,
    private val imageRepository: WallpaperImageRepository,
    context: Context
) {
    private val preferences = WallpaperPreferences(context)
    
    suspend fun getActiveWallpaper(): Pair<WallpaperFolder?, WallpaperImage?> {
        val activeFolder = folderRepository.getActiveFolder() ?: return Pair(null, null)
        
        val lastImageId = preferences.getCurrentImageId(activeFolder.id)
        val image = if (lastImageId != -1L) {
            imageRepository.getImageById(lastImageId) ?: imageRepository.getFirstImageByFolderId(activeFolder.id)
        } else {
            imageRepository.getFirstImageByFolderId(activeFolder.id)
        }
        
        return Pair(activeFolder, image)
    }
    
    suspend fun getNextImage(currentFolderId: Long?): WallpaperImage? {
        val folderId = currentFolderId ?: folderRepository.getActiveFolder()?.id ?: return null
        val currentImageId = preferences.getCurrentImageId(folderId)
        
        val nextImage = imageRepository.getNextImage(folderId, if (currentImageId != -1L) currentImageId else null)
        if (nextImage != null) {
            preferences.setCurrentImageId(folderId, nextImage.id)
        }
        return nextImage
    }
    
    suspend fun getNextFolder(): WallpaperFolder? {
        val folderList = folderRepository.getAllFolders().first()
        if (folderList.isEmpty()) return null
        
        val lastFolderId = preferences.getLastFolderId()
        val currentIndex = folderList.indexOfFirst { it.id == lastFolderId }
        
        val nextIndex = (currentIndex + 1) % folderList.size
        val nextFolder = folderList[nextIndex]
        
        preferences.setLastFolderId(nextFolder.id)
        return nextFolder
    }
    
    fun updateLastImageId(folderId: Long, imageId: Long) {
        preferences.setCurrentImageId(folderId, imageId)
    }
    
    fun updateLastFolderId(folderId: Long) {
        preferences.setLastFolderId(folderId)
    }
}
