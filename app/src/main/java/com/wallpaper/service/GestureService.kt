package com.wallpaper.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityEvent.TYPE_VIEW_CLICKED
import android.os.Handler
import android.os.Looper
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
    private var lastTapTime = System.currentTimeMillis()
    private val tapTimeout = 400L // Reduced to a standard double-tap timeout
    
    private lateinit var wallpaperService: WallpaperService
    private lateinit var serviceRepository: WallpaperServiceRepository
    private lateinit var folderRepository: WallpaperFolderRepository
    private lateinit var imageRepository: WallpaperImageRepository
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        initializeServices()
    }
    
    private fun initializeServices() {
        wallpaperService = WallpaperService(this)
        val database = WallpaperDatabase.getDatabase(this)
        val dao = database.wallpaperDao()
        val recurrenceCalculator = RecurrenceCalculator()
        folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
        imageRepository = WallpaperImageRepository(dao)
        serviceRepository = WallpaperServiceRepository(folderRepository, imageRepository, this)
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != TYPE_VIEW_CLICKED) return
        
        val packageName = event.packageName?.toString() ?: ""
        val className = event.className?.toString() ?: ""
        
        val isHomeScreen = packageName.contains("launcher", ignoreCase = true) || 
                          packageName.contains("systemui", ignoreCase = true)
        
        if (isHomeScreen && !isInteractiveElement(event, className)) {
            handleTap()
        }
    }
    
    private fun isInteractiveElement(event: AccessibilityEvent, className: String): Boolean {
        val interactiveClasses = listOf(
            "ImageView", "TextView", "Button", "ImageButton", 
            "AppWidgetHostView", "LauncherAppWidgetHostView", 
            "BubbleTextView", "RecyclerView"
        )
        
        if (interactiveClasses.any { className.contains(it, ignoreCase = true) }) {
            return true
        }
        
        val source = event.source
        if (source != null) {
            try {
                if (source.isClickable || source.isFocusable) {
                    val isLayout = className.contains("Layout", ignoreCase = true) || 
                                 className.contains("ViewGroup", ignoreCase = true)
                    
                    if (isLayout && source.childCount == 0) {
                        return false // Empty layout clickable = background
                    }
                    return !isLayout
                }
            } finally {
                source.recycle()
            }
        }
        return false
    }
    
    private fun handleTap() {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime > tapTimeout) {
            tapCount = 0
        }
        
        tapCount++
        lastTapTime = currentTime
        
        when (tapCount) {
            2 -> handleDoubleTap()
            4 -> handleQuadrupleTap()
        }
    }
    
    private fun handleDoubleTap() {
        serviceScope.launch {
            val activeFolder = folderRepository.getActiveFolder() ?: return@launch
            val nextImage = serviceRepository.getNextImage(activeFolder.id)
            if (nextImage != null) {
                wallpaperService.applyWallpaper(activeFolder, nextImage)
            }
        }
    }
    
    private fun handleQuadrupleTap() {
        serviceScope.launch {
            val nextFolder = serviceRepository.getNextFolder() ?: return@launch
                    // Utiliser l'ordre aléatoire si activé, sinon la première image
                    val firstImage = if (nextFolder.randomOrder) {
                        imageRepository.getRandomImage(nextFolder.id)
                    } else {
                        imageRepository.getFirstImageByFolderId(nextFolder.id)
                    }
            if (firstImage != null) {
                wallpaperService.applyWallpaper(nextFolder, firstImage)
            }
        }
    }
    
    override fun onInterrupt() {}
}
