package com.wallpaper.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

object ImagePicker {
    
    @Composable
    fun rememberImagePicker(
        onImageSelected: (String) -> Unit
    ): () -> Unit {
        val context = LocalContext.current
        
        val pickImageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result.data?.data?.let { uri ->
                val filePath = saveImageToAppDirectory(context, uri)
                filePath?.let { onImageSelected(it) }
            }
        }
        
        return {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }
    }
    
    private fun saveImageToAppDirectory(context: Context, uri: Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val appDir = File(context.getExternalFilesDir(null), "wallpapers")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            
            val fileName = "image_${System.currentTimeMillis()}.jpg"
            val file = File(appDir, fileName)
            
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}

