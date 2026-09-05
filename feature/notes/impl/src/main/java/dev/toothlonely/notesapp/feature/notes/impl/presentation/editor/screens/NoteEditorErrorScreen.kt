package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun NoteEditorErrorScreen(
    message: String,
    retryLabel: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = NotesAppSpacing.space4,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
        )
        TextButton(onClick = onRetry) {
            Text(text = retryLabel)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteEditorErrorScreenPreview() {
    NotesAppTheme {
        NoteEditorErrorScreen(
            message = "Не удалось подготовить заметку",
            retryLabel = "Повторить",
            onRetry = {},
        )
    }
}
