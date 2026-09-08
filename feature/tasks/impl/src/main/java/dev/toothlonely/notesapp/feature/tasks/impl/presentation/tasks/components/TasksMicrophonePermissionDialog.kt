package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun TasksMicrophonePermissionDialog(
    title: String,
    message: String,
    actionLabel: String,
    dismissLabel: String,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = dismissLabel)
            }
        },
        title = { Text(text = title) },
        text = { Text(text = message) },
    )
}

@Preview(showBackground = true)
@Composable
private fun TasksMicrophonePermissionDialogPreview() {
    NotesAppTheme {
        TasksMicrophonePermissionDialog(
            title = "Доступ к микрофону",
            message = "Для голосового создания задачи нужен доступ к микрофону.",
            actionLabel = "Разрешить",
            dismissLabel = "Не сейчас",
            onAction = {},
            onDismiss = {},
        )
    }
}
