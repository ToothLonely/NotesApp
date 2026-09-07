package dev.toothlonely.notesapp.feature.notes.impl.presentation.notes.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotesScreenScrollBehaviorTest {
    @Test
    fun `scrolling content down hides fab`() {
        assertFalse(
            calculateFabVisibilityAfterScroll(
                currentVisibility = true,
                scrollDelta = -1f,
            ),
        )
    }

    @Test
    fun `scrolling content up shows fab`() {
        assertTrue(
            calculateFabVisibilityAfterScroll(
                currentVisibility = false,
                scrollDelta = 1f,
            ),
        )
    }

    @Test
    fun `zero scroll keeps current fab visibility`() {
        assertTrue(
            calculateFabVisibilityAfterScroll(
                currentVisibility = true,
                scrollDelta = 0f,
            ),
        )
        assertFalse(
            calculateFabVisibilityAfterScroll(
                currentVisibility = false,
                scrollDelta = 0f,
            ),
        )
    }

    @Test
    fun `new room revision scrolls visible notes content to start`() {
        assertTrue(
            shouldScrollNotesToStart(
                scrollToStartOnNotesRevision = true,
                isNotesContentVisible = true,
            ),
        )
    }

    @Test
    fun `deletion revision preserves list position`() {
        assertFalse(
            shouldScrollNotesToStart(
                scrollToStartOnNotesRevision = false,
                isNotesContentVisible = true,
            ),
        )
    }

    @Test
    fun `scroll request waits until notes content is visible`() {
        assertFalse(
            shouldScrollNotesToStart(
                scrollToStartOnNotesRevision = true,
                isNotesContentVisible = false,
            ),
        )
    }

    @Test
    fun `new non-zero revision is handled`() {
        assertTrue(
            shouldHandleNotesRevision(
                notesRevision = 2L,
                handledNotesRevision = 1L,
            ),
        )
    }

    @Test
    fun `already handled revision is ignored`() {
        assertFalse(
            shouldHandleNotesRevision(
                notesRevision = 2L,
                handledNotesRevision = 2L,
            ),
        )
    }

    @Test
    fun `initial loading revision is ignored`() {
        assertFalse(
            shouldHandleNotesRevision(
                notesRevision = 0L,
                handledNotesRevision = 0L,
            ),
        )
    }
}
