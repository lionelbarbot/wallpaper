package com.wallpaper.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import java.io.File
import java.io.FileOutputStream

object TestImageGenerator {
    
    /**
     * Génère une image de test avec une flèche orientée dans une direction spécifique
     * @param context Le contexte Android
     * @param direction Direction de la flèche : 0=haut, 90=droite, 180=bas, 270=gauche
     * @param fileName Nom du fichier à créer
     * @return Le chemin du fichier créé, ou null en cas d'erreur
     */
    fun generateArrowImage(
        context: Context,
        direction: Int,
        fileName: String
    ): String? {
        return try {
            val appDir = File(context.getExternalFilesDir(null), "wallpapers")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            
            val file = File(appDir, fileName)
            
            // Créer un bitmap de 1920x1080 (format wallpaper standard)
            val width = 1920
            val height = 1080
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            
            // Fond dégradé pour mieux voir la flèche
            val paint = Paint().apply {
                isAntiAlias = true
            }
            
            // Fond avec dégradé et couleur spécifique selon la direction
            val (baseR, baseG, baseB) = when (direction) {
                0 -> Triple(100, 150, 200) // Bleu pour HAUT
                90 -> Triple(150, 200, 100) // Vert pour DROITE
                180 -> Triple(200, 150, 100) // Orange pour BAS
                270 -> Triple(200, 100, 150) // Rose pour GAUCHE
                else -> Triple(150, 150, 150) // Gris par défaut
            }
            
            for (y in 0 until height) {
                val ratio = y.toFloat() / height
                val color = Color.rgb(
                    (baseR * (0.3f + ratio * 0.4f)).toInt().coerceIn(0, 255),
                    (baseG * (0.3f + ratio * 0.4f)).toInt().coerceIn(0, 255),
                    (baseB * (0.3f + ratio * 0.4f)).toInt().coerceIn(0, 255)
                )
                paint.color = color
                canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
            }
            
            // Dessiner la flèche au centre
            val centerX = width / 2f
            val centerY = height / 2f
            val arrowSize = 300f
            
            // Créer le path de la flèche pointant vers le haut
            val arrowPath = Path().apply {
                // Corps de la flèche (rectangle)
                moveTo(centerX - arrowSize / 4, centerY + arrowSize / 2)
                lineTo(centerX - arrowSize / 4, centerY - arrowSize / 3)
                lineTo(centerX + arrowSize / 4, centerY - arrowSize / 3)
                lineTo(centerX + arrowSize / 4, centerY + arrowSize / 2)
                close()
                
                // Pointe de la flèche (triangle)
                moveTo(centerX, centerY - arrowSize / 2)
                lineTo(centerX - arrowSize / 2, centerY - arrowSize / 3)
                lineTo(centerX + arrowSize / 2, centerY - arrowSize / 3)
                close()
            }
            
            // Appliquer la rotation
            canvas.save()
            canvas.translate(centerX, centerY)
            canvas.rotate(direction.toFloat())
            canvas.translate(-centerX, -centerY)
            
            // Dessiner la flèche en blanc avec bordure noire
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            canvas.drawPath(arrowPath, paint)
            
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 8f
            paint.color = Color.BLACK
            canvas.drawPath(arrowPath, paint)
            
            canvas.restore()
            
            // Ajouter un texte indiquant la direction
            paint.style = Paint.Style.FILL
            paint.color = Color.WHITE
            paint.textSize = 80f
            paint.textAlign = Paint.Align.CENTER
            val directionText = when (direction) {
                0 -> "HAUT"
                90 -> "DROITE"
                180 -> "BAS"
                270 -> "GAUCHE"
                else -> "$direction°"
            }
            canvas.drawText(
                directionText,
                centerX,
                centerY + arrowSize / 2 + 100,
                paint
            )
            
            // Sauvegarder le bitmap
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            
            bitmap.recycle()
            
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Génère plusieurs images de test avec des flèches dans différentes directions
     */
    fun generateTestImages(context: Context): List<String> {
        val images = mutableListOf<String>()
        
        // Générer 4 images avec des flèches pointant dans différentes directions
        val directions = listOf(0, 90, 180, 270)
        directions.forEachIndexed { index, direction ->
            val fileName = "test_arrow_${direction}.jpg"
            generateArrowImage(context, direction, fileName)?.let {
                images.add(it)
            }
        }
        
        return images
    }
}

