package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.topbar

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.NotesViewMode

@Composable
fun NotesViewModeButton(
    viewMode: NotesViewMode,
    showGridLabel: String,
    showListLabel: String,
    listStateDescription: String,
    gridStateDescription: String,
    enabled: Boolean,
    onViewModeChange: (NotesViewMode) -> Unit,
) {
    val isGrid = viewMode == NotesViewMode.Grid
    IconButton(
        onClick = {
            onViewModeChange(if (isGrid) NotesViewMode.List else NotesViewMode.Grid)
        },
        modifier = Modifier.semantics {
            stateDescription = if (isGrid) gridStateDescription else listStateDescription
        },
        enabled = enabled,
    ) {
        Icon(
            painter = painterResource(
                if (isGrid) {
                    DesignSystemR.drawable.ic_view_list_24
                } else {
                    DesignSystemR.drawable.ic_grid_view_24
                },
            ),
            contentDescription = if (isGrid) showListLabel else showGridLabel,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesViewModeButtonPreview() {
    NotesAppTheme {
        NotesViewModeButton(
            viewMode = NotesViewMode.List,
            showGridLabel = "Показать заметки сеткой",
            showListLabel = "Показать заметки списком",
            listStateDescription = "Список",
            gridStateDescription = "Сетка",
            enabled = true,
            onViewModeChange = {},
        )
    }
}
