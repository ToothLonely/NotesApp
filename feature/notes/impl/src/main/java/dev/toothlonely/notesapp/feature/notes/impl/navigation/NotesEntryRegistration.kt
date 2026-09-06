package dev.toothlonely.notesapp.feature.notes.impl.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.toothlonely.notesapp.feature.notes.api.NoteEditorRoute
import dev.toothlonely.notesapp.feature.notes.api.NotesRoute
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteEditorRoute as NoteEditorRouteContent
import dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.NotesRoute as NotesRouteContent

fun EntryProviderScope<NavKey>.notesEntries(
    onCreateNote: () -> Unit,
    onCloseEditor: () -> Unit,
) {
    entry<NotesRoute> {
        NotesRouteContent(onCreateNote = onCreateNote)
    }
    entry<NoteEditorRoute> { route ->
        NoteEditorRouteContent(
            noteId = route.noteId,
            onBack = onCloseEditor,
        )
    }
}
