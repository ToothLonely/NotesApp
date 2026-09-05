package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens.NotesScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesRoute(
    onCreateNote: () -> Unit,
    viewModel: NotesViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    NotesScreen(
        state = state.value,
        onCreateNote = onCreateNote,
        onRetry = viewModel::retry,
    )
}
