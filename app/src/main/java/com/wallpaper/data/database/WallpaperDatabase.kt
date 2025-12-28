package com.wallpaper.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wallpaper.data.database.dao.WallpaperDao
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.database.entities.WallpaperImage
import com.wallpaper.data.database.converters.RecurrenceTypeConverter
import com.wallpaper.data.database.converters.TargetScreenConverter

@Database(
    entities = [WallpaperFolder::class, WallpaperImage::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(RecurrenceTypeConverter::class, TargetScreenConverter::class)
abstract class WallpaperDatabase : RoomDatabase() {
    abstract fun wallpaperDao(): WallpaperDao
    
    companion object {
        @Volatile
        private var INSTANCE: WallpaperDatabase? = null
        
        fun getDatabase(context: Context): WallpaperDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WallpaperDatabase::class.java,
                    "wallpaper_database"
                )
                .fallbackToDestructiveMigration() // Pour le développement - supprime en production
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

