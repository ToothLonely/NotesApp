package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.field

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun NoteBodyField(
    body: String,
    label: String,
    enabled: Boolean,
    onBodyChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = body,
        onValueChange = onBodyChanged,
        modifier = modifier.fillMaxSize(),
        enabled = enabled,
        label = { Text(text = label) },
        shape = MaterialTheme.shapes.large,
        textStyle = MaterialTheme.typography.bodyLarge,
    )
}

@Preview(showBackground = true)
@Composable
private fun NoteBodyFieldPreview() {
    NotesAppTheme {
        NoteBodyField(
            body = "Текст заметки",
            label = "Текст заметки",
            enabled = true,
            onBodyChanged = {},
        )
    }
}
