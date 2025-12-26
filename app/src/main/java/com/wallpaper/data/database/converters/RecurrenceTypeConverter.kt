package com.wallpaper.data.database.converters

import androidx.room.TypeConverter
import com.wallpaper.data.model.RecurrenceType

class RecurrenceTypeConverter {
    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType): String {
        return value.name
    }
    
    @TypeConverter
    fun toRecurrenceType(value: String): RecurrenceType {
        return RecurrenceType.valueOf(value)
    }
}

