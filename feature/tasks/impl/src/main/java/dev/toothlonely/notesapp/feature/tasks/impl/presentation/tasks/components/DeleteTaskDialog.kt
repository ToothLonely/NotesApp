package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun DeleteTaskDialog(
    title: String,
    message: String,
    cancelLabel: String,
    deleteLabel: String,
    deletingDescription: String,
    isDeleting: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isDeleting) onDismiss() },
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !isDeleting,
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(NotesAppSizes.standardIcon)
                            .semantics { stateDescription = deletingDescription },
                    )
                } else {
                    Text(
                        text = deleteLabel,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting,
            ) {
                Text(text = cancelLabel)
            }
        },
        shape = MaterialTheme.shapes.extraLarge,
    )
}

@Preview(showBackground = true)
@Composable
private fun DeleteTaskDialogPreview() {
    NotesAppTheme {
        DeleteTaskDialog(
            title = "Удалить задачу?",
            message = "Задача «Купить молоко» будет удалена.",
            cancelLabel = "Отмена",
            deleteLabel = "Удалить",
            deletingDescription = "Удаляем задачу",
            isDeleting = false,
            onConfirm = {},
            onDismiss = {},
        )
    }
}
