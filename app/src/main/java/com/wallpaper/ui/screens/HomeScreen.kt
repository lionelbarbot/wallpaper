package com.wallpaper.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wallpaper.R
import com.wallpaper.data.database.entities.WallpaperFolder
import com.wallpaper.data.model.TargetScreen
import com.wallpaper.ui.navigation.Screen
import com.wallpaper.ui.viewmodel.HomeViewModel
import com.wallpaper.ui.viewmodel.HomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToFolder: (Long) -> Unit,
    onNavigateToEditFolder: (Long?) -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModelFactory(LocalContext.current))
) {
    val folders by viewModel.folders.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_screen_title)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToEditFolder(null) }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_folder))
            }
        }
    ) { padding ->
        if (folders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.no_folders))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(folders) { folder ->
                    FolderCard(
                        folder = folder,
                        onClick = { onNavigateToFolder(folder.id) },
                        onEdit = { onNavigateToEditFolder(folder.id) },
                        onToggleActive = { viewModel.toggleFolderActive(folder) },
                        onDelete = { viewModel.deleteFolder(folder) }
                    )
                }
            }
        }
    }
}

@Composable
fun FolderCard(
    folder: WallpaperFolder,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = folder.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getTargetScreenText(folder.targetScreen),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // Afficher les jours sélectionnés et le temps de rotation sur la même ligne
                    val recurrenceRule = com.wallpaper.data.model.RecurrenceRule.fromJson(folder.recurrenceRule)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (recurrenceRule != null && recurrenceRule.daysOfWeek.isNotEmpty()) {
                            Text(
                                text = formatSelectedDays(recurrenceRule.daysOfWeek),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        // Afficher le temps de rotation si défini
                        if (folder.rotationIntervalMinutes != null) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = "Rotation",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${folder.rotationIntervalMinutes} min",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    // Afficher les options activées (ordre aléatoire et changement au déverrouillage)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Icône pour l'ordre aléatoire
                        if (folder.randomOrder) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Shuffle,
                                    contentDescription = stringResource(R.string.random_order),
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = stringResource(R.string.random_order),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                        // Icône pour le changement au déverrouillage
                        if (folder.changeOnUnlock) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.LockOpen,
                                    contentDescription = stringResource(R.string.change_on_unlock),
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = stringResource(R.string.change_on_unlock),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Text(
                        text = if (folder.isActive) stringResource(R.string.active) else stringResource(R.string.inactive),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (folder.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Toggle d'activation (plus gros)
                Switch(
                    checked = folder.isActive,
                    onCheckedChange = { onToggleActive() },
                    modifier = Modifier.size(56.dp, 40.dp)
                )
            }
            
            // Boutons d'édition et suppression en dessous
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bouton d'édition
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        modifier = Modifier.size(24.dp)
                    )
                }
                // Bouton de suppression (plus petit)
                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
    
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text("Supprimer le répertoire ${folder.name} ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun getTargetScreenText(targetScreen: TargetScreen): String {
    return when (targetScreen) {
        TargetScreen.LOCK_SCREEN -> stringResource(R.string.lock_screen)
        TargetScreen.HOME_SCREEN -> stringResource(R.string.home_screen)
        TargetScreen.BOTH -> stringResource(R.string.both_screens)
    }
}

@Composable
fun formatSelectedDays(daysOfWeek: List<Int>): String {
    // Jours de la semaine : 1=Lundi, 2=Mardi, 3=Mercredi, 4=Jeudi, 5=Vendredi, 6=Samedi, 7=Dimanche
    val dayLetters = listOf("L", "M", "M", "J", "V", "S", "D")
    val allDays = listOf(1, 2, 3, 4, 5, 6, 7)
    
    return allDays.joinToString(separator = " ") { day ->
        if (daysOfWeek.contains(day)) {
            dayLetters[day - 1]
        } else {
            "·"
        }
    }
}

