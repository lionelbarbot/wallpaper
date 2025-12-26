package com.wallpaper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.repository.WallpaperFolderRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val folderRepository: WallpaperFolderRepository
) : ViewModel() {
    
    val folders: StateFlow<List<WallpaperFolder>> = folderRepository.getAllFolders()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    fun deleteFolder(folder: WallpaperFolder) {
        viewModelScope.launch {
            folderRepository.deleteFolder(folder)
        }
    }
}

class HomeViewModelFactory(
    private val context: android.content.Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = WallpaperDatabase.getDatabase(context)
        val dao = database.wallpaperDao()
        val recurrenceCalculator = RecurrenceCalculator()
        val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
        return HomeViewModel(folderRepository) as T
    }
}

