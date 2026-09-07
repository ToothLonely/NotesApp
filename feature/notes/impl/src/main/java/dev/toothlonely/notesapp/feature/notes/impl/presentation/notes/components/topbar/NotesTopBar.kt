package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.topbar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.component.SortMenu
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesSortOrder
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesTopBar(
    title: String,
    isDeleteMode: Boolean,
    deleteActionEnabled: Boolean,
    sortOrder: NotesSortOrder,
    viewMode: NotesViewMode,
    isViewModeChangeEnabled: Boolean,
    sortContentDescription: String,
    newestFirstLabel: String,
    oldestFirstLabel: String,
    showGridLabel: String,
    showListLabel: String,
    listStateDescription: String,
    gridStateDescription: String,
    enterDeleteModeLabel: String,
    exitDeleteModeLabel: String,
    onSortOrderChange: (NotesSortOrder) -> Unit,
    onViewModeChange: (NotesViewMode) -> Unit,
    onToggleDeleteMode: () -> Unit,
) {
    TopAppBar(
        title = { Text(text = title) },
        actions = {
            SortMenu(
                newestFirst = sortOrder == NotesSortOrder.NewestFirst,
                sortContentDescription = sortContentDescription,
                newestFirstLabel = newestFirstLabel,
                oldestFirstLabel = oldestFirstLabel,
                onNewestFirst = { onSortOrderChange(NotesSortOrder.NewestFirst) },
                onOldestFirst = { onSortOrderChange(NotesSortOrder.OldestFirst) },
            )
            NotesViewModeButton(
                viewMode = viewMode,
                showGridLabel = showGridLabel,
                showListLabel = showListLabel,
                listStateDescription = listStateDescription,
                gridStateDescription = gridStateDescription,
                enabled = isViewModeChangeEnabled,
                onViewModeChange = onViewModeChange,
            )
            IconButton(
                onClick = onToggleDeleteMode,
                enabled = deleteActionEnabled,
            ) {
                Icon(
                    painter = painterResource(
                        if (isDeleteMode) {
                            DesignSystemR.drawable.ic_close_24
                        } else {
                            DesignSystemR.drawable.ic_delete_24
                        },
                    ),
                    contentDescription = if (isDeleteMode) {
                        exitDeleteModeLabel
                    } else {
                        enterDeleteModeLabel
                    },
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        ),
    )
}

@Preview(showBackground = true)
@Composable
private fun NotesTopBarPreview() {
    NotesAppTheme {
        NotesTopBar(
            title = "Заметки",
            isDeleteMode = false,
            deleteActionEnabled = true,
            sortOrder = NotesSortOrder.NewestFirst,
            viewMode = NotesViewMode.List,
            isViewModeChangeEnabled = true,
            sortContentDescription = "Сортировать заметки",
            newestFirstLabel = "Сначала новые",
            oldestFirstLabel = "Сначала старые",
            showGridLabel = "Показать заметки сеткой",
            showListLabel = "Показать заметки списком",
            listStateDescription = "Список",
            gridStateDescription = "Сетка",
            enterDeleteModeLabel = "Включить режим удаления",
            exitDeleteModeLabel = "Выйти из режима удаления",
            onSortOrderChange = {},
            onViewModeChange = {},
            onToggleDeleteMode = {},
        )
    }
}
