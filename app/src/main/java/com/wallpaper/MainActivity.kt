package com.wallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.wallpaper.ui.navigation.NavGraph
import com.wallpaper.ui.screens.AccessibilityDialog
import com.wallpaper.ui.theme.WallpaperTheme
import com.wallpaper.util.AccessibilityHelper
import com.wallpaper.util.PermissionHelper

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        // Permission granted or denied
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Demander la permission de stockage si nécessaire
        if (!PermissionHelper.hasStoragePermission(this)) {
            requestPermissionLauncher.launch(PermissionHelper.getStoragePermission())
        }
        
        setContent {
            WallpaperTheme {
                MainContent()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Re-vérifier le service d'accessibilité quand l'activité reprend
        // Cela permet de détecter si l'utilisateur a activé le service depuis les paramètres
    }
}

@Composable
private fun MainContent() {
    val context = LocalContext.current
    var showAccessibilityDialog by remember { mutableStateOf(false) }
    
    // Vérifier le service d'accessibilité au démarrage
    LaunchedEffect(Unit) {
        if (!AccessibilityHelper.isAccessibilityServiceEnabled(context)) {
            // Attendre un peu avant d'afficher le dialogue
            kotlinx.coroutines.delay(1000)
            showAccessibilityDialog = true
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavGraph()
        
        // Afficher le dialogue si nécessaire
        if (showAccessibilityDialog) {
            AccessibilityDialog(
                onDismiss = { showAccessibilityDialog = false },
                onEnable = {
                    AccessibilityHelper.openAccessibilitySettings(context)
                    showAccessibilityDialog = false
                }
            )
        }
    }
}
