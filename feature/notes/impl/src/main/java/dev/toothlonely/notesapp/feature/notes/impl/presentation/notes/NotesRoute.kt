package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import dev.toothlonely.notesapp.feature.notes.impl.presentation.image.NoteImageLoader
import dev.toothlonely.notesapp.core.designsystem.component.AppSnackbarHost
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.navigation.NotesDestinationState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens.NotesScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.compose.koinInject

@Composable
fun NotesRoute(
    bottomNavigationPadding: PaddingValues,
    destinationState: NotesDestinationState,
    onCreateNote: () -> Unit,
    onOpenNote: (Long) -> Unit,
    viewModel: NotesViewModel = koinViewModel(),
    imageLoader: NoteImageLoader = koinInject(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val noteDeletedMessage = stringResource(R.string.notes_deleted)

    DisposableEffect(viewModel, destinationState) {
        val detachDeactivationAction = destinationState.attachDeactivationAction(
            viewModel::exitDeleteMode,
        )
        onDispose(detachDeactivationAction)
    }

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
        bottomNavigationPadding = bottomNavigationPadding,
        handledNotesRevision = destinationState.handledNotesRevision,
        onNotesRevisionHandled = destinationState::markNotesRevisionHandled,
        onCreateNote = onCreateNote,
        onOpenNote = onOpenNote,
        onDraftQueryChange = viewModel::updateDraftQuery,
        onSearch = viewModel::applySearch,
        onLoadMore = viewModel::loadMore,
        onClearSearch = viewModel::clearSearch,
        onSortOrderChange = viewModel::changeSortOrder,
        onViewModeChange = viewModel::changeViewMode,
        onRetryViewModeChange = viewModel::retryViewModeChange,
        onDismissViewModeError = viewModel::dismissViewModeSaveError,
        onToggleDeleteMode = viewModel::toggleDeleteMode,
        onRequestDeleteNote = viewModel::requestDeleteNote,
        onConfirmDeleteNote = viewModel::confirmDeleteNote,
        onCancelDeleteNote = viewModel::cancelDeleteNote,
        onRetryDelete = viewModel::retryDelete,
        onDismissDeleteError = viewModel::dismissDeleteError,
        onRetryLoading = viewModel::retryLoading,
        loadImage = imageLoader::load,
        snackbarHost = { AppSnackbarHost(hostState = snackbarHostState) },
    )
}
