package dev.toothlonely.notesapp.feature.notes.impl.navigation

import dev.toothlonely.notesapp.feature.notes.api.NoteEditorRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotesNavigationTest {
    @Test
    fun `editor route carries existing note id`() {
        assertEquals(42L, NoteEditorRoute(noteId = 42).noteId)
    }

    @Test
    fun `editor route without id represents new note`() {
        assertNull(NoteEditorRoute().noteId)
    }

    @Test
    fun `destination remembers the last handled room revision`() {
        val destinationState = NotesDestinationState()

        destinationState.markNotesRevisionHandled(7L)

        assertEquals(7L, destinationState.handledNotesRevision)
    }
}
