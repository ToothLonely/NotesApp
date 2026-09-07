package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.NoteAttachmentErrorBanner

@Composable
fun NoteEditorReadingScreen(
    body: String,
    attachmentErrorMessage: String?,
    attachmentErrorDismissLabel: String,
    onDismissAttachmentError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = NotesAppSpacing.space4,
                top = NotesAppSpacing.space4,
                end = NotesAppSpacing.space4,
                bottom = NotesAppSizes.fab + NotesAppSpacing.space8,
            ),
        verticalArrangement = Arrangement.spacedBy(NotesAppSpacing.space4),
    ) {
        if (attachmentErrorMessage != null) {
            NoteAttachmentErrorBanner(
                message = attachmentErrorMessage,
                dismissLabel = attachmentErrorDismissLabel,
                actionLabel = null,
                onAction = {},
                onDismiss = onDismissAttachmentError,
            )
        }
        SelectionContainer {
            Text(
                text = body,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteEditorReadingScreenPreview() {
    NotesAppTheme {
        NoteEditorReadingScreen(
            body = "Посмотреть старый город утром, затем пройти вдоль набережной.",
            attachmentErrorMessage = null,
            attachmentErrorDismissLabel = "Закрыть сообщение",
            onDismissAttachmentError = {},
        )
    }
}
