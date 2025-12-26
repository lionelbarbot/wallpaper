package com.wallpaper.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var name by remember { mutableStateOf("") }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.NONE) }
    var targetScreen by remember { mutableStateOf(TargetScreen.BOTH) }
    
    LaunchedEffect(folderId) {
        if (folderId != null) {
            val folder = viewModel.getFolder()
            folder?.let {
                name = it.name
                recurrenceType = it.recurrenceType
                targetScreen = it.targetScreen
            }
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.folder_name)) },
                placeholder = { Text(stringResource(R.string.folder_name_hint)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            
            Text(
                text = stringResource(R.string.recurrence),
                style = MaterialTheme.typography.titleMedium
            )
            
            RecurrenceType.values().forEach { type ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = recurrenceType == type,
                        onClick = { recurrenceType = type }
                    )
                    Text(
                        text = when (type) {
                            RecurrenceType.NONE -> stringResource(R.string.recurrence_none)
                            RecurrenceType.DAILY -> stringResource(R.string.recurrence_daily)
                            RecurrenceType.WEEKLY -> stringResource(R.string.recurrence_weekly)
                            RecurrenceType.MONTHLY -> stringResource(R.string.recurrence_monthly)
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Text(
                text = stringResource(R.string.target_screens),
                style = MaterialTheme.typography.titleMedium
            )
            
            TargetScreen.values().forEach { screen ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = targetScreen == screen,
                        onClick = { targetScreen = screen }
                    )
                    Text(
                        text = when (screen) {
                            TargetScreen.LOCK_SCREEN -> stringResource(R.string.lock_screen)
                            TargetScreen.HOME_SCREEN -> stringResource(R.string.home_screen)
                            TargetScreen.BOTH -> stringResource(R.string.both_screens)
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val rule = RecurrenceRule(
                        type = recurrenceType,
                        daysOfWeek = emptyList(),
                        startHour = null,
                        endHour = null,
                        dayOfMonth = null
                    )
                    scope.launch {
                        viewModel.saveFolder(name, recurrenceType, rule, targetScreen)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}

