package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens

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
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
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
import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.NotesContentState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.NotesUiState
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.feedback.DeleteNoteDialog
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.feedback.NotesActionErrorBanner
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens.content.NotesGrid
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.topbar.NotesTopBar
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens.content.NotesListScreen

@Composable
fun NotesScreen(
    state: NotesUiState,
    bottomNavigationPadding: PaddingValues = PaddingValues(0.dp),
    handledNotesRevision: Long,
    onNotesRevisionHandled: (Long) -> Unit,
    onCreateNote: () -> Unit,
    onOpenNote: (Long) -> Unit,
    onDraftQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearSearch: () -> Unit,
    onSortOrderChange: (NotesSortOrder) -> Unit,
    onViewModeChange: (NotesViewMode) -> Unit,
    onRetryViewModeChange: () -> Unit,
    onDismissViewModeError: () -> Unit,
    onToggleDeleteMode: () -> Unit,
    onRequestDeleteNote: (Long, String) -> Unit,
    onConfirmDeleteNote: () -> Unit,
    onCancelDeleteNote: () -> Unit,
    onRetryDelete: () -> Unit,
    onDismissDeleteError: () -> Unit,
    onRetryLoading: () -> Unit,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    snackbarHost: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val deleteDescriptionFormat = stringResource(R.string.notes_delete_note)
    val screenTitle = stringResource(
        if (state.isDeleteMode) {
            R.string.notes_delete_mode_title
        } else {
            R.string.notes_title
        },
    )
    val bottomNavigationInset = bottomNavigationPadding.calculateBottomPadding()
    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()
    var isFabVisible by remember { mutableStateOf(true) }
    val fabVisibilityScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                isFabVisible = calculateFabVisibilityAfterScroll(
                    currentVisibility = isFabVisible,
                    scrollDelta = available.y,
                )
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(state.isDeleteMode) {
        if (!state.isDeleteMode) {
            isFabVisible = true
        }
    }
    SideEffect {
        if (shouldHandleNotesRevision(state.notesRevision, handledNotesRevision)) {
            if (
                shouldScrollNotesToStart(
                    scrollToStartOnNotesRevision = state.scrollToStartOnNotesRevision,
                    isNotesContentVisible = state.content is NotesContentState.Content,
                )
            ) {
                when (state.viewMode) {
                    NotesViewMode.List -> listState.requestScrollToItem(0)
                    NotesViewMode.Grid -> gridState.requestScrollToItem(0)
                }
            }
            onNotesRevisionHandled(state.notesRevision)
        }
    }
    val isFloatingActionButtonVisible = !state.isDeleteMode && isFabVisible
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(fabVisibilityScrollConnection)
            .semantics { paneTitle = screenTitle },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal),
        topBar = {
            NotesTopBar(
                title = screenTitle,
                isDeleteMode = state.isDeleteMode,
                deleteActionEnabled = state.isDeleteMode ||
                    state.content is NotesContentState.Content,
                sortOrder = state.sortOrder,
                viewMode = state.viewMode,
                isViewModeChangeEnabled = !state.isViewModeSaving,
                sortContentDescription = stringResource(R.string.notes_sort),
                newestFirstLabel = stringResource(R.string.notes_sort_newest_first),
                oldestFirstLabel = stringResource(R.string.notes_sort_oldest_first),
                showGridLabel = stringResource(R.string.notes_show_grid),
                showListLabel = stringResource(R.string.notes_show_list),
                listStateDescription = stringResource(R.string.notes_view_mode_list),
                gridStateDescription = stringResource(R.string.notes_view_mode_grid),
                enterDeleteModeLabel = stringResource(R.string.notes_enter_delete_mode),
                exitDeleteModeLabel = stringResource(R.string.notes_exit_delete_mode),
                onSortOrderChange = onSortOrderChange,
                onViewModeChange = onViewModeChange,
                onToggleDeleteMode = onToggleDeleteMode,
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
                    onClick = onCreateNote,
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
                        contentDescription = stringResource(R.string.notes_create),
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
                    placeholder = stringResource(R.string.notes_search_placeholder),
                    searchContentDescription = stringResource(R.string.notes_search),
                    clearContentDescription = stringResource(R.string.notes_clear_search),
                    onQueryChange = onDraftQueryChange,
                    onSearch = onSearch,
                    onClear = onClearSearch,
                    modifier = Modifier.padding(top = NotesAppSpacing.space2),
                )
                if (state.isDeleteMode && state.failedDeleteNoteId != null) {
                    NotesActionErrorBanner(
                        message = stringResource(R.string.notes_delete_error),
                        retryLabel = stringResource(R.string.retry),
                        dismissLabel = stringResource(R.string.notes_dismiss_delete_error),
                        onRetry = onRetryDelete,
                        onDismiss = onDismissDeleteError,
                        modifier = Modifier.padding(top = NotesAppSpacing.space3),
                    )
                }
                if (state.hasViewModeSaveError) {
                    NotesActionErrorBanner(
                        message = stringResource(R.string.notes_view_mode_save_error),
                        retryLabel = stringResource(R.string.retry),
                        dismissLabel = stringResource(R.string.notes_dismiss_view_mode_error),
                        onRetry = onRetryViewModeChange,
                        onDismiss = onDismissViewModeError,
                        modifier = Modifier.padding(top = NotesAppSpacing.space3),
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    val screenModifier = Modifier.fillMaxSize()
                    val bottomNavigationSafeScreenModifier = screenModifier.padding(
                        bottom = bottomNavigationInset,
                    )
                    when (val content = state.content) {

                        NotesContentState.Loading -> NotesLoadingScreen(
                            label = stringResource(R.string.notes_loading),
                            modifier = bottomNavigationSafeScreenModifier,
                        )

                        NotesContentState.Empty -> NotesEmptyScreen(
                            title = stringResource(R.string.notes_empty_title),
                            modifier = bottomNavigationSafeScreenModifier,
                        )

                        NotesContentState.SearchEmpty -> NotesSearchEmptyScreen(
                            title = stringResource(R.string.notes_search_empty_title),
                            modifier = bottomNavigationSafeScreenModifier,
                        )

                        is NotesContentState.Content -> when (state.viewMode) {
                            NotesViewMode.List -> NotesListScreen(
                                notes = content.notes,
                                state = listState,
                                createdDateFormat = stringResource(R.string.note_created_date),
                                deleteDescription = { title ->
                                    deleteDescriptionFormat.format(title)
                                },
                                isDeleteMode = state.isDeleteMode,
                                deletingNoteIds = state.deletingNoteIds,
                                onOpenNote = onOpenNote,
                                onDeleteNote = onRequestDeleteNote,
                                loadImage = loadImage,
                                bottomContentPadding = bottomNavigationInset,
                                modifier = screenModifier,
                            )

                            NotesViewMode.Grid -> NotesGrid(
                                notes = content.notes,
                                state = gridState,
                                createdDateFormat = stringResource(R.string.note_created_date),
                                deleteDescription = { title ->
                                    deleteDescriptionFormat.format(title)
                                },
                                isDeleteMode = state.isDeleteMode,
                                deletingNoteIds = state.deletingNoteIds,
                                onOpenNote = onOpenNote,
                                onDeleteNote = onRequestDeleteNote,
                                loadImage = loadImage,
                                bottomContentPadding = bottomNavigationInset,
                                modifier = screenModifier,
                            )
                        }

                        NotesContentState.Error -> NotesErrorScreen(
                            title = stringResource(R.string.notes_error_title),
                            retryLabel = stringResource(R.string.retry),
                            onRetry = onRetryLoading,
                            modifier = bottomNavigationSafeScreenModifier,
                        )

                    }
                }
            }
        }
    }

    state.deleteConfirmation?.let { confirmation ->
        DeleteNoteDialog(
            title = stringResource(R.string.notes_delete_dialog_title),
            message = stringResource(
                R.string.notes_delete_dialog_message,
                confirmation.noteTitle,
            ),
            cancelLabel = stringResource(R.string.notes_delete_cancel),
            deleteLabel = stringResource(R.string.notes_delete),
            deletingDescription = stringResource(R.string.notes_deleting),
            isDeleting = confirmation.isDeleting,
            onConfirm = onConfirmDeleteNote,
            onDismiss = onCancelDeleteNote,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesScreenPreview() {
    NotesAppTheme {
        NotesScreen(
            state = NotesUiState(content = NotesContentState.Empty),
            handledNotesRevision = 0L,
            onNotesRevisionHandled = {},
            onCreateNote = {},
            onOpenNote = {},
            onDraftQueryChange = {},
            onSearch = {},
            onClearSearch = {},
            onSortOrderChange = {},
            onViewModeChange = {},
            onRetryViewModeChange = {},
            onDismissViewModeError = {},
            onToggleDeleteMode = {},
            onRequestDeleteNote = { _, _ -> },
            onConfirmDeleteNote = {},
            onCancelDeleteNote = {},
            onRetryDelete = {},
            onDismissDeleteError = {},
            onRetryLoading = {},
            loadImage = { _, _, _, _ -> null },
            snackbarHost = {},
        )
    }
}

internal fun calculateFabVisibilityAfterScroll(
    currentVisibility: Boolean,
    scrollDelta: Float,
): Boolean = when {
    scrollDelta < 0f -> false
    scrollDelta > 0f -> true
    else -> currentVisibility
}

internal fun shouldScrollNotesToStart(
    scrollToStartOnNotesRevision: Boolean,
    isNotesContentVisible: Boolean,
): Boolean = scrollToStartOnNotesRevision && isNotesContentVisible

internal fun shouldHandleNotesRevision(
    notesRevision: Long,
    handledNotesRevision: Long,
): Boolean = notesRevision != 0L && notesRevision != handledNotesRevision
