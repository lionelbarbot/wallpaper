package com.wallpaper.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun ImageCropper(
    imagePath: String,
    onCropComplete: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    // État de transformation
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    // Zone de recadrage (cropRect)
    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var containerSize by remember { mutableStateOf(Size.Zero) }
    
    // Charger l'image
    LaunchedEffect(imagePath) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(imagePath)
                if (file.exists()) {
                    val loadedBitmap = BitmapFactory.decodeFile(imagePath)
                    bitmap = loadedBitmap
                    imageBitmap = loadedBitmap.asImageBitmap()
                } else {
                    Log.e("ImageCropper", "Image file not found: $imagePath")
                }
            } catch (e: Exception) {
                Log.e("ImageCropper", "Error loading image", e)
            }
        }
    }
    
    // Initialiser les dimensions et la zone de recadrage
    LaunchedEffect(imageBitmap, containerSize.width, containerSize.height) {
        imageBitmap?.let { img ->
            if (containerSize.width > 0 && containerSize.height > 0) {
                val imageWidth = img.width.toFloat()
                val imageHeight = img.height.toFloat()
                val containerWidth = containerSize.width
                val containerHeight = containerSize.height
                
                // Calculer l'échelle pour que l'image s'adapte au conteneur
                val scaleX = containerWidth / imageWidth
                val scaleY = containerHeight / imageHeight
                val initialScale = minOf(scaleX, scaleY) * 0.9f
                
                scale = initialScale
                offset = Offset(
                    (containerWidth - imageWidth * initialScale) / 2,
                    (containerHeight - imageHeight * initialScale) / 2
                )
                
                // Initialiser la zone de recadrage au centre (80% de la taille de l'image)
                val cropWidth = imageWidth * initialScale * 0.8f
                val cropHeight = imageHeight * initialScale * 0.8f
                cropRect = Rect(
                    offset = Offset(
                        (containerWidth - cropWidth) / 2,
                        (containerHeight - cropHeight) / 2
                    ),
                    size = Size(cropWidth, cropHeight)
                )
            }
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        imageBitmap?.let { img ->
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotationChange ->
                            scale = (scale * zoom).coerceIn(0.5f, 5f)
                            rotation += rotationChange
                            offset += pan
                        }
                    }
                    .onSizeChanged { size ->
                        containerSize = Size(size.width.toFloat(), size.height.toFloat())
                    }
            ) {
                val imageWidth = img.width.toFloat()
                val imageHeight = img.height.toFloat()
                
                // Appliquer les transformations avec withTransform
                withTransform({
                    translate(offset.x, offset.y)
                    val pivot = Offset(imageWidth / 2, imageHeight / 2)
                    scale(scale, scale, pivot)
                    rotate(rotation, pivot)
                }) {
                    // Dessiner l'image
                    drawImage(
                        image = img,
                        dstSize = IntSize(imageWidth.toInt(), imageHeight.toInt())
                    )
                }
                
                // Dessiner l'overlay sombre autour de la zone de recadrage
                cropRect?.let { rect ->
                    // Zone sombre en haut
                    drawRect(
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, rect.top)
                    )
                    // Zone sombre en bas
                    drawRect(
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(0f, rect.bottom),
                        size = Size(size.width, size.height - rect.bottom)
                    )
                    // Zone sombre à gauche
                    drawRect(
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(0f, rect.top),
                        size = Size(rect.left, rect.height)
                    )
                    // Zone sombre à droite
                    drawRect(
                        color = Color.Black.copy(alpha = 0.5f),
                        topLeft = Offset(rect.right, rect.top),
                        size = Size(size.width - rect.right, rect.height)
                    )
                    
                    // Dessiner le cadre de recadrage
                    drawRect(
                        color = Color.White,
                        style = Stroke(width = 3.dp.toPx()),
                        topLeft = rect.topLeft,
                        size = rect.size
                    )
                    
                    // Dessiner la grille de recadrage (3x3)
                    // Lignes verticales
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(rect.left + rect.width / 3, rect.top),
                        end = Offset(rect.left + rect.width / 3, rect.bottom),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(rect.left + rect.width * 2 / 3, rect.top),
                        end = Offset(rect.left + rect.width * 2 / 3, rect.bottom),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    // Lignes horizontales
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(rect.left, rect.top + rect.height / 3),
                        end = Offset(rect.right, rect.top + rect.height / 3),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(rect.left, rect.top + rect.height * 2 / 3),
                        end = Offset(rect.right, rect.top + rect.height * 2 / 3),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        } ?: run {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Barre d'outils en bas
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bouton Annuler
                TextButton(onClick = onCancel) {
                    Text("Annuler")
                }
                
                // Bouton Rotation
                IconButton(
                    onClick = {
                        rotation += 90f
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.RotateRight,
                        contentDescription = "Rotation"
                    )
                }
                
                // Bouton Valider
                Button(
                    onClick = {
                        bitmap?.let { bmp ->
                            cropRect?.let { rect ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    try {
                                        val imageWidth = bmp.width.toFloat()
                                        val imageHeight = bmp.height.toFloat()
                                        
                                        // Calculer les coordonnées de recadrage dans l'image originale
                                        // Le rectangle de recadrage est en coordonnées canvas
                                        // On doit le convertir en coordonnées image en tenant compte de scale et offset
                                        
                                        // Convertir les coordonnées du rectangle de recadrage (canvas) vers l'image
                                        // En tenant compte que l'image est centrée et transformée
                                        val imageCenterX = offset.x + imageWidth * scale / 2
                                        val imageCenterY = offset.y + imageHeight * scale / 2
                                        
                                        // Coordonnées du rectangle de recadrage par rapport au centre de l'image transformée
                                        val cropCenterX = (rect.left + rect.right) / 2
                                        val cropCenterY = (rect.top + rect.bottom) / 2
                                        
                                        // Convertir en coordonnées image (sans rotation pour l'instant)
                                        val relativeX = (cropCenterX - imageCenterX) / scale
                                        val relativeY = (cropCenterY - imageCenterY) / scale
                                        
                                        val cropWidthInImage = rect.width / scale
                                        val cropHeightInImage = rect.height / scale
                                        
                                        // Coordonnées dans l'image originale (avant rotation)
                                        val cropX = (imageWidth / 2 + relativeX - cropWidthInImage / 2).coerceIn(0f, imageWidth)
                                        val cropY = (imageHeight / 2 + relativeY - cropHeightInImage / 2).coerceIn(0f, imageHeight)
                                        val cropRight = (cropX + cropWidthInImage).coerceIn(0f, imageWidth)
                                        val cropBottom = (cropY + cropHeightInImage).coerceIn(0f, imageHeight)
                                        
                                        val finalCropX = cropX.toInt()
                                        val finalCropY = cropY.toInt()
                                        val finalCropWidth = (cropRight - cropX).toInt().coerceIn(1, bmp.width - finalCropX)
                                        val finalCropHeight = (cropBottom - cropY).toInt().coerceIn(1, bmp.height - finalCropY)
                                        
                                        // Recadrer d'abord, puis appliquer la rotation
                                        val croppedBitmap = Bitmap.createBitmap(
                                            bmp,
                                            finalCropX,
                                            finalCropY,
                                            finalCropWidth,
                                            finalCropHeight
                                        )
                                        
                                        // Appliquer la rotation si nécessaire
                                        val finalBitmap = if (rotation != 0f) {
                                            val rotationMatrix = Matrix().apply {
                                                postRotate(rotation)
                                            }
                                            val rotated = Bitmap.createBitmap(
                                                croppedBitmap,
                                                0,
                                                0,
                                                croppedBitmap.width,
                                                croppedBitmap.height,
                                                rotationMatrix,
                                                true
                                            )
                                            croppedBitmap.recycle()
                                            rotated
                                        } else {
                                            croppedBitmap
                                        }
                                        
                                        // Sauvegarder l'image recadrée
                                        val file = File(imagePath)
                                        FileOutputStream(file).use { out ->
                                            finalBitmap.compress(
                                                Bitmap.CompressFormat.JPEG,
                                                95,
                                                out
                                            )
                                        }
                                        
                                        finalBitmap.recycle()
                                        
                                        onCropComplete(imagePath)
                                    } catch (e: Exception) {
                                        Log.e("ImageCropper", "Error saving cropped image", e)
                                    }
                                }
                            }
                        }
                    }
                ) {
                    Text("Valider")
                }
            }
        }
    }
}
