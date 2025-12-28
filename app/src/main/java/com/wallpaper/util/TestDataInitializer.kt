package com.wallpaper.util

import android.content.Context
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.database.entities.WallpaperImage
import com.wallpaper.data.model.RecurrenceRule
import com.wallpaper.data.model.RecurrenceType
import com.wallpaper.data.model.TargetScreen
import com.wallpaper.data.repository.WallpaperFolderRepository
import com.wallpaper.data.repository.WallpaperImageRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object TestDataInitializer {
    private const val PREFS_NAME = "wallpaper_prefs"
    private const val KEY_TEST_DATA_INITIALIZED = "test_data_initialized"
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Initialise les données de test si ce n'est pas déjà fait
     */
    fun initializeTestDataIfNeeded(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        if (prefs.getBoolean(KEY_TEST_DATA_INITIALIZED, false)) {
            return // Déjà initialisé
        }
        
        scope.launch {
            try {
                val database = WallpaperDatabase.getDatabase(context)
                val dao = database.wallpaperDao()
                val recurrenceCalculator = RecurrenceCalculator()
                val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
                val imageRepository = WallpaperImageRepository(dao)
                
                // Vérifier si un répertoire de test existe déjà
                val existingFolders = folderRepository.getAllFolders().first()
                val testFolderExists = existingFolders.any { it.name == "Répertoire de test" }
                
                if (!testFolderExists) {
                    // Générer les images de test avec des flèches
                    val testImages = TestImageGenerator.generateTestImages(context)
                    
                    if (testImages.isNotEmpty()) {
                        // Créer le répertoire de test
                        val recurrenceRule = RecurrenceRule(
                            type = RecurrenceType.WEEKLY,
                            daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7), // Tous les jours
                            startHour = null,
                            endHour = null,
                            dayOfMonth = null
                        )
                        
                        val testFolder = WallpaperFolder(
                            name = "Répertoire de test",
                            recurrenceType = RecurrenceType.WEEKLY,
                            recurrenceRule = recurrenceRule.toJson(),
                            targetScreen = TargetScreen.BOTH,
                            rotationIntervalMinutes = null,
                            changeOnUnlock = false,
                            randomOrder = false
                        )
                        
                        val folderId = folderRepository.insertFolder(testFolder)
                        
                        // Ajouter les images au répertoire
                        testImages.forEachIndexed { index, imagePath ->
                            val image = WallpaperImage(
                                folderId = folderId,
                                filePath = imagePath,
                                displayOrder = index
                            )
                            imageRepository.insertImage(image)
                        }
                    }
                }
                
                // Marquer comme initialisé
                prefs.edit().putBoolean(KEY_TEST_DATA_INITIALIZED, true).apply()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

