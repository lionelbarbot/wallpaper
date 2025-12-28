package com.wallpaper.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wallpaper.ui.components.ImageCropper
import com.wallpaper.ui.viewmodel.ImageEditorViewModel
import com.wallpaper.ui.viewmodel.ImageEditorViewModelFactory
import com.wallpaper.util.ImageCacheHelper

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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Éditeur d'image") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            image?.let { img ->
                val imagePath = img.filePath
                if (imagePath.isNotEmpty()) {
                    ImageCropper(
                        imagePath = imagePath,
                        onCropComplete = { savedPath ->
                            Log.d("ImageEditor", "Image cropped and saved: $savedPath")
                            // Invalider le cache pour forcer le rechargement
                            ImageCacheHelper.invalidateCache(context, savedPath)
                            onNavigateBack()
                        },
                        onCancel = {
                            Log.d("ImageEditor", "Crop cancelled")
                            onNavigateBack()
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("Chemin d'image invalide")
                }
            } ?: run {
                CircularProgressIndicator(
                    modifier = Modifier.align(androidx.compose.ui.Alignment.Center)
                )
            }
        }
    }
}

