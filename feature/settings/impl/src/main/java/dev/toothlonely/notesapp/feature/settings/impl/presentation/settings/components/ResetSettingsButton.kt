package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.settings.impl.R

@Composable
fun ResetSettingsButton(
    isResetting: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = NotesAppShapes.floating,
        border = BorderStroke(
            width = NotesAppSizes.outlineWidth,
            color = if (enabled) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
    ) {
        if (isResetting) {
            CircularProgressIndicator(
                modifier = Modifier.size(NotesAppSizes.standardIcon),
                color = MaterialTheme.colorScheme.error,
                strokeWidth = NotesAppSizes.outlineWidth,
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_reset_settings_24),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = NotesAppSpacing.space2)
                    .size(NotesAppSizes.standardIcon),
            )
            Text(text = stringResource(R.string.settings_reset))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ResetSettingsButtonPreview() {
    NotesAppTheme {
        ResetSettingsButton(
            isResetting = false,
            enabled = true,
            onClick = {},
        )
    }
}
