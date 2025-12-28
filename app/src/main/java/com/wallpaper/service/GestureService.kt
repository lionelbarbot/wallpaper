package com.wallpaper.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
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
    private var lastTapTime = System.currentTimeMillis() // Initialiser avec le temps actuel
    private val tapTimeout = 2000L // 2000ms (2 secondes) pour considérer les taps comme une séquence
    private var pendingDoubleTap: Runnable? = null
    
    private lateinit var wallpaperService: WallpaperService
    private lateinit var serviceRepository: WallpaperServiceRepository
    private lateinit var imageRepository: WallpaperImageRepository
    private var currentImageId: Long? = null
    private var currentFolderId: Long? = null
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d("GestureService", "Service connected")
        initializeServices()
    }
    
    private fun initializeServices() {
        Log.d("GestureService", "Initializing services")
        wallpaperService = WallpaperService(this)
        val database = WallpaperDatabase.getDatabase(this)
        val dao = database.wallpaperDao()
        val recurrenceCalculator = RecurrenceCalculator()
        val folderRepository = WallpaperFolderRepository(dao, recurrenceCalculator)
        imageRepository = WallpaperImageRepository(dao)
        serviceRepository = WallpaperServiceRepository(folderRepository, imageRepository)
        
        // Initialiser les IDs actuels
        serviceScope.launch {
            val (folder, image) = serviceRepository.getActiveWallpaper()
            currentFolderId = folder?.id
            currentImageId = image?.id
            Log.d("GestureService", "Initialized: folderId=$currentFolderId, imageId=$currentImageId")
        }
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        
        // Filtrer uniquement les événements pertinents pour réduire le bruit
        val eventType = event.eventType
        val packageName = event.packageName?.toString() ?: ""
        val className = event.className?.toString() ?: ""
        
        // Ne traiter que les événements du launcher ou de l'écran d'accueil
        val isHomeScreen = packageName.contains("launcher", ignoreCase = true) || 
                          packageName.contains("systemui", ignoreCase = true) ||
                          packageName == "com.wallpaper"
        
        if (!isHomeScreen) {
            return
        }
        
        // Détecter les clics sur l'écran d'accueil
        if (eventType == TYPE_VIEW_CLICKED) {
            // Vérifier si le clic est sur un élément interactif (widget, icône, etc.)
            val isInteractiveElement = isInteractiveElement(event, className)
            
            Log.d("GestureService", "Event received: type=$eventType, className=$className, packageName=$packageName, isInteractive=$isInteractiveElement")
            
            // Ne traiter que les clics sur des zones NON interactives (fond d'écran vide)
            if (!isInteractiveElement) {
                Log.d("GestureService", "Click on empty area detected, processing gesture")
                handleTap()
            } else {
                Log.d("GestureService", "Click on interactive element ignored (widget/icon/app)")
            }
        }
    }
    
    /**
     * Vérifie si l'élément cliqué est interactif (widget, icône, application, etc.)
     * Retourne true si c'est un élément interactif, false si c'est une zone vide
     */
    private fun isInteractiveElement(event: AccessibilityEvent, className: String): Boolean {
        // Liste des classes qui indiquent des éléments interactifs
        val interactiveClasses = listOf(
            "ImageView",           // Icônes d'applications
            "TextView",            // Textes, labels de widgets
            "Button",              // Boutons
            "ImageButton",         // Boutons avec images
            "AppWidgetHostView",   // Widgets
            "LauncherAppWidgetHostView", // Widgets du launcher
            "BubbleTextView",      // Icônes de certaines versions de launcher
            "CellLayout",          // Cellules de grille avec contenu
            "ShortcutAndWidgetContainer", // Conteneur de widgets
            "RecyclerView"         // Listes défilantes
        )
        
        // Vérifier si la classe correspond directement à un élément interactif
        if (interactiveClasses.any { className.contains(it, ignoreCase = true) }) {
            return true
        }
        
        // Vérifier la source de l'événement pour plus de précision
        val source = event.source
        if (source != null) {
            try {
                // Pour les FrameLayout et ViewGroup, vérifier s'ils contiennent des éléments interactifs
                val isContainer = className.contains("FrameLayout", ignoreCase = true) || 
                                 className.contains("ViewGroup", ignoreCase = true) ||
                                 className.contains("LinearLayout", ignoreCase = true) ||
                                 className.contains("RelativeLayout", ignoreCase = true)
                
                if (isContainer) {
                    // Vérifier si le conteneur a des enfants interactifs
                    val childCount = source.childCount
                    var hasInteractiveChild = false
                    
                    if (childCount > 0) {
                        // Vérifier les enfants pour voir s'ils sont interactifs
                        for (i in 0 until minOf(childCount, 20)) { // Limiter à 20 enfants pour performance
                            val child = source.getChild(i) ?: continue
                            val childClassName = child.className?.toString() ?: ""
                            
                            // Si un enfant correspond à une classe interactive, le parent l'est aussi
                            if (interactiveClasses.any { childClassName.contains(it, ignoreCase = true) }) {
                                hasInteractiveChild = true
                                break
                            }
                            
                            // Vérifier aussi si l'enfant est cliquable ET a du contenu visible
                            try {
                                if ((child.isClickable || child.isFocusable) && childClassName !in listOf("FrameLayout", "ViewGroup", "LinearLayout", "RelativeLayout")) {
                                    // Vérifier si l'enfant a du texte ou une description (indique du contenu)
                                    val contentDescription = child.contentDescription?.toString()
                                    val text = child.text?.toString()
                                    if (!contentDescription.isNullOrEmpty() || !text.isNullOrEmpty()) {
                                        hasInteractiveChild = true
                                        break
                                    }
                                }
                            } catch (e: Exception) {
                                // Ignorer les erreurs de vérification
                            }
                        }
                    }
                    
                    // Si le conteneur a des enfants interactifs, c'est interactif
                    if (hasInteractiveChild) {
                        return true
                    }
                    
                    // Si c'est un conteneur sans enfants interactifs, c'est une zone vide
                    // Même s'il est cliquable, on le considère comme zone vide pour permettre les gestes
                    Log.d("GestureService", "Container detected as empty area: className=$className, childCount=$childCount")
                    return false
                }
                
                // Pour les autres éléments, vérifier s'ils sont cliquables ou focusable
                if (source.isClickable || source.isFocusable) {
                    return true
                }
            } catch (e: Exception) {
                Log.w("GestureService", "Error checking element interactivity", e)
            } finally {
                source.recycle()
            }
        }
        
        // Par défaut, considérer comme non interactif (zone vide)
        return false
    }
    
    /**
     * Gère la détection des taps pour les gestes double/quadruple tap
     */
    private fun handleTap() {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastTap = currentTime - lastTapTime
        
        Log.d("GestureService", "Click detected: tapCount=$tapCount, timeSinceLastTap=${timeSinceLastTap}ms")
        
        // Annuler le callback précédent s'il existe
        pendingDoubleTap?.let { handler.removeCallbacks(it) }
        pendingDoubleTap = null
        
        // Réinitialiser le compteur si trop de temps s'est écoulé OU si c'est le premier tap après une longue période
        if (timeSinceLastTap > tapTimeout || (tapCount == 0 && timeSinceLastTap > 10000)) {
            Log.d("GestureService", "Resetting tap count (timeout: ${timeSinceLastTap}ms > ${tapTimeout}ms)")
            tapCount = 0
        }
        
        tapCount++
        lastTapTime = currentTime
        
        Log.d("GestureService", "Updated tapCount=$tapCount")
        
        // Détecter double tap (2 taps)
        if (tapCount == 2) {
            Log.d("GestureService", "Double tap detected! Executing immediately")
            // Exécuter immédiatement au lieu d'attendre le timeout
            handleDoubleTap()
            tapCount = 0
        }
        
        // Détecter quadruple tap (4 taps)
        if (tapCount == 4) {
            Log.d("GestureService", "Quadruple tap detected! Executing immediately")
            handleQuadrupleTap()
            tapCount = 0
        }
    }
    
    /**
     * Détecte les gestes directement (Android 7.0+)
     * Cette méthode peut être utilisée pour détecter les gestes sur le fond d'écran
     */
    override fun onGesture(gestureId: Int): Boolean {
        Log.d("GestureService", "onGesture called with gestureId=$gestureId")
        // Pour l'instant, on utilise toujours onAccessibilityEvent
        // mais cette méthode peut être utilisée pour une détection plus précise
        return super.onGesture(gestureId)
    }
    
    private fun handleDoubleTap() {
        Log.d("GestureService", "handleDoubleTap called")
        serviceScope.launch {
            try {
                Log.d("GestureService", "Getting next image, currentImageId=$currentImageId")
                val nextImage = serviceRepository.getNextImage(currentImageId)
                Log.d("GestureService", "Next image: ${nextImage?.id}")
                
                if (nextImage != null) {
                    val activeFolder = serviceRepository.getActiveWallpaper().first
                    Log.d("GestureService", "Active folder: ${activeFolder?.name}")
                    
                    if (activeFolder != null) {
                        Log.d("GestureService", "Applying wallpaper: ${nextImage.filePath}")
                        val result = wallpaperService.applyWallpaper(activeFolder, nextImage)
                        if (result.isSuccess) {
                            currentImageId = nextImage.id
                            Log.d("GestureService", "Wallpaper changed successfully, new imageId=$currentImageId")
                        } else {
                            Log.e("GestureService", "Failed to apply wallpaper: ${result.exceptionOrNull()?.message}")
                        }
                    } else {
                        Log.w("GestureService", "No active folder found")
                    }
                } else {
                    Log.w("GestureService", "No next image found")
                }
            } catch (e: Exception) {
                Log.e("GestureService", "Error in handleDoubleTap", e)
            }
        }
    }
    
    private fun handleQuadrupleTap() {
        Log.d("GestureService", "handleQuadrupleTap called")
        serviceScope.launch(Dispatchers.IO) {
            try {
                Log.d("GestureService", "Getting next folder, currentFolderId=$currentFolderId")
                val nextFolder = serviceRepository.getNextFolder(currentFolderId)
                Log.d("GestureService", "Next folder: ${nextFolder?.name}")
                
                if (nextFolder != null) {
                    // Récupérer la première image du nouveau répertoire
                    val firstImage = imageRepository.getFirstImageByFolderId(nextFolder.id)
                    Log.d("GestureService", "First image of next folder: ${firstImage?.id}")
                    
                    if (firstImage != null) {
                        Log.d("GestureService", "Applying wallpaper: ${firstImage.filePath}")
                        val result = wallpaperService.applyWallpaper(nextFolder, firstImage)
                        if (result.isSuccess) {
                            currentFolderId = nextFolder.id
                            currentImageId = firstImage.id
                            Log.d("GestureService", "Wallpaper changed successfully, new folderId=$currentFolderId, imageId=$currentImageId")
                        } else {
                            Log.e("GestureService", "Failed to apply wallpaper: ${result.exceptionOrNull()?.message}")
                            result.exceptionOrNull()?.printStackTrace()
                        }
                    } else {
                        Log.w("GestureService", "No image found in next folder")
                    }
                } else {
                    Log.w("GestureService", "No next folder found")
                }
            } catch (e: Exception) {
                Log.e("GestureService", "Error in handleQuadrupleTap", e)
                e.printStackTrace()
            }
        }
    }
    
    override fun onInterrupt() {
        // Service interrompu
    }
}

