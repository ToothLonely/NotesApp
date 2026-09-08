package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.domain.model.Note
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.card.NoteGridCard

@Composable
fun NotesGrid(
    notes: List<Note>,
    state: LazyGridState,
    createdDateFormat: String,
    deleteDescription: (String) -> String,
    isDeleteMode: Boolean,
    deletingNoteIds: Set<Long>,
    onOpenNote: (Long) -> Unit,
    onDeleteNote: (Long, String) -> Unit,
    loadImage: suspend (String, Boolean, Int, Int) -> ImageBitmap?,
    bottomContentPadding: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier) {
        val columns = if (maxWidth < NotesAppSizes.adaptiveContentThreshold) {
            GridCells.Fixed(COMPACT_GRID_COLUMNS)
        } else {
            GridCells.Adaptive(NotesAppSizes.noteGridMinimumCardWidth)
        }
        LazyVerticalGrid(
            columns = columns,
            state = state,
            contentPadding = PaddingValues(
                top = NotesAppSpacing.space4,
                bottom = NotesAppSizes.fab + NotesAppSpacing.space6 + bottomContentPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(NotesAppSizes.noteGridGap),
            verticalArrangement = Arrangement.spacedBy(NotesAppSizes.noteGridGap),
        ) {
            items(items = notes, key = Note::id) { note ->
                NoteGridCard(
                    note = note,
                    createdDateFormat = createdDateFormat,
                    deleteDescription = deleteDescription(note.title),
                    isDeleteMode = isDeleteMode,
                    isDeleting = note.id in deletingNoteIds,
                    onOpen = { onOpenNote(note.id) },
                    onDelete = { onDeleteNote(note.id, note.title) },
                    loadImage = loadImage,
                )
            }
        }
    }
}

private const val COMPACT_GRID_COLUMNS = 2

@Preview(showBackground = true)
@Composable
private fun NotesGridPreview() {
    NotesAppTheme {
        NotesGrid(
            notes = listOf(
                Note(1, "Первая заметка", "Текст", 1_750_000_000_000),
                Note(2, "Вторая заметка", "Текст", 1_750_000_100_000),
            ),
            state = androidx.compose.foundation.lazy.grid.rememberLazyGridState(),
            createdDateFormat = "Создано %s",
            deleteDescription = { title -> "Удалить заметку «$title»" },
            isDeleteMode = true,
            deletingNoteIds = emptySet(),
            onOpenNote = {},
            onDeleteNote = { _, _ -> },
            loadImage = { _, _, _, _ -> null },
        )
    }
}
