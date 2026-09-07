package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens.TasksScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TasksRoute(
    bottomNavigationPadding: PaddingValues,
    viewModel: TasksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    BackHandler(enabled = state.editor != null && state.editor?.isSaving == false) {
        viewModel.cancelTaskCreation()
    }

    TasksScreen(
        state = state,
        bottomNavigationPadding = bottomNavigationPadding,
        onAddTask = viewModel::startCreatingTask,
        onDraftTitleChange = viewModel::updateDraftTitle,
        onConfirmTask = viewModel::confirmTaskCreation,
        onCancelTask = viewModel::cancelTaskCreation,
        onToggleTaskStatus = viewModel::toggleTaskStatus,
        onRetryStatusUpdate = viewModel::retryStatusUpdate,
        onDismissStatusUpdateError = viewModel::dismissStatusUpdateError,
        onRetryLoading = viewModel::retryLoading,
    )
}
