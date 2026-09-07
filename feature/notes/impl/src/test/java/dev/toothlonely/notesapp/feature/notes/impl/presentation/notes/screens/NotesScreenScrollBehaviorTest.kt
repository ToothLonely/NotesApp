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
}
