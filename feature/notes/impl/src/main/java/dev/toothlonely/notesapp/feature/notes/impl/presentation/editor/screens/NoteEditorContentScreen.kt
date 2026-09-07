package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.NoteAttachmentErrorBanner
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
    saveRetryLabel: String,
    attachmentErrorMessage: String?,
    attachmentErrorDismissLabel: String,
    imageProcessingLabel: String,
    isSaving: Boolean,
    isClosing: Boolean,
    hasSaveError: Boolean,
    isProcessingImage: Boolean,
    isSaveEnabled: Boolean,
    onBodyChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismissAttachmentError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(NotesAppSpacing.space4),
    ) {
        if (hasSaveError) {
            NoteSaveErrorBanner(
                message = saveErrorMessage,
                retryLabel = saveRetryLabel,
                onRetry = onSave,
            )
        }
        if (attachmentErrorMessage != null) {
            NoteAttachmentErrorBanner(
                message = attachmentErrorMessage,
                dismissLabel = attachmentErrorDismissLabel,
                actionLabel = null,
                onAction = {},
                onDismiss = onDismissAttachmentError,
            )
        }
        if (isProcessingImage) {
            Text(
                text = imageProcessingLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        NoteBodyField(
            body = body,
            label = bodyLabel,
            enabled = !isSaving && !isClosing,
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
            saveRetryLabel = "Повторить",
            attachmentErrorMessage = null,
            attachmentErrorDismissLabel = "Закрыть сообщение",
            imageProcessingLabel = "Обрабатываем изображение…",
            isSaving = false,
            isClosing = false,
            hasSaveError = false,
            isProcessingImage = false,
            isSaveEnabled = true,
            onBodyChanged = {},
            onSave = {},
            onDismissAttachmentError = {},
        )
    }
}
