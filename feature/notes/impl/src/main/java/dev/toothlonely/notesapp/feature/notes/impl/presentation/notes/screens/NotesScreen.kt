package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.NotesContentState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.NotesUiState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.DeleteModeBanner
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.NotesDeleteErrorBanner
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.NotesTopBar

@Composable
fun NotesScreen(
    state: NotesUiState,
    onCreateNote: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onToggleDeleteMode: () -> Unit,
    onDeleteNote: (Long) -> Unit,
    onRetryDelete: () -> Unit,
    onDismissDeleteError: () -> Unit,
    onRetryLoading: () -> Unit,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deleteDescriptionFormat = stringResource(R.string.notes_delete_note)
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            NotesTopBar(
                title = stringResource(
                    if (state.isDeleteMode) {
                        R.string.notes_delete_mode_title
                    } else {
                        R.string.notes_title
                    },
                ),
                isDeleteMode = state.isDeleteMode,
                deleteActionEnabled = state.isDeleteMode ||
                    state.content is NotesContentState.Content,
                enterDeleteModeLabel = stringResource(R.string.notes_enter_delete_mode),
                exitDeleteModeLabel = stringResource(R.string.notes_exit_delete_mode),
                onToggleDeleteMode = onToggleDeleteMode,
            )
        },
        snackbarHost = snackbarHost,
        floatingActionButton = {
            if (!state.isDeleteMode) {
                FloatingActionButton(
                    onClick = onCreateNote,
                    shape = NotesAppShapes.full,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_add_24),
                        contentDescription = stringResource(R.string.notes_create),
                    )
                }
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = NotesAppSizes.maximumContentWidth)
                    .fillMaxSize()
                    .padding(horizontal = NotesAppSpacing.space4),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (state.isDeleteMode) {
                    DeleteModeBanner(
                        message = stringResource(R.string.notes_delete_mode_banner),
                        modifier = Modifier.padding(top = NotesAppSpacing.space3),
                    )
                }
                if (state.isDeleteMode && state.failedDeleteNoteId != null) {
                    NotesDeleteErrorBanner(
                        message = stringResource(R.string.notes_delete_error),
                        retryLabel = stringResource(R.string.retry),
                        dismissLabel = stringResource(R.string.notes_dismiss_delete_error),
                        onRetry = onRetryDelete,
                        onDismiss = onDismissDeleteError,
                        modifier = Modifier.padding(top = NotesAppSpacing.space3),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    val screenModifier = Modifier.fillMaxSize()
                    when (val content = state.content) {

                        NotesContentState.Loading -> NotesLoadingScreen(
                            label = stringResource(R.string.notes_loading),
                            modifier = screenModifier,
                        )

                        NotesContentState.Empty -> NotesEmptyScreen(
                            title = stringResource(R.string.notes_empty_title),
                            modifier = screenModifier,
                        )

                        is NotesContentState.Content -> NotesListScreen(
                            notes = content.notes,
                            createdDateFormat = stringResource(R.string.note_created_date),
                            deleteDescription = { title -> deleteDescriptionFormat.format(title) },
                            isDeleteMode = state.isDeleteMode,
                            deletingNoteIds = state.deletingNoteIds,
                            onOpenNote = onOpenNote,
                            onDeleteNote = onDeleteNote,
                            loadImage = loadImage,
                            modifier = screenModifier,
                        )

                        NotesContentState.Error -> NotesErrorScreen(
                            title = stringResource(R.string.notes_error_title),
                            retryLabel = stringResource(R.string.retry),
                            onRetry = onRetryLoading,
                            modifier = screenModifier,
                        )

                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenPreview() {
    NotesAppTheme {
        NotesScreen(
            state = NotesUiState(content = NotesContentState.Empty),
            onCreateNote = {},
            onOpenNote = {},
            onToggleDeleteMode = {},
            onDeleteNote = {},
            onRetryDelete = {},
            onDismissDeleteError = {},
            onRetryLoading = {},
            loadImage = { _, _, _, _ -> null },
            snackbarHost = {},
        )
    }
}
