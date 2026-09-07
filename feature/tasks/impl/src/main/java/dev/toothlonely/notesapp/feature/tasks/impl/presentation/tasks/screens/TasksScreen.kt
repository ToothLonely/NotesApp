package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.tasks.impl.R
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.InlineTaskEditorError
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.TasksContentState
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.TasksUiState
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components.TasksActionErrorBanner
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components.TasksTopBar

@Composable
fun TasksScreen(
    state: TasksUiState,
    bottomNavigationPadding: PaddingValues = PaddingValues(0.dp),
    onAddTask: () -> Unit,
    onDraftTitleChange: (String) -> Unit,
    onConfirmTask: () -> Unit,
    onCancelTask: () -> Unit,
    onToggleTaskStatus: (Long) -> Unit,
    onRetryStatusUpdate: () -> Unit,
    onDismissStatusUpdateError: () -> Unit,
    onRetryLoading: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenTitle = stringResource(R.string.tasks_title)
    val bottomNavigationInset = bottomNavigationPadding.calculateBottomPadding()
    val listState = rememberLazyListState()
    var wasEditorVisible by remember { mutableStateOf(false) }
    val activeTaskDescriptionFormat = stringResource(R.string.tasks_active_content_description,)
    val completedTaskDescriptionFormat = stringResource(R.string.tasks_completed_content_description,)
    val editorErrorMessage = when (state.editor?.error) {
        InlineTaskEditorError.EmptyTitle -> stringResource(R.string.tasks_title_empty_error)
        InlineTaskEditorError.Storage -> stringResource(R.string.tasks_create_storage_error)
        null -> null
    }
    val loadedContent = state.content is TasksContentState.Empty ||
        state.content is TasksContentState.Content
    val isEditorVisible = state.editor != null

    SideEffect {
        if (
            shouldScrollTasksToStart(
                wasEditorVisible = wasEditorVisible,
                isEditorVisible = isEditorVisible,
                isListVisible = loadedContent,
            )
        ) {
            listState.requestScrollToItem(0)
        }
        wasEditorVisible = isEditorVisible
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .semantics { paneTitle = screenTitle },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            TasksTopBar(title = screenTitle)
        },
        floatingActionButton = {
            if (loadedContent && state.editor == null) {
                FloatingActionButton(
                    onClick = onAddTask,
                    modifier = Modifier.padding(
                        end = NotesAppSpacing.space2,
                        bottom = bottomNavigationInset,
                    ),
                    shape = NotesAppShapes.full,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_add_24),
                        contentDescription = stringResource(R.string.tasks_add),
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
                if (state.statusUpdateError != null) {
                    TasksActionErrorBanner(
                        message = stringResource(R.string.tasks_status_update_error),
                        retryLabel = stringResource(R.string.tasks_retry),
                        dismissLabel = stringResource(R.string.tasks_dismiss_status_error),
                        onRetry = onRetryStatusUpdate,
                        onDismiss = onDismissStatusUpdateError,
                        modifier = Modifier.padding(top = NotesAppSpacing.space3),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    val safeScreenModifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomNavigationInset)
                    val tasks = (state.content as? TasksContentState.Content)?.tasks.orEmpty()
                    when {
                        state.content is TasksContentState.Loading -> TasksLoadingScreen(
                            label = stringResource(R.string.tasks_loading),
                            modifier = safeScreenModifier,
                        )

                        state.content is TasksContentState.Error -> TasksErrorScreen(
                            title = stringResource(R.string.tasks_load_error),
                            retryLabel = stringResource(R.string.tasks_retry),
                            onRetry = onRetryLoading,
                            modifier = safeScreenModifier,
                        )

                        state.content is TasksContentState.Empty && state.editor == null ->
                            TasksEmptyScreen(
                                title = stringResource(R.string.tasks_empty_title),
                                body = stringResource(R.string.tasks_empty_body),
                                addTaskLabel = stringResource(R.string.tasks_add),
                                onAddTask = onAddTask,
                                modifier = safeScreenModifier,
                            )

                        else -> TasksListScreen(
                            tasks = tasks,
                            editor = state.editor,
                            pendingStatusTaskIds = state.pendingStatusTaskIds,
                            taskStatusContentDescription = { task ->
                                if (task.isCompleted) {
                                    completedTaskDescriptionFormat.format(task.title)
                                } else {
                                    activeTaskDescriptionFormat.format(task.title)
                                }
                            },
                            activeStateDescription = stringResource(R.string.tasks_active_state),
                            completedStateDescription = stringResource(
                                R.string.tasks_completed_state,
                            ),
                            editorPlaceholder = stringResource(R.string.tasks_title_placeholder),
                            editorModeDescription = stringResource(R.string.tasks_create_mode),
                            editorErrorMessage = editorErrorMessage,
                            confirmTaskDescription = stringResource(
                                R.string.tasks_confirm_create,
                            ),
                            cancelTaskDescription = stringResource(R.string.tasks_cancel_create),
                            savingTaskDescription = stringResource(R.string.tasks_saving),
                            onDraftTitleChange = onDraftTitleChange,
                            onConfirmTask = onConfirmTask,
                            onCancelTask = onCancelTask,
                            onToggleTaskStatus = onToggleTaskStatus,
                            bottomContentPadding = bottomNavigationInset,
                            state = listState,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksScreenPreview() {
    NotesAppTheme {
        TasksScreen(
            state = TasksUiState(
                content = TasksContentState.Content(
                    listOf(
                        Task(1, "Подготовить презентацию", false, 2),
                        Task(2, "Позвонить маме", true, 1),
                    ),
                ),
            ),
            onAddTask = {},
            onDraftTitleChange = {},
            onConfirmTask = {},
            onCancelTask = {},
            onToggleTaskStatus = {},
            onRetryStatusUpdate = {},
            onDismissStatusUpdateError = {},
            onRetryLoading = {},
        )
    }
}

internal fun shouldScrollTasksToStart(
    wasEditorVisible: Boolean,
    isEditorVisible: Boolean,
    isListVisible: Boolean,
): Boolean = isListVisible && wasEditorVisible != isEditorVisible
