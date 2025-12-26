package com.wallpaper.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.model.RecurrenceRule
import com.wallpaper.data.model.RecurrenceType
import com.wallpaper.data.model.TargetScreen
import com.wallpaper.data.repository.WallpaperFolderRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator
import kotlinx.coroutines.launch

class EditFolderViewModel(
    private val folderId: Long?,
    private val folderRepository: WallpaperFolderRepository
) : ViewModel() {
    
    suspend fun getFolder(): WallpaperFolder? {
        return if (folderId != null) {
            folderRepository.getFolderById(folderId)
        } else {
            null
        }
    }
    
    fun saveFolder(
        name: String,
        recurrenceType: RecurrenceType,
        recurrenceRule: RecurrenceRule,
        targetScreen: TargetScreen
    ) {
        viewModelScope.launch {
            val folder = if (folderId != null) {
                getFolder()?.copy(
                    name = name,
                    recurrenceType = recurrenceType,
                    recurrenceRule = recurrenceRule.toJson(),
                    targetScreen = targetScreen
                ) ?: return@launch
            } else {
                WallpaperFolder(
                    name = name,
                    recurrenceType = recurrenceType,
                    recurrenceRule = recurrenceRule.toJson(),
                    targetScreen = targetScreen
                )
            }
            
            if (folderId != null) {
                folderRepository.updateFolder(folder)
            } else {
                folderRepository.insertFolder(folder)
            }
        }
    }
}

class EditFolderViewModelFactory(
    private val context: Context,
    private val folderId: Long?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = WallpaperDatabase.getDatabase(context)
        val dao = database.wallpaperDao()
        val recurrenceCalculator = RecurrenceCalculator()
        val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
        return EditFolderViewModel(folderId, folderRepository) as T
    }
}

