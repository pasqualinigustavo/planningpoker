package com.planningpoker.feature.sprints.details

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.planningpoker.feature.sprints.R

@Composable
internal fun DeleteSprintDialog(
    sprintName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Warning, contentDescription = null) },
        iconContentColor = MaterialTheme.colorScheme.error,
        title = { Text(stringResource(R.string.feature_sprints_delete_dialog_title)) },
        text = { Text(stringResource(R.string.feature_sprints_delete_dialog_message, sprintName)) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Text(stringResource(R.string.feature_sprints_delete_dialog_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.feature_sprints_dialog_cancel))
            }
        },
    )
}
