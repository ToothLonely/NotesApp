package dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.components.action

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.impl.R
import dev.toothlonely.notesapp.feature.notes.impl.presentation.editor.NoteVoiceInputUiState

@Composable
fun EditorVoiceInputFab(
    state: NoteVoiceInputUiState,
    enabled: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRecording = state is NoteVoiceInputUiState.Recording
    val isProcessing = state is NoteVoiceInputUiState.Processing
    val isActionEnabled = enabled || isRecording
    FloatingActionButton(
        onClick = {
            when {
                isRecording -> onStop()
                enabled -> onStart()
            }
        },
        modifier = modifier
            .size(NotesAppSizes.fab)
            .semantics {
                if (!isActionEnabled) disabled()
            },
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        if (isProcessing) {
            CircularProgressIndicator(
                modifier = Modifier.size(NotesAppSizes.standardIcon),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = NotesAppSizes.selectedOutlineWidth,
            )
        } else {
            Icon(
                painter = painterResource(
                    if (isRecording) DesignSystemR.drawable.ic_stop_24
                    else DesignSystemR.drawable.ic_mic_24,
                ),
                contentDescription = stringResource(
                    when {
                        isRecording -> R.string.note_voice_stop
                        !enabled -> R.string.note_voice_processing
                        else -> R.string.note_voice_start
                    },
                ),
                tint = if (isActionEnabled) {
                    MaterialTheme.colorScheme.onPrimary
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditorVoiceInputFabPreview() {
    NotesAppTheme {
        EditorVoiceInputFab(
            state = NoteVoiceInputUiState.Idle,
            enabled = true,
            onStart = {},
            onStop = {},
        )
    }
}
