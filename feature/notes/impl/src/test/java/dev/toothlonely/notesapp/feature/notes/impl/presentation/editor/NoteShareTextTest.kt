package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class NoteShareTextTest {
    @Test
    fun `share text contains title and body separated by an empty line`() {
        assertEquals(
            "Идеи для путешествия\n\nПосмотреть старый город",
            buildNoteShareText(
                title = "Идеи для путешествия",
                body = "Посмотреть старый город",
            ),
        )
    }

    @Test
    fun `share text does not append separators when body is blank`() {
        assertEquals(
            "Идеи для путешествия",
            buildNoteShareText(title = "Идеи для путешествия", body = ""),
        )
    }
}
