package com.wallpaper.service

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.database.entities.WallpaperImage
import com.wallpaper.data.model.TargetScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WallpaperService(private val context: Context) {
    
    private val wallpaperManager: WallpaperManager = 
        WallpaperManager.getInstance(context)
    
    /**
     * Applique une image comme fond d'écran selon la configuration du répertoire
     * L'image est automatiquement redimensionnée pour s'adapter à la taille de l'écran
     */
    suspend fun applyWallpaper(
        folder: WallpaperFolder,
        image: WallpaperImage
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            Log.d("WallpaperService", "applyWallpaper called: folder=${folder.name}, image=${image.filePath}")
            
            val originalBitmap = loadBitmap(image.filePath) ?: return@withContext Result.failure(
                Exception("Impossible de charger l'image")
            )
            
            Log.d("WallpaperService", "Bitmap loaded: ${originalBitmap.width}x${originalBitmap.height}")
            
            // Obtenir la taille recommandée pour le fond d'écran
            val desiredWidth = wallpaperManager.desiredMinimumWidth
            val desiredHeight = wallpaperManager.desiredMinimumHeight
            
            Log.d("WallpaperService", "Desired size: ${desiredWidth}x${desiredHeight}")
            
            // Redimensionner le bitmap pour s'adapter à la taille de l'écran
            val resizedBitmap = resizeBitmapToFitScreen(originalBitmap, desiredWidth, desiredHeight)
            
            Log.d("WallpaperService", "Bitmap resized: ${resizedBitmap.width}x${resizedBitmap.height}")
            
            // Libérer la mémoire de l'image originale
            if (originalBitmap != resizedBitmap) {
                originalBitmap.recycle()
            }
            
            // Appliquer le même fond d'écran à tous les écrans demandés
            // Avec minSdk 35, nous utilisons toujours les flags FLAG_SYSTEM et FLAG_LOCK (API 24+)
            when (folder.targetScreen) {
                TargetScreen.LOCK_SCREEN -> {
                    Log.d("WallpaperService", "Applying to LOCK_SCREEN")
                    wallpaperManager.setBitmap(resizedBitmap, null, true, WallpaperManager.FLAG_LOCK)
                }
                TargetScreen.HOME_SCREEN -> {
                    Log.d("WallpaperService", "Applying to HOME_SCREEN")
                    wallpaperManager.setBitmap(resizedBitmap, null, true, WallpaperManager.FLAG_SYSTEM)
                }
                TargetScreen.BOTH -> {
                    Log.d("WallpaperService", "Applying to BOTH screens")
                    // Pour BOTH, appliquer le même fond d'écran aux deux écrans
                    wallpaperManager.setBitmap(resizedBitmap, null, true, WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK)
                }
            }
            
            Log.d("WallpaperService", "Wallpaper applied successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("WallpaperService", "Error applying wallpaper", e)
            Result.failure(e)
        }
    }
    
    /**
     * Charge un bitmap depuis un chemin de fichier
     */
    private fun loadBitmap(filePath: String): Bitmap? {
        return try {
            val file = File(filePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(filePath)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Redimensionne un bitmap pour remplir complètement l'écran sans bandes noires
     * Utilise le scale le plus grand pour couvrir toute la zone, puis recadre au centre si nécessaire
     */
    private fun resizeBitmapToFitScreen(bitmap: Bitmap, targetWidth: Int, targetHeight: Int): Bitmap {
        val bitmapWidth = bitmap.width
        val bitmapHeight = bitmap.height
        
        // Si le bitmap est déjà à la bonne taille, le retourner tel quel
        if (bitmapWidth == targetWidth && bitmapHeight == targetHeight) {
            return bitmap
        }
        
        // Calculer le ratio de mise à l'échelle pour couvrir toute la zone (pas de bandes noires)
        // Utiliser le scale le plus grand garantit que l'image remplira toujours l'écran
        val scaleX = targetWidth.toFloat() / bitmapWidth
        val scaleY = targetHeight.toFloat() / bitmapHeight
        val scale = maxOf(scaleX, scaleY) // Utiliser le plus grand pour couvrir toute la zone
        
        // Calculer les nouvelles dimensions
        val newWidth = (bitmapWidth * scale).toInt()
        val newHeight = (bitmapHeight * scale).toInt()
        
        // Redimensionner le bitmap
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        
        // Si les dimensions sont exactement celles cibles, retourner directement
        if (newWidth == targetWidth && newHeight == targetHeight) {
            return resizedBitmap
        }
        
        // Sinon, recadrer au centre pour obtenir exactement la taille cible
        val startX = (newWidth - targetWidth) / 2
        val startY = (newHeight - targetHeight) / 2
        
        val finalBitmap = Bitmap.createBitmap(
            resizedBitmap,
            startX.coerceAtLeast(0),
            startY.coerceAtLeast(0),
            minOf(targetWidth, newWidth),
            minOf(targetHeight, newHeight)
        )
        
        // Libérer la mémoire du bitmap redimensionné intermédiaire
        if (resizedBitmap != finalBitmap) {
            resizedBitmap.recycle()
        }
        
        return finalBitmap
    }
    
    /**
     * Vérifie si l'application a la permission SET_WALLPAPER
     */
    fun hasWallpaperPermission(): Boolean {
        return try {
            wallpaperManager.isSetWallpaperAllowed
        } catch (e: Exception) {
            false
        }
    }
}

