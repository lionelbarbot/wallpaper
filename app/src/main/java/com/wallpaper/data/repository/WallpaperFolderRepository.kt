package com.wallpaper.data.repository

import com.wallpaper.data.database.dao.WallpaperDao
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.model.RecurrenceRule
import com.wallpaper.domain.usecase.RecurrenceCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class WallpaperFolderRepository(
    private val dao: WallpaperDao,
    private val recurrenceCalculator: RecurrenceCalculator
) {
    
    fun getAllFolders(): Flow<List<WallpaperFolder>> = dao.getAllFolders()
    
    suspend fun getFolderById(id: Long): WallpaperFolder? = dao.getFolderById(id)
    
    suspend fun insertFolder(folder: WallpaperFolder): Long {
        return dao.insertFolder(folder)
    }
    
    suspend fun updateFolder(folder: WallpaperFolder) {
        dao.updateFolder(folder)
    }
    
    suspend fun deleteFolder(folder: WallpaperFolder) {
        dao.deleteFolder(folder)
    }
    
    /**
     * Met à jour le statut actif de tous les répertoires selon leurs règles de récurrence
     */
    suspend fun updateActiveStatuses() {
        dao.deactivateAllFolders()
        val folderList = dao.getAllFolders().first()
        folderList.forEach { folder ->
            val rule = RecurrenceRule.fromJson(folder.recurrenceRule)
            if (rule != null && recurrenceCalculator.isActive(rule)) {
                dao.updateFolderActiveStatus(folder.id, true)
            }
        }
    }
    
    /**
     * Retourne le répertoire actif actuellement
     */
    suspend fun getActiveFolder(): WallpaperFolder? {
        val folderList = dao.getAllFolders().first()
        return folderList.firstOrNull { folder ->
            val rule = RecurrenceRule.fromJson(folder.recurrenceRule)
            rule != null && recurrenceCalculator.isActive(rule)
        }
    }
}

