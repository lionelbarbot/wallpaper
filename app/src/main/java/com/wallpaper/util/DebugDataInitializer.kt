package com.wallpaper.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.database.entities.WallpaperImage
import com.wallpaper.data.model.RecurrenceRule
import com.wallpaper.data.model.RecurrenceType
import com.wallpaper.data.model.TargetScreen
import com.wallpaper.data.repository.WallpaperFolderRepository
import com.wallpaper.data.repository.WallpaperImageRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object DebugDataInitializer {
    private const val PREFS_NAME = "wallpaper_prefs"
    private const val KEY_FIRST_LAUNCH = "first_launch"
    
    /**
     * Initialise les données de débogage au premier lancement
     */
    suspend fun initializeDebugDataIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        
        if (!isFirstLaunch) {
            return@withContext
        }
        
        try {
            val database = WallpaperDatabase.getDatabase(context)
            val dao = database.wallpaperDao()
            val recurrenceCalculator = RecurrenceCalculator()
            val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
            val imageRepository = WallpaperImageRepository(dao)
            
            // Vérifier si des répertoires existent déjà
            val existingFolders = folderRepository.getAllFolders().first()
            if (existingFolders.isNotEmpty()) {
                // Marquer comme initialisé même si on n'a pas créé de données
                prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
                return@withContext
            }
            
            // Créer le répertoire par défaut avec récurrence tous les jours
            // Tous les jours de la semaine sont sélectionnés (1-7)
            val defaultRule = RecurrenceRule(
                type = RecurrenceType.WEEKLY,
                daysOfWeek = listOf(1, 2, 3, 4, 5, 6, 7), // Tous les jours
                startHour = null,
                endHour = null,
                dayOfMonth = null
            )
            
            val defaultFolder = com.wallpaper.data.database.entities.WallpaperFolder(
                name = "Répertoire de test",
                recurrenceType = RecurrenceType.WEEKLY,
                recurrenceRule = defaultRule.toJson(),
                targetScreen = TargetScreen.BOTH,
                rotationIntervalMinutes = null,
                changeOnUnlock = false
            )
            
            val folderId = folderRepository.insertFolder(defaultFolder)
            
            // Créer le répertoire pour les images
            val imagesDir = File(context.getExternalFilesDir(null), "wallpapers")
            if (!imagesDir.exists()) {
                imagesDir.mkdirs()
            }
            
            // Créer 4 images de test avec des couleurs différentes
            val testImages = listOf(
                createTestImage(imagesDir, "test_1.png", Color.RED, "Image 1"),
                createTestImage(imagesDir, "test_2.png", Color.BLUE, "Image 2"),
                createTestImage(imagesDir, "test_3.png", Color.GREEN, "Image 3"),
                createTestImage(imagesDir, "test_4.png", Color.MAGENTA, "Image 4")
            )
            
            // Ajouter les images au répertoire
            testImages.forEachIndexed { index, imagePath ->
                val wallpaperImage = WallpaperImage(
                    folderId = folderId,
                    filePath = imagePath,
                    displayOrder = index
                )
                imageRepository.insertImage(wallpaperImage)
            }
            
            // Marquer comme initialisé
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        } catch (e: Exception) {
            // En cas d'erreur, on continue quand même
            e.printStackTrace()
        }
    }
    
    /**
     * Crée une image de test avec une couleur de fond
     */
    private fun createTestImage(directory: File, filename: String, backgroundColor: Int, label: String): String {
        val width = 1920
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fond coloré
        canvas.drawColor(backgroundColor)
        
        // Ajouter du texte au centre
        val paint = Paint().apply {
            color = Color.WHITE
            textSize = 120f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        val x = width / 2f
        val y = height / 2f - (paint.descent() + paint.ascent()) / 2
        canvas.drawText(label, x, y, paint)
        
        // Sauvegarder l'image
        val file = File(directory, filename)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        bitmap.recycle()
        
        return file.absolutePath
    }
}

