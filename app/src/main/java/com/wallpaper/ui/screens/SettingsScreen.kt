package com.wallpaper.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wallpaper.R
import com.wallpaper.util.AccessibilityHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    var rotationInterval by remember { mutableStateOf("60") }
    val context = LocalContext.current
    
    // Vérifier l'état du service d'accessibilité
    var isAccessibilityEnabled by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isAccessibilityEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(context)
    }
    
    // Re-vérifier quand l'écran revient au premier plan
    DisposableEffect(Unit) {
        val checkAccessibility = {
            isAccessibilityEnabled = AccessibilityHelper.isAccessibilityServiceEnabled(context)
        }
        
        // Vérifier périodiquement (toutes les secondes quand l'écran est visible)
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                checkAccessibility()
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
        
        onDispose {
            handler.removeCallbacks(runnable)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section Service d'Accessibilité
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAccessibilityEnabled) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.accessibility_service_title),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isAccessibilityEnabled) 
                                    stringResource(R.string.accessibility_service_enabled)
                                else 
                                    stringResource(R.string.accessibility_service_disabled),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isAccessibilityEnabled) 
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else 
                                    MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Icon(
                            imageVector = if (isAccessibilityEnabled) 
                                Icons.Default.CheckCircle 
                            else 
                                Icons.Default.Settings,
                            contentDescription = null,
                            tint = if (isAccessibilityEnabled) 
                                MaterialTheme.colorScheme.primary
                            else 
                                MaterialTheme.colorScheme.error
                        )
                    }
                    
                    Text(
                        text = stringResource(R.string.accessibility_service_explanation),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isAccessibilityEnabled) 
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else 
                            MaterialTheme.colorScheme.onErrorContainer
                    )
                    
                    if (!isAccessibilityEnabled) {
                        Button(
                            onClick = {
                                AccessibilityHelper.openAccessibilitySettings(context)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.open_accessibility_settings))
                        }
                    }
                }
            }
            
            Divider()
            
            // Section Rotation Automatique
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.rotation_interval),
                    style = MaterialTheme.typography.titleMedium
                )
                
                OutlinedTextField(
                    value = rotationInterval,
                    onValueChange = { rotationInterval = it },
                    label = { Text(stringResource(R.string.rotation_interval)) },
                    placeholder = { Text(stringResource(R.string.rotation_interval_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Text(
                    text = "L'intervalle de rotation sera sauvegardé et utilisé par le Worker pour changer automatiquement les fonds d'écran.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

