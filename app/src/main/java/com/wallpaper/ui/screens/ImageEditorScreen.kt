package com.wallpaper.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wallpaper.R
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
    
    LaunchedEffect(image) {
        image?.let { img ->
            val imagePath = img.filePath
            if (imagePath.isNotEmpty() && context is android.app.Activity) {
                // Démarrer uCrop pour éditer l'image
                val sourceUri = Uri.fromFile(File(imagePath))
                val destinationUri = Uri.fromFile(File(imagePath)) // Écrase l'original
                
                UCrop.of(sourceUri, destinationUri)
                    .withAspectRatio(16f, 9f)
                    .withMaxResultSize(1920, 1080)
                    .start(context as android.app.Activity)
            }
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

