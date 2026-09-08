package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.component.AppSearchField
import dev.toothlonely.notesapp.core.designsystem.component.BottomNavigationAwareSnackbarHost
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.tasks.impl.R
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.Task
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskSortOrder
import dev.toothlonely.notesapp.feature.tasks.impl.domain.model.TaskStatusFilter
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.InlineTaskEditorError
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.TasksContentState
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.TasksUiState
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components.DeleteTaskDialog
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components.TaskStatusFilters
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components.TasksActionErrorBanner
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components.TasksTopBar

@Composable
fun TasksScreen(
    state: TasksUiState,
    bottomNavigationPadding: PaddingValues = PaddingValues(0.dp),
    onAddTask: () -> Unit,
    onDraftQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onStatusFilterChange: (TaskStatusFilter) -> Unit,
    onSortOrderChange: (TaskSortOrder) -> Unit,
    onResetSearchAndFilter: () -> Unit,
    onDraftTitleChange: (String) -> Unit,
    onConfirmTask: () -> Unit,
    onCancelTask: () -> Unit,
    onEditTask: (Long) -> Unit,
    onRequestDeleteTask: (Long) -> Unit,
    onConfirmDeleteTask: () -> Unit,
    onCancelDeleteTask: () -> Unit,
    onRetryDeleteTask: () -> Unit,
    onDismissDeleteError: () -> Unit,
    onToggleTaskStatus: (Long) -> Unit,
    onRetryStatusUpdate: () -> Unit,
    onDismissStatusUpdateError: () -> Unit,
    onRetryLoading: () -> Unit,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val screenTitle = stringResource(R.string.tasks_title)
    val bottomNavigationInset = bottomNavigationPadding.calculateBottomPadding()
    val listState = rememberLazyListState()
    var wasEditorVisible by remember { mutableStateOf(false) }
    var isFabVisible by remember { mutableStateOf(true) }
    val fabVisibilityScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                isFabVisible = calculateTasksFabVisibilityAfterScroll(
                    currentVisibility = isFabVisible,
                    scrollDelta = available.y,
                )
                return Offset.Zero
            }
        }
    }
    val activeTaskDescriptionFormat = stringResource(R.string.tasks_active_content_description)
    val completedTaskDescriptionFormat = stringResource(
        R.string.tasks_completed_content_description,
    )
    val actionsDescriptionFormat = stringResource(R.string.tasks_actions_description)
    val editDescriptionFormat = stringResource(R.string.tasks_edit_description)
    val deleteDescriptionFormat = stringResource(R.string.tasks_delete_description)
    val isEditing = state.editor?.taskId != null
    val editorErrorMessage = when (state.editor?.error) {
        InlineTaskEditorError.EmptyTitle -> stringResource(R.string.tasks_title_empty_error)
        InlineTaskEditorError.Storage -> stringResource(
            if (isEditing) R.string.tasks_edit_storage_error else R.string.tasks_create_storage_error,
        )
        null -> null
    }
    val loadedContent = state.content is TasksContentState.Empty ||
        state.content is TasksContentState.SearchEmpty ||
        state.content is TasksContentState.Content
    val isEditorVisible = state.editor != null
    val isCreatingTask = state.editor?.taskId == null && isEditorVisible
    val isFloatingActionButtonVisible = loadedContent && state.editor == null && isFabVisible

    SideEffect {
        if (
            shouldScrollTasksToStart(
                wasEditorVisible = wasEditorVisible,
                isEditorVisible = isEditorVisible,
                isCreatingTask = isCreatingTask,
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
            .nestedScroll(fabVisibilityScrollConnection)
            .semantics { paneTitle = screenTitle },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            TasksTopBar(
                title = screenTitle,
                sortOrder = state.sortOrder,
                sortContentDescription = stringResource(R.string.tasks_sort),
                newestFirstLabel = stringResource(R.string.tasks_sort_newest_first),
                oldestFirstLabel = stringResource(R.string.tasks_sort_oldest_first),
                onSortOrderChange = onSortOrderChange,
            )
        },
        snackbarHost = {
            BottomNavigationAwareSnackbarHost(
                bottomNavigationPadding = bottomNavigationPadding,
                isFloatingActionButtonVisible = isFloatingActionButtonVisible,
                snackbarHost = snackbarHost,
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = isFloatingActionButtonVisible,
                enter = slideInVertically { fullHeight -> fullHeight } + fadeIn(),
                exit = slideOutVertically { fullHeight -> fullHeight } + fadeOut(),
            ) {
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
                AppSearchField(
                    query = state.draftQuery,
                    placeholder = stringResource(R.string.tasks_search_placeholder),
                    searchContentDescription = stringResource(R.string.tasks_search),
                    clearContentDescription = stringResource(R.string.tasks_clear_search),
                    onQueryChange = onDraftQueryChange,
                    onSearch = onSearch,
                    onClear = onClearSearch,
                    modifier = Modifier.padding(top = NotesAppSpacing.space2),
                )
                TaskStatusFilters(
                    selectedFilter = state.statusFilter,
                    allLabel = stringResource(R.string.tasks_filter_all),
                    activeLabel = stringResource(R.string.tasks_filter_active),
                    completedLabel = stringResource(R.string.tasks_filter_completed),
                    onFilterSelected = onStatusFilterChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = NotesAppSpacing.space2),
                )
                state.deleteError?.let {
                    TasksActionErrorBanner(
                        message = stringResource(R.string.tasks_delete_error),
                        retryLabel = stringResource(R.string.tasks_retry),
                        dismissLabel = stringResource(R.string.tasks_dismiss_delete_error),
                        onRetry = onRetryDeleteTask,
                        onDismiss = onDismissDeleteError,
                        modifier = Modifier.padding(top = NotesAppSpacing.space2),
                    )
                }
                state.statusUpdateError?.let {
                    TasksActionErrorBanner(
                        message = stringResource(R.string.tasks_status_update_error),
                        retryLabel = stringResource(R.string.tasks_retry),
                        dismissLabel = stringResource(R.string.tasks_dismiss_status_error),
                        onRetry = onRetryStatusUpdate,
                        onDismiss = onDismissStatusUpdateError,
                        modifier = Modifier.padding(top = NotesAppSpacing.space2),
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

                        state.content is TasksContentState.SearchEmpty && state.editor == null ->
                            TasksSearchEmptyScreen(
                                title = stringResource(R.string.tasks_search_empty),
                                resetLabel = stringResource(R.string.tasks_reset_filters),
                                onReset = onResetSearchAndFilter,
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
                            taskActionsContentDescription = { task ->
                                actionsDescriptionFormat.format(task.title)
                            },
                            editTaskContentDescription = { task ->
                                editDescriptionFormat.format(task.title)
                            },
                            deleteTaskContentDescription = { task ->
                                deleteDescriptionFormat.format(task.title)
                            },
                            expandTaskTitleDescription = stringResource(
                                R.string.tasks_expand_title,
                            ),
                            collapseTaskTitleDescription = stringResource(
                                R.string.tasks_collapse_title,
                            ),
                            activeStateDescription = stringResource(R.string.tasks_active_state),
                            completedStateDescription = stringResource(
                                R.string.tasks_completed_state,
                            ),
                            editLabel = stringResource(R.string.tasks_edit),
                            deleteLabel = stringResource(R.string.tasks_delete),
                            editorPlaceholder = stringResource(R.string.tasks_title_placeholder),
                            editorModeDescription = stringResource(
                                if (isEditing) R.string.tasks_edit_mode else R.string.tasks_create_mode,
                            ),
                            editorErrorMessage = editorErrorMessage,
                            confirmTaskDescription = stringResource(
                                if (isEditing) R.string.tasks_confirm_edit else R.string.tasks_confirm_create,
                            ),
                            cancelTaskDescription = stringResource(
                                if (isEditing) R.string.tasks_cancel_edit else R.string.tasks_cancel_create,
                            ),
                            savingTaskDescription = stringResource(R.string.tasks_saving),
                            onDraftTitleChange = onDraftTitleChange,
                            onConfirmTask = onConfirmTask,
                            onCancelTask = onCancelTask,
                            onToggleTaskStatus = onToggleTaskStatus,
                            onEditTask = onEditTask,
                            onDeleteTask = onRequestDeleteTask,
                            bottomContentPadding = bottomNavigationInset,
                            state = listState,
                        )
                    }
                }
            }
        }
    }

    state.deleteConfirmation?.let { confirmation ->
        DeleteTaskDialog(
            title = stringResource(R.string.tasks_delete_dialog_title),
            message = stringResource(
                R.string.tasks_delete_dialog_message,
                confirmation.taskTitle,
            ),
            cancelLabel = stringResource(R.string.tasks_delete_cancel),
            deleteLabel = stringResource(R.string.tasks_delete),
            deletingDescription = stringResource(R.string.tasks_deleting),
            isDeleting = confirmation.isDeleting,
            onConfirm = onConfirmDeleteTask,
            onDismiss = onCancelDeleteTask,
        )
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
            onDraftQueryChange = {},
            onSearch = {},
            onClearSearch = {},
            onStatusFilterChange = {},
            onSortOrderChange = {},
            onResetSearchAndFilter = {},
            onDraftTitleChange = {},
            onConfirmTask = {},
            onCancelTask = {},
            onEditTask = {},
            onRequestDeleteTask = {},
            onConfirmDeleteTask = {},
            onCancelDeleteTask = {},
            onRetryDeleteTask = {},
            onDismissDeleteError = {},
            onToggleTaskStatus = {},
            onRetryStatusUpdate = {},
            onDismissStatusUpdateError = {},
            onRetryLoading = {},
            snackbarHost = {},
        )
    }
}

internal fun shouldScrollTasksToStart(
    wasEditorVisible: Boolean,
    isEditorVisible: Boolean,
    isCreatingTask: Boolean,
    isListVisible: Boolean,
): Boolean = isListVisible && !wasEditorVisible && isEditorVisible && isCreatingTask

internal fun calculateTasksFabVisibilityAfterScroll(
    currentVisibility: Boolean,
    scrollDelta: Float,
): Boolean = when {
    scrollDelta < 0f -> false
    scrollDelta > 0f -> true
    else -> currentVisibility
}
