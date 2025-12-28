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
        changeOnUnlock: Boolean = false,
        randomOrder: Boolean = false
    ) {
        viewModelScope.launch {
            val folder = if (folderId != null) {
                getFolder()?.copy(
                    name = name,
                    recurrenceType = recurrenceType,
                    recurrenceRule = recurrenceRule.toJson(),
                    targetScreen = targetScreen,
                    rotationIntervalMinutes = rotationIntervalMinutes,
                    changeOnUnlock = changeOnUnlock,
                    randomOrder = randomOrder
                    // Préserver isActive - ne pas le modifier ici
                ) ?: return@launch
            } else {
                WallpaperFolder(
                    name = name,
                    recurrenceType = recurrenceType,
                    recurrenceRule = recurrenceRule.toJson(),
                    targetScreen = targetScreen,
                    rotationIntervalMinutes = rotationIntervalMinutes,
                    changeOnUnlock = changeOnUnlock,
                    randomOrder = randomOrder
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
                // Récupérer le répertoire sauvegardé (avec son statut isActive préservé)
                val savedFolder = folderRepository.getFolderById(savedFolderId) ?: folder.copy(id = savedFolderId)
                
                // Utiliser l'intervalle du répertoire sauvegardé
                val rotationInterval = savedFolder.rotationIntervalMinutes
                
                // Ne pas appeler updateActiveStatuses() ici car cela écraserait l'état isActive
                // défini manuellement par l'utilisateur via le toggle
                // Vérifier directement si le répertoire est actif
                if (savedFolder.isActive && rotationInterval != null && rotationInterval > 0) {
                    // Démarrer ou redémarrer WorkManager avec le nouvel intervalle
                    WorkManagerHelper.startWallpaperRotation(ctx, rotationInterval.toLong())
                } else if (!savedFolder.isActive) {
                    // Arrêter WorkManager si le répertoire n'est pas actif
                    WorkManagerHelper.stopWallpaperRotation(ctx)
                }
                // Si isActive mais pas d'intervalle, ne rien faire (WorkManager reste dans son état actuel)
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

