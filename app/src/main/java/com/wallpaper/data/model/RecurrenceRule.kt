package com.wallpaper.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

@Serializable
data class RecurrenceRule(
    val type: RecurrenceType,
    val daysOfWeek: List<Int> = emptyList(), // 1-7 (Monday-Sunday)
    val startHour: Int? = null, // 0-23
    val endHour: Int? = null, // 0-23
    val dayOfMonth: Int? = null // 1-31 for monthly
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }
    
    companion object {
        fun fromJson(json: String): RecurrenceRule? {
            return try {
                Json.decodeFromString(json)
            } catch (e: Exception) {
                null
            }
        }
    }
}

