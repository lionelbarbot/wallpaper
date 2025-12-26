package com.wallpaper.data.repository

import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.database.entities.WallpaperImage
import kotlinx.coroutines.flow.first

/**
 * Repository pour la logique métier liée au service de fonds d'écran
 */
class WallpaperServiceRepository(
    private val folderRepository: WallpaperFolderRepository,
    private val imageRepository: WallpaperImageRepository
) {
    
    /**
     * Récupère le répertoire actif et son image actuelle
     */
    suspend fun getActiveWallpaper(): Pair<WallpaperFolder?, WallpaperImage?> {
        val activeFolder = folderRepository.getActiveFolder()
        if (activeFolder == null) {
            return Pair(null, null)
        }
        
        val image = imageRepository.getFirstImageByFolderId(activeFolder.id)
        return Pair(activeFolder, image)
    }
    
    /**
     * Récupère l'image suivante du répertoire actif
     */
    suspend fun getNextImage(currentImageId: Long?): WallpaperImage? {
        val activeFolder = folderRepository.getActiveFolder() ?: return null
        return imageRepository.getNextImage(activeFolder.id, currentImageId)
    }
    
    /**
     * Récupère le répertoire suivant (rotation circulaire)
     */
    suspend fun getNextFolder(currentFolderId: Long?): WallpaperFolder? {
        val folderList = folderRepository.getAllFolders().first()
        
        if (folderList.isEmpty()) return null
        
        if (currentFolderId == null) {
            return folderList.firstOrNull()
        }
        
        val currentIndex = folderList.indexOfFirst { it.id == currentFolderId }
        if (currentIndex == -1) {
            return folderList.firstOrNull()
        }
        
        val nextIndex = (currentIndex + 1) % folderList.size
        return folderList[nextIndex]
    }
}

