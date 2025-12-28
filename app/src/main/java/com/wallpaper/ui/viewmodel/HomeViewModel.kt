package com.wallpaper.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.content.Context
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.repository.WallpaperFolderRepository
import com.wallpaper.data.repository.WallpaperImageRepository
import com.wallpaper.data.repository.WallpaperServiceRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator
import com.wallpaper.service.WallpaperService
import com.wallpaper.util.WorkManagerHelper
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val folderRepository: WallpaperFolderRepository,
    private val context: Context? = null
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
    
    fun toggleFolderActive(folder: WallpaperFolder) {
        viewModelScope.launch {
            if (!folder.isActive) {
                // Désactiver tous les autres répertoires manuellement (sans réévaluer les règles)
                val allFolders = folderRepository.getAllFolders().first()
                allFolders.forEach { f ->
                    if (f.id != folder.id && f.isActive) {
                        folderRepository.updateFolder(f.copy(isActive = false))
                    }
                }
                // Activer ce répertoire
                val updatedFolder = folder.copy(isActive = true)
                folderRepository.updateFolder(updatedFolder)
                
                // Appliquer le fond d'écran du répertoire activé
                context?.let { ctx ->
                    val database = WallpaperDatabase.getDatabase(ctx)
                    val dao = database.wallpaperDao()
                    val imageRepository = WallpaperImageRepository(dao)
                    val wallpaperService = WallpaperService(ctx)
                    
                    // Utiliser l'ordre aléatoire si activé, sinon la première image
                    val firstImage = if (updatedFolder.randomOrder) {
                        imageRepository.getRandomImage(updatedFolder.id)
                    } else {
                        imageRepository.getFirstImageByFolderId(updatedFolder.id)
                    }
                    if (firstImage != null) {
                        wallpaperService.applyWallpaper(updatedFolder, firstImage)
                    }
                    
                    // Démarrer WorkManager si le répertoire a un intervalle de rotation
                    updatedFolder.rotationIntervalMinutes?.let { interval ->
                        if (interval > 0) {
                            WorkManagerHelper.startWallpaperRotation(ctx, interval.toLong())
                        } else {
                            WorkManagerHelper.stopWallpaperRotation(ctx)
                        }
                    } ?: WorkManagerHelper.stopWallpaperRotation(ctx)
                }
            } else {
                // Désactiver ce répertoire
                folderRepository.updateFolder(folder.copy(isActive = false))
                
                // Arrêter WorkManager quand le répertoire est désactivé
                context?.let { ctx ->
                    WorkManagerHelper.stopWallpaperRotation(ctx)
                }
            }
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
        return HomeViewModel(folderRepository, context) as T
    }
}

