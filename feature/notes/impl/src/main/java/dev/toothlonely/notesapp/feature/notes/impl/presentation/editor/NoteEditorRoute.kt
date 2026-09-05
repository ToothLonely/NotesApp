package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.screens.NoteEditorScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NoteEditorRoute(
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = koinViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                NoteEditorEvent.SaveSucceeded -> onBack()
            }
        }
    }

    NoteEditorScreen(
        state = state.value,
        onTitleChanged = viewModel::onTitleChanged,
        onBodyChanged = viewModel::onBodyChanged,
        onSave = viewModel::save,
        onRetryPreparation = viewModel::retryPreparation,
        onBack = onBack,
    )
}
