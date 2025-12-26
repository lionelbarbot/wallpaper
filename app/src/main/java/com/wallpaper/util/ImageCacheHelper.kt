package com.wallpaper.util

import android.content.Context
import coil.ImageLoader
import coil.memory.MemoryCache
import java.io.File

object ImageCacheHelper {
    /**
     * Invalide le cache Coil pour un fichier spécifique
     */
    fun invalidateCache(context: Context, filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            val imageLoader = ImageLoader(context)
            val cacheKey = file.absolutePath
            
            // Invalider le cache mémoire
            val memoryCache = imageLoader.memoryCache
            memoryCache?.remove(MemoryCache.Key(cacheKey))
            
            // Invalider le cache disque
            val diskCache = imageLoader.diskCache
            diskCache?.remove(cacheKey)
        }
    }
    
    /**
     * Invalide tout le cache Coil
     */
    fun clearAllCache(context: Context) {
        val imageLoader = ImageLoader(context)
        imageLoader.memoryCache?.clear()
        imageLoader.diskCache?.clear()
    }
}

