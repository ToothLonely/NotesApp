package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.toothlonely.notesapp.feature.tasks.impl.R
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens.TasksScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TasksRoute(
    bottomNavigationPadding: PaddingValues,
    viewModel: TasksViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val taskDeletedMessage = stringResource(R.string.tasks_deleted)

    BackHandler(
        enabled = state.editor != null &&
            state.editor?.isSaving == false &&
            state.deleteConfirmation == null,
    ) {
        viewModel.cancelTaskEditor()
    }

    LaunchedEffect(viewModel, taskDeletedMessage) {
        viewModel.events.collect { event ->
            when (event) {
                TasksEvent.TaskDeleted -> launch {
                    snackbarHostState.showSnackbar(taskDeletedMessage)
                }
            }
        }
    }

    TasksScreen(
        state = state,
        bottomNavigationPadding = bottomNavigationPadding,
        onAddTask = viewModel::startCreatingTask,
        onDraftQueryChange = viewModel::updateDraftQuery,
        onSearch = viewModel::applySearch,
        onClearSearch = viewModel::clearSearch,
        onStatusFilterChange = viewModel::changeStatusFilter,
        onSortOrderChange = viewModel::changeSortOrder,
        onResetSearchAndFilter = viewModel::resetSearchAndFilter,
        onDraftTitleChange = viewModel::updateDraftTitle,
        onConfirmTask = viewModel::confirmTaskEditor,
        onCancelTask = viewModel::cancelTaskEditor,
        onEditTask = viewModel::startEditingTask,
        onRequestDeleteTask = viewModel::requestTaskDeletion,
        onConfirmDeleteTask = viewModel::confirmTaskDeletion,
        onCancelDeleteTask = viewModel::cancelTaskDeletion,
        onRetryDeleteTask = viewModel::retryTaskDeletion,
        onDismissDeleteError = viewModel::dismissTaskDeletionError,
        onToggleTaskStatus = viewModel::toggleTaskStatus,
        onRetryStatusUpdate = viewModel::retryStatusUpdate,
        onDismissStatusUpdateError = viewModel::dismissStatusUpdateError,
        onRetryLoading = viewModel::retryLoading,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    )
}
