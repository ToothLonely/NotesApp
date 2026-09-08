package dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.tasks.impl.R
import dev.toothlonely.notesapp.feature.tasks.impl.presentation.tasks.TasksVoiceInputUiState

@Composable
fun TasksVoiceStatusPanel(
    state: TasksVoiceInputUiState,
    modifier: Modifier = Modifier,
) {
    val message = when (state) {
        TasksVoiceInputUiState.Idle,
        is TasksVoiceInputUiState.PermissionDenied,
        is TasksVoiceInputUiState.Error,
            -> return
        is TasksVoiceInputUiState.Recording -> stringResource(
            R.string.tasks_voice_recording,
            state.durationSeconds / SECONDS_PER_MINUTE,
            state.durationSeconds % SECONDS_PER_MINUTE,
        )
        TasksVoiceInputUiState.SpeechProcessing ->
            stringResource(R.string.tasks_voice_recognizing)
        TasksVoiceInputUiState.GigaChatProcessing ->
            stringResource(R.string.tasks_voice_formulating)
    }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Polite },
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(NotesAppSpacing.space3),
            horizontalArrangement = Arrangement.spacedBy(NotesAppSpacing.space2),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (
                state is TasksVoiceInputUiState.SpeechProcessing ||
                state is TasksVoiceInputUiState.GigaChatProcessing
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(NotesAppSizes.standardIcon),
                    strokeWidth = NotesAppSizes.selectedOutlineWidth,
                )
            }
            Text(
                text = message,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TasksVoiceStatusPanelPreview() {
    NotesAppTheme {
        TasksVoiceStatusPanel(
            state = TasksVoiceInputUiState.GigaChatProcessing,
            modifier = Modifier.padding(NotesAppSpacing.space4),
        )
    }
}

private const val SECONDS_PER_MINUTE = 60
