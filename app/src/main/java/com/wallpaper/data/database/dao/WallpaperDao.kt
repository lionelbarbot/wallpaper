package com.wallpaper.data.database.dao

import androidx.room.*
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.database.entities.WallpaperImage
import kotlinx.coroutines.flow.Flow

@Dao
interface WallpaperDao {
    
    // WallpaperFolder operations
    @Query("SELECT * FROM wallpaper_folders ORDER BY createdAt DESC")
    fun getAllFolders(): Flow<List<WallpaperFolder>>
    
    @Query("SELECT * FROM wallpaper_folders WHERE id = :id")
    suspend fun getFolderById(id: Long): WallpaperFolder?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: WallpaperFolder): Long
    
    @Update
    suspend fun updateFolder(folder: WallpaperFolder)
    
    @Delete
    suspend fun deleteFolder(folder: WallpaperFolder)
    
    @Query("UPDATE wallpaper_folders SET isActive = :isActive WHERE id = :id")
    suspend fun updateFolderActiveStatus(id: Long, isActive: Boolean)
    
    @Query("UPDATE wallpaper_folders SET isActive = 0")
    suspend fun deactivateAllFolders()
    
    // WallpaperImage operations
    @Query("SELECT * FROM wallpaper_images WHERE folderId = :folderId ORDER BY displayOrder ASC, createdAt ASC")
    fun getImagesByFolderId(folderId: Long): Flow<List<WallpaperImage>>
    
    @Query("SELECT * FROM wallpaper_images WHERE id = :id")
    suspend fun getImageById(id: Long): WallpaperImage?
    
    @Query("SELECT * FROM wallpaper_images WHERE folderId = :folderId ORDER BY displayOrder ASC, createdAt ASC LIMIT 1")
    suspend fun getFirstImageByFolderId(folderId: Long): WallpaperImage?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: WallpaperImage): Long
    
    @Update
    suspend fun updateImage(image: WallpaperImage)
    
    @Delete
    suspend fun deleteImage(image: WallpaperImage)
    
    @Query("DELETE FROM wallpaper_images WHERE folderId = :folderId")
    suspend fun deleteImagesByFolderId(folderId: Long)
    
    @Query("SELECT COUNT(*) FROM wallpaper_images WHERE folderId = :folderId")
    suspend fun getImageCountByFolderId(folderId: Long): Int
}

