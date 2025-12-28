package com.wallpaper.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wallpaper.R
import com.wallpaper.data.model.RecurrenceRule
import com.wallpaper.data.model.RecurrenceType
import com.wallpaper.data.model.TargetScreen
import com.wallpaper.ui.viewmodel.EditFolderViewModel
import com.wallpaper.ui.viewmodel.EditFolderViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFolderScreen(
    folderId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: EditFolderViewModel = viewModel(
        factory = EditFolderViewModelFactory(LocalContext.current, folderId)
    )
) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    var name by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<Int>()) } // 1-7 (Monday-Sunday)
    var targetScreen by remember { mutableStateOf(TargetScreen.BOTH) }
    var rotationInterval by remember { mutableStateOf("") }
    var changeOnUnlock by remember { mutableStateOf(false) }
    var randomOrder by remember { mutableStateOf(false) }
    
    // Job pour la sauvegarde automatique avec debounce
    var autoSaveJob by remember { mutableStateOf<Job?>(null) }
    
    // Fonction pour sauvegarder automatiquement
    fun autoSave() {
        if (selectedDays.isEmpty()) return
        
        autoSaveJob?.cancel()
        autoSaveJob = scope.launch {
            delay(500) // Debounce de 500ms
            val rule = RecurrenceRule(
                type = RecurrenceType.WEEKLY,
                daysOfWeek = selectedDays.sorted().toList(),
                startHour = null,
                endHour = null,
                dayOfMonth = null
            )
            
            val rotationMinutes = rotationInterval.trim().takeIf { it.isNotEmpty() }?.toIntOrNull()
            
            viewModel.saveFolder(
                name = name,
                recurrenceType = RecurrenceType.WEEKLY,
                recurrenceRule = rule,
                targetScreen = targetScreen,
                rotationIntervalMinutes = rotationMinutes,
                changeOnUnlock = changeOnUnlock,
                randomOrder = randomOrder
            )
        }
    }
    
    LaunchedEffect(folderId) {
        if (folderId != null) {
            val folder = viewModel.getFolder()
            folder?.let {
                name = it.name
                targetScreen = it.targetScreen
                rotationInterval = it.rotationIntervalMinutes?.toString() ?: ""
                changeOnUnlock = it.changeOnUnlock
                randomOrder = it.randomOrder
                // Charger les jours sélectionnés depuis la règle de récurrence
                val rule = RecurrenceRule.fromJson(it.recurrenceRule)
                selectedDays = rule?.daysOfWeek?.toSet() ?: emptySet()
            }
        } else {
            // Par défaut, sélectionner tous les jours pour un nouveau répertoire
            selectedDays = setOf(1, 2, 3, 4, 5, 6, 7)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        if (folderId == null) stringResource(R.string.create_folder_title)
                        else stringResource(R.string.edit_folder_title)
                    )
                },
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
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { 
                    name = it
                    autoSave()
                },
                label = { Text(stringResource(R.string.folder_name)) },
                placeholder = { Text(stringResource(R.string.folder_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Text(
                text = stringResource(R.string.recurrence),
                style = MaterialTheme.typography.titleMedium
            )
            
            // Sélecteur de jours de la semaine
            Text(
                text = stringResource(R.string.select_days),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            val daysOfWeek = listOf(
                1 to stringResource(R.string.monday),
                2 to stringResource(R.string.tuesday),
                3 to stringResource(R.string.wednesday),
                4 to stringResource(R.string.thursday),
                5 to stringResource(R.string.friday),
                6 to stringResource(R.string.saturday),
                7 to stringResource(R.string.sunday)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                daysOfWeek.forEach { (dayNumber, dayName) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        FilterChip(
                            selected = selectedDays.contains(dayNumber),
                            onClick = {
                                selectedDays = if (selectedDays.contains(dayNumber)) {
                                    selectedDays - dayNumber
                                } else {
                                    selectedDays + dayNumber
                                }
                                autoSave()
                            },
                            label = { 
                                Text(
                                    text = dayName.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            Text(
                text = stringResource(R.string.target_screens),
                style = MaterialTheme.typography.titleMedium
            )
            
            TargetScreen.values().forEach { screen ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = targetScreen == screen,
                        onClick = { 
                            targetScreen = screen
                            autoSave()
                        },
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = when (screen) {
                            TargetScreen.LOCK_SCREEN -> stringResource(R.string.lock_screen)
                            TargetScreen.HOME_SCREEN -> stringResource(R.string.home_screen)
                            TargetScreen.BOTH -> stringResource(R.string.both_screens)
                        },
                        modifier = Modifier.padding(start = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            // Intervalle de rotation
            Text(
                text = stringResource(R.string.rotation_interval),
                style = MaterialTheme.typography.titleMedium
            )
            
            OutlinedTextField(
                value = rotationInterval,
                onValueChange = { 
                    rotationInterval = it
                    autoSave()
                },
                label = { Text(stringResource(R.string.rotation_interval)) },
                placeholder = { Text(stringResource(R.string.rotation_interval_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            // Option changement au déverrouillage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.change_on_unlock),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.change_on_unlock_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = changeOnUnlock,
                    onCheckedChange = { 
                        changeOnUnlock = it
                        autoSave()
                    }
                )
            }
            
            // Option ordre aléatoire
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.random_order),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.random_order_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = randomOrder,
                    onCheckedChange = { 
                        randomOrder = it
                        autoSave()
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

