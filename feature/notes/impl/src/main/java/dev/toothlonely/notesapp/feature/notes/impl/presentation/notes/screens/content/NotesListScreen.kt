package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.card.NoteCard

@Composable
fun NotesListScreen(
    notes: List<Note>,
    state: LazyListState,
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
    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(
            top = NotesAppSpacing.space4,
            bottom = NotesAppSizes.fab + NotesAppSpacing.space6 + bottomContentPadding,
        ),
        verticalArrangement = Arrangement.spacedBy(NotesAppSpacing.space3),
    ) {
        items(items = notes, key = Note::id) { note ->
            NoteCard(
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

@Preview(showBackground = true)
@Composable
private fun NotesListScreenPreview() {
    NotesAppTheme {
        NotesListScreen(
            notes = listOf(
                Note(1, "Первая заметка", "Текст", 1_750_000_000_000),
                Note(2, "Вторая заметка", "Текст", 1_750_000_100_000),
            ),
            state = rememberLazyListState(),
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
