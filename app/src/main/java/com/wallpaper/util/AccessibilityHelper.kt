package com.wallpaper.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.wallpaper.service.GestureService

object AccessibilityHelper {
    
    /**
     * Vérifie si le service d'accessibilité GestureService est activé
     */
    fun isAccessibilityServiceEnabled(context: Context): Boolean {
        val accessibilityManager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
            AccessibilityServiceInfo.FEEDBACK_ALL_MASK
        )
        
        val serviceName = GestureService::class.java.name
        val packageName = context.packageName
        
        return enabledServices.any { serviceInfo ->
            serviceInfo.resolveInfo.serviceInfo.packageName == packageName &&
            serviceInfo.resolveInfo.serviceInfo.name == serviceName
        }
    }
    
    /**
     * Ouvre les paramètres d'accessibilité pour activer le service
     */
    fun openAccessibilitySettings(context: Context) {
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }
    
    /**
     * Ouvre directement les paramètres d'accessibilité avec un filtre pour cette application
     * (Android 11+)
     */
    fun openAccessibilitySettingsForApp(context: Context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.putExtra(":settings:fragment_args_key", context.packageName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } else {
            openAccessibilitySettings(context)
        }
    }
}

