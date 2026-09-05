package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.NoteBodyField
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.NoteSaveErrorBanner
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.SaveNoteButton

@Composable
fun NoteEditorContentScreen(
    body: String,
    bodyLabel: String,
    saveLabel: String,
    savingLabel: String,
    saveErrorMessage: String,
    retryLabel: String,
    isSaving: Boolean,
    hasSaveError: Boolean,
    isSaveEnabled: Boolean,
    onBodyChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(NotesAppSpacing.space4),
    ) {
        if (hasSaveError) {
            NoteSaveErrorBanner(
                message = saveErrorMessage,
                retryLabel = retryLabel,
                onRetry = onSave,
            )
        }
        NoteBodyField(
            body = body,
            label = bodyLabel,
            enabled = !isSaving,
            onBodyChanged = onBodyChanged,
            modifier = Modifier.weight(1f),
        )
        SaveNoteButton(
            label = saveLabel,
            savingLabel = savingLabel,
            enabled = isSaveEnabled,
            isSaving = isSaving,
            onSave = onSave,
            modifier = Modifier.padding(bottom = NotesAppSpacing.space4),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteEditorContentScreenPreview() {
    NotesAppTheme {
        NoteEditorContentScreen(
            body = "Текст заметки",
            bodyLabel = "Текст заметки",
            saveLabel = "Сохранить",
            savingLabel = "Сохраняем заметку…",
            saveErrorMessage = "Не удалось сохранить заметку",
            retryLabel = "Повторить",
            isSaving = false,
            hasSaveError = false,
            isSaveEnabled = true,
            onBodyChanged = {},
            onSave = {},
        )
    }
}
