package com.wallpaper.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wallpaper_images",
    foreignKeys = [
        ForeignKey(
            entity = WallpaperFolder::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class WallpaperImage(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val folderId: Long,
    val filePath: String,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
