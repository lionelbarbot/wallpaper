package com.wallpaper.ui.screens

import android.app.Activity
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wallpaper.ui.viewmodel.ImageEditorViewModel
import com.wallpaper.ui.viewmodel.ImageEditorViewModelFactory
import com.yalantis.ucrop.UCrop
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageEditorScreen(
    imageId: Long,
    onNavigateBack: () -> Unit,
    viewModel: ImageEditorViewModel = viewModel(
        factory = ImageEditorViewModelFactory(LocalContext.current, imageId)
    )
) {
    val context = LocalContext.current
    val image by viewModel.image.collectAsState()
    var uCropStarted by remember { mutableStateOf(false) }
    
    LaunchedEffect(image) {
        Log.d("ImageEditor", "LaunchedEffect triggered, image: $image")
        image?.let { img ->
            val imagePath = img.filePath
            Log.d("ImageEditor", "Image path: $imagePath")
            if (imagePath.isNotEmpty() && !uCropStarted) {
                uCropStarted = true
                
                // Obtenir l'Activity depuis le contexte
                fun findActivity(ctx: android.content.Context): Activity? {
                    return when (ctx) {
                        is Activity -> ctx
                        is android.content.ContextWrapper -> findActivity(ctx.baseContext)
                        else -> null
                    }
                }
                val activity = findActivity(context)
                
                Log.d("ImageEditor", "Activity found: ${activity != null}")
                
                if (activity != null) {
                    try {
                        val file = File(imagePath)
                        Log.d("ImageEditor", "File exists: ${file.exists()}, path: ${file.absolutePath}")
                        
                        if (!file.exists()) {
                            Log.e("ImageEditor", "File does not exist: $imagePath")
                            onNavigateBack()
                            return@LaunchedEffect
                        }
                        
                        // Démarrer uCrop pour éditer l'image
                        val sourceUri = Uri.fromFile(file)
                        val destinationUri = Uri.fromFile(file) // Écrase l'original
                        
                        Log.d("ImageEditor", "Starting uCrop with source: $sourceUri, destination: $destinationUri")
                        
                        // Configurer les options uCrop pour une édition plus efficace
                        val options = com.yalantis.ucrop.UCrop.Options()
                        
                        // Qualité de compression JPEG (0-100, 100 = meilleure qualité)
                        options.setCompressionQuality(95)
                        
                        // Format de sortie
                        options.setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
                        
                        // Activer les gestes simultanés (multi-touch) pour zoom et rotation
                        options.setAllowedGestures(
                            com.yalantis.ucrop.UCropActivity.SCALE,
                            com.yalantis.ucrop.UCropActivity.ROTATE,
                            com.yalantis.ucrop.UCropActivity.ALL
                        )
                        
                        // Afficher les guides de recadrage pour un meilleur alignement
                        options.setShowCropGrid(true)
                        options.setShowCropFrame(true)
                        
                        // Activer la rotation libre
                        options.setFreeStyleCropEnabled(true)
                        
                        // Personnaliser les couleurs de l'interface
                        // Utiliser une couleur primaire par défaut (bleu Material)
                        val primaryColorInt = android.graphics.Color.parseColor("#6200EE")
                        options.setToolbarColor(primaryColorInt)
                        options.setStatusBarColor(primaryColorInt)
                        options.setActiveControlsWidgetColor(primaryColorInt)
                        
                        // Utiliser start() avec les options - uCrop gère son propre cycle de vie
                        // Pas de ratio d'aspect fixe pour permettre le recadrage libre
                        UCrop.of(sourceUri, destinationUri)
                            .withOptions(options)
                            .withMaxResultSize(1920, 1080)
                            .start(activity)
                        
                        // uCrop va ouvrir une nouvelle activité
                        // Quand uCrop se ferme, onActivityResult() dans MainActivity sera appelé
                        // et onResume() sera appelé, ce qui rafraîchira les images dans FolderDetailScreen
                        // On attend un court délai pour s'assurer que uCrop s'est bien fermé
                        kotlinx.coroutines.delay(500)
                        Log.d("ImageEditor", "Navigating back after uCrop")
                        onNavigateBack()
                    } catch (e: Exception) {
                        Log.e("ImageEditor", "Error starting uCrop", e)
                        e.printStackTrace()
                        // En cas d'erreur, retourner à l'écran précédent
                        onNavigateBack()
                    }
                } else {
                    Log.e("ImageEditor", "Context is not an Activity: ${context.javaClass.name}")
                    // Si le contexte n'est pas une Activity, retourner
                    onNavigateBack()
                }
            }
        } ?: run {
            Log.d("ImageEditor", "Image is null")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Éditeur d'image") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            Text("Édition d'image en cours...")
        }
    }
}

