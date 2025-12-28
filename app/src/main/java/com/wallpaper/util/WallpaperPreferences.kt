package com.wallpaper.util

import android.content.Context
import android.content.SharedPreferences

class WallpaperPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "wallpaper_prefs",
        Context.MODE_PRIVATE
    )

    fun getCurrentImageId(folderId: Long): Long {
        return prefs.getLong(KEY_CURRENT_IMAGE_ID + folderId, -1L)
    }

    fun setCurrentImageId(folderId: Long, imageId: Long) {
        prefs.edit().putLong(KEY_CURRENT_IMAGE_ID + folderId, imageId).apply()
    }

    fun getLastFolderId(): Long {
        return prefs.getLong(KEY_LAST_FOLDER_ID, -1L)
    }

    fun setLastFolderId(folderId: Long) {
        prefs.edit().putLong(KEY_LAST_FOLDER_ID, folderId).apply()
    }

    companion object {
        private const val KEY_CURRENT_IMAGE_ID = "current_image_id_"
        private const val KEY_LAST_FOLDER_ID = "last_folder_id"
    }
}
