package com.wallpaper.data.repository

import com.wallpaper.data.database.dao.WallpaperDao
import com.wallpaper.data.database.entities.WallpaperImage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class WallpaperImageRepository(
    private val dao: WallpaperDao
) {
    
    fun getImagesByFolderId(folderId: Long): Flow<List<WallpaperImage>> = 
        dao.getImagesByFolderId(folderId)
    
    suspend fun getImageById(id: Long): WallpaperImage? = dao.getImageById(id)
    
    suspend fun getFirstImageByFolderId(folderId: Long): WallpaperImage? = 
        dao.getFirstImageByFolderId(folderId)
    
    suspend fun insertImage(image: WallpaperImage): Long {
        return dao.insertImage(image)
    }
    
    suspend fun updateImage(image: WallpaperImage) {
        dao.updateImage(image)
    }
    
    suspend fun deleteImage(image: WallpaperImage) {
        dao.deleteImage(image)
    }
    
    suspend fun deleteImagesByFolderId(folderId: Long) {
        dao.deleteImagesByFolderId(folderId)
    }
    
    suspend fun getImageCountByFolderId(folderId: Long): Int = 
        dao.getImageCountByFolderId(folderId)
    
    /**
     * Récupère l'image suivante dans un répertoire (rotation circulaire)
     */
    suspend fun getNextImage(folderId: Long, currentImageId: Long?): WallpaperImage? {
        val imageList = getImagesByFolderId(folderId).first()
        
        if (imageList.isEmpty()) return null
        
        if (currentImageId == null) {
            return imageList.firstOrNull()
        }
        
        val currentIndex = imageList.indexOfFirst { it.id == currentImageId }
        if (currentIndex == -1) {
            return imageList.firstOrNull()
        }
        
        val nextIndex = (currentIndex + 1) % imageList.size
        return imageList[nextIndex]
    }
    
    /**
     * Récupère une image aléatoire dans un répertoire
     */
    suspend fun getRandomImage(folderId: Long, excludeImageId: Long? = null): WallpaperImage? {
        val imageList = getImagesByFolderId(folderId).first()
        
        if (imageList.isEmpty()) return null
        
        val filteredList = if (excludeImageId != null && imageList.size > 1) {
            imageList.filter { it.id != excludeImageId }
        } else {
            imageList
        }
        
        return filteredList.randomOrNull()
    }
}

