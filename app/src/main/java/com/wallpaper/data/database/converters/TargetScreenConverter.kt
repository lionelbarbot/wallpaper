package com.wallpaper.data.database.converters

import androidx.room.TypeConverter
import com.wallpaper.data.model.TargetScreen

class TargetScreenConverter {
    @TypeConverter
    fun fromTargetScreen(value: TargetScreen): String {
        return value.name
    }
    
    @TypeConverter
    fun toTargetScreen(value: String): TargetScreen {
        return TargetScreen.valueOf(value)
    }
}

