package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorUiState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.NoteEditorTopBar

@Composable
fun NoteEditorScreen(
    state: NoteEditorUiState,
    onTitleChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onSave: () -> Unit,
    onRetryPreparation: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = state as? NoteEditorUiState.Content
    val titlePlaceholder = if (content == null) {
        ""
    } else {
        stringResource(R.string.note_title_placeholder)
    }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NoteEditorTopBar(
                title = content?.title.orEmpty(),
                placeholder = titlePlaceholder,
                titleLabel = stringResource(R.string.note_title_label),
                backLabel = stringResource(R.string.note_editor_back),
                enabled = content?.isSaving == false,
                onTitleChanged = onTitleChanged,
                onBack = onBack,
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (state) {
                NoteEditorUiState.Loading -> NoteEditorLoadingScreen(
                    label = stringResource(R.string.note_editor_loading),
                    modifier = Modifier.fillMaxSize(),
                )

                NoteEditorUiState.Error -> NoteEditorErrorScreen(
                    message = stringResource(R.string.note_editor_error),
                    retryLabel = stringResource(R.string.retry),
                    onRetry = onRetryPreparation,
                    modifier = Modifier.fillMaxSize(),
                )

                is NoteEditorUiState.Content -> NoteEditorContentScreen(
                    body = state.body,
                    bodyLabel = stringResource(R.string.note_body_label),
                    saveLabel = stringResource(R.string.note_save),
                    savingLabel = stringResource(R.string.note_saving),
                    saveErrorMessage = stringResource(R.string.note_save_error),
                    retryLabel = stringResource(R.string.retry),
                    isSaving = state.isSaving,
                    hasSaveError = state.hasSaveError,
                    isSaveEnabled = state.isSaveEnabled,
                    onBodyChanged = onBodyChanged,
                    onSave = onSave,
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = NotesAppSizes.maximumContentWidth)
                        .padding(horizontal = NotesAppSpacing.space4)
                        .navigationBarsPadding()
                        .imePadding(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NoteEditorScreenPreview() {
    NotesAppTheme {
        NoteEditorScreen(
            state = NoteEditorUiState.Content(
                title = "",
                body = "",
                generatedTitleNumber = 1,
            ),
            onTitleChanged = {},
            onBodyChanged = {},
            onSave = {},
            onRetryPreparation = {},
            onBack = {},
        )
    }
}
