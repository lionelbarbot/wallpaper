package com.wallpaper.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.database.entities.WallpaperImage
import com.wallpaper.data.repository.WallpaperImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ImageEditorViewModel(
    private val imageId: Long,
    private val imageRepository: WallpaperImageRepository
) : ViewModel() {
    
    private val _image = MutableStateFlow<WallpaperImage?>(null)
    val image: StateFlow<WallpaperImage?> = _image
    
    init {
        viewModelScope.launch {
            _image.value = imageRepository.getImageById(imageId)
        }
    }
}

class ImageEditorViewModelFactory(
    private val context: Context,
    private val imageId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val database = WallpaperDatabase.getDatabase(context)
        val dao = database.wallpaperDao()
        val imageRepository = WallpaperImageRepository(dao)
        return ImageEditorViewModel(imageId, imageRepository) as T
    }
}

