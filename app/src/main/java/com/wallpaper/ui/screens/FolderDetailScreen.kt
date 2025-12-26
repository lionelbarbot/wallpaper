package com.wallpaper.ui.screens

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.wallpaper.R
import com.wallpaper.util.ImageCacheHelper
import com.wallpaper.data.database.WallpaperDatabase
import com.wallpaper.data.database.entities.WallpaperImage
import com.wallpaper.data.repository.WallpaperImageRepository
import com.wallpaper.domain.usecase.RecurrenceCalculator
import com.wallpaper.ui.viewmodel.FolderDetailViewModel
import com.wallpaper.ui.viewmodel.FolderDetailViewModelFactory
import com.wallpaper.util.ImagePicker
import com.wallpaper.util.PermissionHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(
    folderId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditImage: (Long, String) -> Unit,
    viewModel: FolderDetailViewModel = viewModel(
        factory = FolderDetailViewModelFactory(LocalContext.current, folderId)
    )
) {
    val context = LocalContext.current
    val images by viewModel.images.collectAsState()
    val folder by viewModel.folder.collectAsState()
    var imageRefreshKey by remember { mutableStateOf(0) }
    
    // Surveiller onResume pour rafraîchir les images après uCrop
    val activity = context as? Activity
    DisposableEffect(activity) {
        if (activity is LifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    // Quand on revient sur l'écran, rafraîchir les images
                    // Cela permet de voir les images éditées immédiatement
                    imageRefreshKey++
                    // Invalider le cache Coil pour toutes les images
                    images.forEach { image ->
                        ImageCacheHelper.invalidateCache(context, image.filePath)
                    }
                }
            }
            activity.lifecycle.addObserver(observer)
            onDispose {
                activity.lifecycle.removeObserver(observer)
            }
        } else {
            onDispose { }
        }
    }
    
    val pickImage = ImagePicker.rememberImagePicker { imagePath ->
        CoroutineScope(Dispatchers.IO).launch {
            val database = WallpaperDatabase.getDatabase(context)
            val dao = database.wallpaperDao()
            val imageRepository = WallpaperImageRepository(dao)
            val imageCount = imageRepository.getImageCountByFolderId(folderId)
            val newImage = WallpaperImage(
                folderId = folderId,
                filePath = imagePath,
                displayOrder = imageCount
            )
            imageRepository.insertImage(newImage)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folder?.name ?: stringResource(R.string.folder_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (PermissionHelper.hasStoragePermission(context)) {
                        pickImage()
                    }
                }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_image))
            }
        }
    ) { padding ->
        if (images.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_images))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(images, key = { it.id }) { image ->
                    ImageItem(
                        image = image,
                        onClick = { onNavigateToEditImage(image.id, image.filePath) },
                        onDelete = { viewModel.deleteImage(image) },
                        refreshKey = imageRefreshKey
                    )
                }
            }
        }
    }
}

@Composable
fun ImageItem(
    image: WallpaperImage,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    refreshKey: Int = 0
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val file = File(image.filePath)
    val context = LocalContext.current
    
    // Utiliser refreshKey pour forcer le rechargement de l'image
    // Invalider le cache quand refreshKey change
    LaunchedEffect(refreshKey) {
        if (refreshKey > 0) {
            ImageCacheHelper.invalidateCache(context, image.filePath)
        }
    }
    
    // Créer un Uri avec un paramètre de requête pour forcer le rechargement quand refreshKey change
    val imageUri = remember(refreshKey, image.filePath) {
        if (refreshKey > 0 && file.exists()) {
            // Ajouter un timestamp au URI pour forcer le rechargement
            android.net.Uri.fromFile(file).buildUpon()
                .appendQueryParameter("t", refreshKey.toString())
                .build()
        } else {
            android.net.Uri.fromFile(file)
        }
    }
    
    Card(
        modifier = Modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick)
    ) {
        Box {
            AsyncImage(
                model = imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text("Supprimer cette image ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

