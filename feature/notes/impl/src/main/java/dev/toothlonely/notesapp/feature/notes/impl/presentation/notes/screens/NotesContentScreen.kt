package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.domain.Note
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.components.NoteCard

@Composable
fun NotesContentScreen(
    notes: List<Note>,
    createdDateFormat: String,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            top = NotesAppSpacing.space4,
            bottom = NotesAppSizes.fab + NotesAppSpacing.space6,
        ),
        verticalArrangement = Arrangement.spacedBy(NotesAppSpacing.space3),
    ) {
        items(items = notes, key = Note::id) { note ->
            NoteCard(
                note = note,
                createdDateFormat = createdDateFormat,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesContentScreenPreview() {
    NotesAppTheme {
        NotesContentScreen(
            notes = listOf(Note(1, "Первая заметка", "Текст", 1_750_000_000_000)),
            createdDateFormat = "Создано %s",
        )
    }
}
