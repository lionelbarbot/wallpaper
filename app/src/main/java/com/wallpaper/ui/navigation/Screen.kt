package com.wallpaper.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    data class FolderDetail(val folderId: Long) : Screen("folder_detail/{folderId}") {
        fun createRoute(folderId: Long) = "folder_detail/$folderId"
    }
    data class EditFolder(val folderId: Long?) : Screen("edit_folder/{folderId}") {
        fun createRoute(folderId: Long?) = if (folderId != null) "edit_folder/$folderId" else "edit_folder/new"
    }
    data class EditImage(val imageId: Long) : Screen("edit_image/{imageId}") {
        fun createRoute(imageId: Long) = "edit_image/$imageId"
    }
    
    data class AddImage(val folderId: Long) : Screen("add_image/{folderId}") {
        fun createRoute(folderId: Long) = "add_image/$folderId"
    }
    object Settings : Screen("settings")
}

