package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.screens

import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.TasksVoiceInputUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TasksScreenScrollBehaviorTest {
    @Test
    fun `recording keeps stop fab visible regardless of scroll`() {
        assertTrue(
            shouldShowTasksFab(
                isContentLoaded = true,
                isEditorVisible = false,
                voiceInput = TasksVoiceInputUiState.Recording(),
                isVisibleByScroll = false,
            ),
        )
    }

    @Test
    fun `voice processing hides tasks fab`() {
        assertFalse(
            shouldShowTasksFab(
                isContentLoaded = true,
                isEditorVisible = false,
                voiceInput = TasksVoiceInputUiState.SpeechProcessing,
                isVisibleByScroll = true,
            ),
        )
    }

    @Test
    fun `scrolling down hides tasks fab`() {
        assertFalse(
            calculateTasksFabVisibilityAfterScroll(
                currentVisibility = true,
                scrollDelta = -1f,
            ),
        )
    }

    @Test
    fun `scrolling up shows tasks fab`() {
        assertTrue(
            calculateTasksFabVisibilityAfterScroll(
                currentVisibility = false,
                scrollDelta = 1f,
            ),
        )
    }

    @Test
    fun `stationary list preserves tasks fab visibility`() {
        assertFalse(
            calculateTasksFabVisibilityAfterScroll(
                currentVisibility = false,
                scrollDelta = 0f,
            ),
        )
        assertTrue(
            calculateTasksFabVisibilityAfterScroll(
                currentVisibility = true,
                scrollDelta = 0f,
            ),
        )
    }

    @Test
    fun `opening task creation scrolls task list to start`() {
        assertTrue(
            shouldScrollTasksToStart(
                wasEditorVisible = false,
                isEditorVisible = true,
                isCreatingTask = true,
                isListVisible = true,
            ),
        )
    }

    @Test
    fun `opening existing task editor preserves task list position`() {
        assertFalse(
            shouldScrollTasksToStart(
                wasEditorVisible = false,
                isEditorVisible = true,
                isCreatingTask = false,
                isListVisible = true,
            ),
        )
    }

    @Test
    fun `closing inline editor preserves task list position`() {
        assertFalse(
            shouldScrollTasksToStart(
                wasEditorVisible = true,
                isEditorVisible = false,
                isCreatingTask = false,
                isListVisible = true,
            ),
        )
    }

    @Test
    fun `editor updates preserve task list position`() {
        assertFalse(
            shouldScrollTasksToStart(
                wasEditorVisible = true,
                isEditorVisible = true,
                isCreatingTask = true,
                isListVisible = true,
            ),
        )
    }

    @Test
    fun `hidden task list is not scrolled when creation opens`() {
        assertFalse(
            shouldScrollTasksToStart(
                wasEditorVisible = false,
                isEditorVisible = true,
                isCreatingTask = true,
                isListVisible = false,
            ),
        )
    }
}
