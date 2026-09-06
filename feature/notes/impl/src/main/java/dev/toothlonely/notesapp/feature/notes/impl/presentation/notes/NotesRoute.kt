package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import androidx.activity.compose.BackHandler
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens.NotesScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesRoute(
    onCreateNote: () -> Unit,
    onOpenNote: (Long) -> Unit,
    viewModel: NotesViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val noteDeletedMessage = stringResource(R.string.notes_deleted)

    BackHandler(enabled = state.value.isDeleteMode) {
        viewModel.exitDeleteMode()
    }

    LaunchedEffect(viewModel, noteDeletedMessage) {
        viewModel.events.collect { event ->
            when (event) {
                NotesEvent.NoteDeleted -> launch {
                    snackbarHostState.showSnackbar(noteDeletedMessage)
                }
            }
        }
    }

    NotesScreen(
        state = state.value,
        onCreateNote = onCreateNote,
        onOpenNote = onOpenNote,
        onToggleDeleteMode = viewModel::toggleDeleteMode,
        onDeleteNote = viewModel::deleteNote,
        onRetryDelete = viewModel::retryDelete,
        onDismissDeleteError = viewModel::dismissDeleteError,
        onRetryLoading = viewModel::retryLoading,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    )
}
