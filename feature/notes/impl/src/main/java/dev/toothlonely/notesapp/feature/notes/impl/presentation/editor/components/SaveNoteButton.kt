package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme

@Composable
fun SaveNoteButton(
    label: String,
    savingLabel: String,
    enabled: Boolean,
    isSaving: Boolean,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = onSave,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = NotesAppSizes.buttonMinimumHeight),
        enabled = enabled,
        shape = NotesAppShapes.full,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(NotesAppSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(NotesAppSizes.standardIcon),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
            Text(text = if (isSaving) savingLabel else label)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SaveNoteButtonPreview() {
    NotesAppTheme {
        SaveNoteButton(
            label = "Сохранить",
            savingLabel = "Сохраняем заметку…",
            enabled = false,
            isSaving = false,
            onSave = {},
        )
    }
}
