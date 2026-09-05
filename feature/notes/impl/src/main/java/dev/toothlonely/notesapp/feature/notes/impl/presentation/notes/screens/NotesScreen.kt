package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.icon.NotesAppIcons
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.NotesUiState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.NotesTopBar

@Composable
fun NotesScreen(
    state: NotesUiState,
    onCreateNote: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { NotesTopBar(title = stringResource(R.string.notes_title)) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNote,
                shape = NotesAppShapes.full,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ) {
                Icon(
                    imageVector = NotesAppIcons.Add,
                    contentDescription = stringResource(R.string.notes_create),
                )
            }
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            val contentModifier = Modifier
                .fillMaxSize()
                .widthIn(max = NotesAppSizes.maximumContentWidth)
                .padding(horizontal = NotesAppSpacing.space4)

            when (state) {

                NotesUiState.Loading -> NotesLoadingScreen(
                    label = stringResource(R.string.notes_loading),
                    modifier = contentModifier,
                )

                NotesUiState.Empty -> NotesEmptyScreen(
                    title = stringResource(R.string.notes_empty_title),
                    description = stringResource(R.string.notes_empty_description),
                    actionLabel = stringResource(R.string.notes_create),
                    onCreateNote = onCreateNote,
                    modifier = contentModifier,
                )

                is NotesUiState.Content -> NotesContentScreen(
                    notes = state.notes,
                    createdDateFormat = stringResource(R.string.note_created_date),
                    modifier = contentModifier,
                )

                NotesUiState.Error -> NotesErrorScreen(
                    title = stringResource(R.string.notes_error_title),
                    retryLabel = stringResource(R.string.retry),
                    onRetry = onRetry,
                    modifier = contentModifier,
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenPreview() {
    NotesAppTheme {
        NotesScreen(
            state = NotesUiState.Empty,
            onCreateNote = {},
            onRetry = {},
        )
    }
}
