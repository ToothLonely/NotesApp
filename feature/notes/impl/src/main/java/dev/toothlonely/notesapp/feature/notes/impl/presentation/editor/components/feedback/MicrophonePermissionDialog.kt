package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.feedback

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun MicrophonePermissionDialog(
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
private fun MicrophonePermissionDialogPreview() {
    NotesAppTheme {
        MicrophonePermissionDialog(
            title = "Доступ к микрофону",
            message = "Для голосового ввода нужен доступ к микрофону.",
            actionLabel = "Разрешить",
            dismissLabel = "Не сейчас",
            onAction = {},
            onDismiss = {},
        )
    }
}
