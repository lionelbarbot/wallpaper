package com.wallpaper.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.database.entities.WallpaperImage
import com.wallpaper.data.repository.WallpaperFolderRepository
import com.wallpaper.data.repository.WallpaperImageRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FolderDetailViewModel(
    private val folderId: Long,
    private val folderRepository: WallpaperFolderRepository,
    private val imageRepository: WallpaperImageRepository
) : ViewModel() {
    
    val images: StateFlow<List<WallpaperImage>> = imageRepository.getImagesByFolderId(folderId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    val folder: StateFlow<WallpaperFolder?> = folderRepository.getAllFolders()
        .map { folders ->
            folders.firstOrNull { it.id == folderId }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    
    fun deleteImage(image: WallpaperImage) {
        viewModelScope.launch {
            imageRepository.deleteImage(image)
        }
    }
}

class FolderDetailViewModelFactory(
    private val context: Context,
    private val folderId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = WallpaperDatabase.getDatabase(context)
        val dao = database.wallpaperDao()
        val recurrenceCalculator = RecurrenceCalculator()
        val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
        val imageRepository = WallpaperImageRepository(dao)
        return FolderDetailViewModel(folderId, folderRepository, imageRepository) as T
    }
}

