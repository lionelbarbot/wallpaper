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
import com.wallpaper.util.WorkManagerHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EditFolderViewModel(
    private val folderId: Long?,
    private val folderRepository: WallpaperFolderRepository,
    private val context: Context? = null
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
        targetScreen: TargetScreen,
        rotationIntervalMinutes: Int? = null,
        changeOnUnlock: Boolean = false
    ) {
        viewModelScope.launch {
            val folder = if (folderId != null) {
                getFolder()?.copy(
                    name = name,
                    recurrenceType = recurrenceType,
                    recurrenceRule = recurrenceRule.toJson(),
                    targetScreen = targetScreen,
                    rotationIntervalMinutes = rotationIntervalMinutes,
                    changeOnUnlock = changeOnUnlock
                ) ?: return@launch
            } else {
                WallpaperFolder(
                    name = name,
                    recurrenceType = recurrenceType,
                    recurrenceRule = recurrenceRule.toJson(),
                    targetScreen = targetScreen,
                    rotationIntervalMinutes = rotationIntervalMinutes,
                    changeOnUnlock = changeOnUnlock
                )
            }
            
            val savedFolderId = if (folderId != null) {
                folderRepository.updateFolder(folder)
                folderId
            } else {
                folderRepository.insertFolder(folder)
            }
            
            // Gérer WorkManager si le répertoire est actif et a un intervalle de rotation
            context?.let { ctx ->
                // Vérifier si le répertoire est actif (mettre à jour les statuts d'abord)
                folderRepository.updateActiveStatuses()
                
                // Récupérer le répertoire sauvegardé avec son statut actif mis à jour
                val savedFolder = folderRepository.getFolderById(savedFolderId) ?: folder.copy(id = savedFolderId)
                
                // Vérifier si le répertoire est actif après mise à jour des statuts
                val allFolders = folderRepository.getAllFolders().first()
                val isActive = allFolders.any { it.id == savedFolderId && it.isActive }
                
                // Utiliser l'intervalle du répertoire sauvegardé
                val rotationInterval = savedFolder.rotationIntervalMinutes
                
                if (isActive && rotationInterval != null && rotationInterval > 0) {
                    // Démarrer ou redémarrer WorkManager avec le nouvel intervalle
                    WorkManagerHelper.startWallpaperRotation(ctx, rotationInterval.toLong())
                } else {
                    // Arrêter WorkManager si pas d'intervalle ou répertoire non actif
                    WorkManagerHelper.stopWallpaperRotation(ctx)
                }
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
        return EditFolderViewModel(folderId, folderRepository, context) as T
    }
}

