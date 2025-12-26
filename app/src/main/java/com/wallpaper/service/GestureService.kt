package com.wallpaper.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED
import android.os.Handler
import android.os.Looper
import com.wallpaper.WallpaperApplication
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.repository.WallpaperFolderRepository
import com.wallpaper.data.repository.WallpaperImageRepository
import com.wallpaper.data.repository.WallpaperServiceRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GestureService : AccessibilityService() {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())
    
    private var tapCount = 0
    private var lastTapTime = 0L
    private val tapTimeout = 500L // 500ms pour considérer les taps comme une séquence
    
    private lateinit var wallpaperService: WallpaperService
    private lateinit var serviceRepository: WallpaperServiceRepository
    private var currentImageId: Long? = null
    private var currentFolderId: Long? = null
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        initializeServices()
    }
    
    private fun initializeServices() {
        wallpaperService = WallpaperService(this)
        val database = WallpaperDatabase.getDatabase(this)
        val dao = database.wallpaperDao()
        val recurrenceCalculator = RecurrenceCalculator()
        val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
        val imageRepository = WallpaperImageRepository(dao)
        serviceRepository = WallpaperServiceRepository(folderRepository, imageRepository)
        
        // Initialiser les IDs actuels
        serviceScope.launch {
            val (folder, image) = serviceRepository.getActiveWallpaper()
            currentFolderId = folder?.id
            currentImageId = image?.id
        }
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // Détecter uniquement les clics sur l'écran d'accueil
        if (event.eventType == TYPE_VIEW_CLICKED) {
            val currentTime = System.currentTimeMillis()
            
            // Réinitialiser le compteur si trop de temps s'est écoulé
            if (currentTime - lastTapTime > tapTimeout) {
                tapCount = 0
            }
            
            tapCount++
            lastTapTime = currentTime
            
            // Détecter double tap (2 taps)
            if (tapCount == 2) {
                handler.postDelayed({
                    if (tapCount == 2) {
                        handleDoubleTap()
                        tapCount = 0
                    }
                }, tapTimeout)
            }
            
            // Détecter quadruple tap (4 taps)
            if (tapCount == 4) {
                handler.removeCallbacksAndMessages(null)
                handleQuadrupleTap()
                tapCount = 0
            }
        }
    }
    
    private fun handleDoubleTap() {
        serviceScope.launch {
            try {
                val nextImage = serviceRepository.getNextImage(currentImageId)
                if (nextImage != null) {
                    val activeFolder = serviceRepository.getActiveWallpaper().first
                    if (activeFolder != null) {
                        wallpaperService.applyWallpaper(activeFolder, nextImage)
                        currentImageId = nextImage.id
                    }
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }
    
    private fun handleQuadrupleTap() {
        serviceScope.launch {
            try {
                val nextFolder = serviceRepository.getNextFolder(currentFolderId)
                if (nextFolder != null) {
                    val firstImage = serviceRepository.getActiveWallpaper().second
                    if (firstImage != null) {
                        wallpaperService.applyWallpaper(nextFolder, firstImage)
                        currentFolderId = nextFolder.id
                        currentImageId = firstImage.id
                    }
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }
    
    override fun onInterrupt() {
        // Service interrompu
    }
}

