package com.wallpaper.ui.screens

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wallpaper.R

@Composable
fun AccessibilityDialog(
    onDismiss: () -> Unit,
    onEnable: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.accessibility_required_title))
        },
        text = {
            Text(stringResource(R.string.accessibility_required_message))
        },
        confirmButton = {
            TextButton(onClick = onEnable) {
                Text(stringResource(R.string.enable_now))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.later))
            }
        }
    )
}

