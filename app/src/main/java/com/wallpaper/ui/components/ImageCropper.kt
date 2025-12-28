package com.wallpaper.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

enum class AspectRatio(val label: String, val ratio: Float?) {
    FREE("Libre", null),
    SCREEN("Écran", -1f),
    SQUARE("1:1", 1f),
    PORTRAIT("9:16", 9f/16f),
    LANDSCAPE("16:9", 16f/9f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropper(
    imagePath: String,
    onCropComplete: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val coroutineScope = rememberCoroutineScope()
    
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var imageBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isFlippedHorizontal by remember { mutableStateOf(false) }
    
    val screenRatio = configuration.screenHeightDp.toFloat() / configuration.screenWidthDp.toFloat()
    var selectedRatio by remember { mutableStateOf(AspectRatio.SCREEN) }
    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var containerSize by remember { mutableStateOf(Size.Zero) }
    
    var isDraggingCropRect by remember { mutableStateOf(false) }
    var isResizingCropRect by remember { mutableStateOf(false) }
    var resizeCorner by remember { mutableStateOf(-1) }
    var cropRectStart by remember { mutableStateOf<Rect?>(null) }

    LaunchedEffect(imagePath) {
        withContext(Dispatchers.IO) {
            try {
                BitmapFactory.decodeFile(imagePath)?.let {
                    bitmap = it
                    imageBitmap = it.asImageBitmap()
                }
            } catch (e: Exception) {
                Log.e("ImageCropper", "Load error", e)
            }
        }
    }

    LaunchedEffect(containerSize, imageBitmap) {
        if (containerSize.width > 0 && imageBitmap != null && cropRect == null) {
            val screenAspectRatio = containerSize.height / containerSize.width
            val cropWidth = containerSize.width * 0.8f
            val cropHeight = cropWidth * screenAspectRatio
            
            cropRect = Rect(
                offset = Offset(
                    (containerSize.width - cropWidth) / 2,
                    (containerSize.height - cropHeight) / 2
                ),
                size = Size(cropWidth, cropHeight)
            )
            
            val img = imageBitmap!!
            val scaleX = containerSize.width / img.width
            val scaleY = containerSize.height / img.height
            scale = maxOf(scaleX, scaleY)
            offset = Offset(
                (containerSize.width - img.width * scale) / 2,
                (containerSize.height - img.height * scale) / 2
            )
        }
    }

    Column(modifier = modifier.fillMaxSize().background(Color.Black)) {
        Box(modifier = Modifier.weight(1f).onSizeChanged { 
            containerSize = Size(it.width.toFloat(), it.height.toFloat()) 
        }) {
            imageBitmap?.let { img ->
                Canvas(
                    modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, rot ->
                                scale = (scale * zoom).coerceIn(0.1f, 10f)
                                rotation += rot
                                offset += pan
                            }
                        }
                        .pointerInput(cropRect) {
                            detectDragGestures(
                                onDragStart = { startOffset ->
                                    cropRect?.let { rect ->
                                        val touchMargin = 40.dp.toPx()
                                        val corners = listOf(rect.topLeft, Offset(rect.right, rect.top), Offset(rect.right, rect.bottom), Offset(rect.left, rect.bottom))
                                        resizeCorner = corners.indexOfFirst { (it - startOffset).getDistance() < touchMargin }
                                        
                                        if (resizeCorner != -1) {
                                            isResizingCropRect = true
                                        } else if (rect.contains(startOffset)) {
                                            isDraggingCropRect = true
                                        }
                                        cropRectStart = rect
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    cropRectStart?.let { start ->
                                        if (isResizingCropRect) {
                                            val ratio = if (selectedRatio == AspectRatio.SCREEN) containerSize.height/containerSize.width 
                                                       else selectedRatio.ratio
                                            
                                            cropRect = applyResize(start, dragAmount, resizeCorner, ratio, containerSize)
                                        } else if (isDraggingCropRect) {
                                            cropRect = Rect(
                                                offset = Offset(
                                                    (start.left + dragAmount.x).coerceIn(0f, containerSize.width - start.width),
                                                    (start.top + dragAmount.y).coerceIn(0f, containerSize.height - start.height)
                                                ),
                                                size = start.size
                                            )
                                        }
                                        cropRectStart = cropRect
                                    }
                                },
                                onDragEnd = {
                                    isResizingCropRect = false
                                    isDraggingCropRect = false
                                }
                            )
                        }
                ) {
                    withTransform({
                        translate(offset.x, offset.y)
                        val pivot = Offset(img.width / 2f, img.height / 2f)
                        scale(scaleX = if (isFlippedHorizontal) -scale else scale, scaleY = scale, pivot = pivot)
                        rotate(rotation, pivot)
                    }) {
                        drawImage(img, dstSize = IntSize(img.width, img.height))
                    }

                    cropRect?.let { rect ->
                        val path = Path().apply {
                            addRect(Rect(Offset.Zero, containerSize))
                            addRect(rect)
                            fillType = PathFillType.EvenOdd
                        }
                        drawPath(path, Color.Black.copy(alpha = 0.7f))
                        drawRect(color = Color.White, topLeft = rect.topLeft, size = rect.size, style = Stroke(2.dp.toPx()))
                        
                        val cSize = 10.dp.toPx()
                        listOf(rect.topLeft, Offset(rect.right, rect.top), Offset(rect.right, rect.bottom), Offset(rect.left, rect.bottom)).forEach {
                            drawRect(color = Color.White, topLeft = Offset(it.x - cSize, it.y - cSize), size = Size(cSize*2, cSize*2))
                        }
                    }
                }
            }
        }

        Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
            Column {
                Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    AspectRatio.values().forEach { ratio ->
                        FilterChip(
                            selected = selectedRatio == ratio,
                            onClick = { 
                                selectedRatio = ratio
                                cropRect = resetCropRect(containerSize, ratio)
                            },
                            label = { Text(ratio.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                
                Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onCancel() }) { Icon(Icons.Default.Close, "Annuler") }
                    IconButton(onClick = { rotation -= 90f }) { Icon(Icons.Default.RotateLeft, "Rotation") }
                    IconButton(onClick = { isFlippedHorizontal = !isFlippedHorizontal }) { Icon(Icons.Default.Flip, "Miroir") }
                    Button(onClick = {
                        coroutineScope.launch {
                            val result = processCrop(bitmap!!, cropRect!!, scale, rotation, offset, isFlippedHorizontal, containerSize)
                            saveBitmap(result, imagePath)
                            onCropComplete(imagePath)
                        }
                    }) { Text("Enregistrer") }
                }
            }
        }
    }
}

private fun resetCropRect(containerSize: Size, ratio: AspectRatio): Rect {
    val r = when(ratio) {
        AspectRatio.FREE -> null
        AspectRatio.SCREEN -> containerSize.height / containerSize.width
        else -> ratio.ratio!!
    }
    
    val w = containerSize.width * 0.8f
    val h = if (r != null) w * r else w
    val finalH = if (h > containerSize.height * 0.8f) containerSize.height * 0.8f else h
    val finalW = if (r != null) finalH / r else w

    return Rect(
        offset = Offset((containerSize.width - finalW)/2, (containerSize.height - finalH)/2),
        size = Size(finalW, finalH)
    )
}

private fun applyResize(start: Rect, delta: Offset, corner: Int, ratio: Float?, container: Size): Rect {
    var newLeft = start.left
    var newTop = start.top
    var newRight = start.right
    var newBottom = start.bottom

    when (corner) {
        0 -> { newLeft += delta.x; newTop += delta.y }
        1 -> { newRight += delta.x; newTop += delta.y }
        2 -> { newRight += delta.x; newBottom += delta.y }
        3 -> { newLeft += delta.x; newBottom += delta.y }
    }

    var width = (newRight - newLeft).coerceAtLeast(100f)
    var height = (newBottom - newTop).coerceAtLeast(100f)

    if (ratio != null) {
        if (abs(delta.x) > abs(delta.y)) {
            height = width * ratio
        } else {
            width = height / ratio
        }
    }

    return Rect(offset = Offset(newLeft, newTop), size = Size(width, height))
}

private suspend fun processCrop(src: Bitmap, crop: Rect, scale: Float, rot: Float, offset: Offset, flip: Boolean, container: Size): Bitmap = withContext(Dispatchers.IO) {
    // Créer un bitmap de la taille du rectangle de recadrage
    val result = Bitmap.createBitmap(crop.width.toInt(), crop.height.toInt(), Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)
    
    // Créer la matrice de transformation complète pour l'image
    val transformMatrix = Matrix()
    
    // Centre de l'image originale
    val imageCenterX = src.width / 2f
    val imageCenterY = src.height / 2f
    
    // 1. Déplacer l'origine au centre de l'image
    transformMatrix.postTranslate(-imageCenterX, -imageCenterY)
    
    // 2. Appliquer le retournement horizontal si nécessaire
    if (flip) {
        transformMatrix.postScale(-1f, 1f)
    }
    
    // 3. Appliquer la rotation autour du centre
    transformMatrix.postRotate(rot)
    
    // 4. Appliquer le scale
    transformMatrix.postScale(scale, scale)
    
    // 5. Déplacer l'image selon l'offset dans le canvas principal
    transformMatrix.postTranslate(offset.x, offset.y)
    
    // Maintenant, on veut extraire la partie qui correspond au rectangle de recadrage
    // On doit déplacer le canvas pour que le coin supérieur gauche du rectangle de recadrage
    // corresponde au coin supérieur gauche du canvas de résultat
    transformMatrix.postTranslate(-crop.left, -crop.top)
    
    // Dessiner l'image avec la transformation
    val paint = android.graphics.Paint(android.graphics.Paint.FILTER_BITMAP_FLAG)
    paint.isAntiAlias = true
    paint.isFilterBitmap = true
    
    // Clipper le canvas pour ne dessiner que dans les limites du rectangle de recadrage
    canvas.save()
    canvas.clipRect(0f, 0f, crop.width, crop.height)
    canvas.drawBitmap(src, transformMatrix, paint)
    canvas.restore()
    
    result
}

private fun saveBitmap(bmp: Bitmap, path: String) {
    FileOutputStream(File(path)).use { bmp.compress(Bitmap.CompressFormat.JPEG, 95, it) }
}
