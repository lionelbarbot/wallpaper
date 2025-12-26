package com.wallpaper.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wallpaper.data.model.RecurrenceType
import com.wallpaper.data.model.TargetScreen

@Entity(tableName = "wallpaper_folders")
data class WallpaperFolder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val recurrenceType: RecurrenceType,
    val recurrenceRule: String, // JSON string pour les détails de récurrence (jours de la semaine, heures, etc.)
    val targetScreen: TargetScreen,
    val isActive: Boolean = false,
    val rotationIntervalMinutes: Int? = null, // Intervalle de rotation en minutes (null = pas de rotation automatique)
    val changeOnUnlock: Boolean = false, // Changer l'image au déverrouillage
    val createdAt: Long = System.currentTimeMillis()
)

