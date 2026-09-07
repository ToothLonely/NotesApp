package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TasksScreenScrollBehaviorTest {
    @Test
    fun `opening inline editor scrolls task list to start`() {
        assertTrue(
            shouldScrollTasksToStart(
                wasEditorVisible = false,
                isEditorVisible = true,
                isListVisible = true,
            ),
        )
    }

    @Test
    fun `closing inline editor scrolls task list to start`() {
        assertTrue(
            shouldScrollTasksToStart(
                wasEditorVisible = true,
                isEditorVisible = false,
                isListVisible = true,
            ),
        )
    }

    @Test
    fun `editor updates do not reset task list position`() {
        assertFalse(
            shouldScrollTasksToStart(
                wasEditorVisible = true,
                isEditorVisible = true,
                isListVisible = true,
            ),
        )
    }

    @Test
    fun `hidden task list is not scrolled`() {
        assertFalse(
            shouldScrollTasksToStart(
                wasEditorVisible = true,
                isEditorVisible = false,
                isListVisible = false,
            ),
        )
    }
}
