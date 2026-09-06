package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.screens.NoteEditorScreen
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteEditorRoute(
    noteId: Long?,
    onBack: () -> Unit,
    viewModel: NoteEditorViewModel = koinViewModel(
        parameters = { parametersOf(NoteEditorArgs(noteId)) },
    ),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val content = state.value as? NoteEditorUiState.Content

    BackHandler(
        enabled = content?.mode == NoteEditorMode.Editing || content?.isSaving == true,
    ) {
        viewModel.cancelEditing()
    }

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
        onEdit = viewModel::startEditing,
        onRetryPreparation = viewModel::retryPreparation,
        onBack = {
            if (!viewModel.cancelEditing()) onBack()
        },
    )
}
