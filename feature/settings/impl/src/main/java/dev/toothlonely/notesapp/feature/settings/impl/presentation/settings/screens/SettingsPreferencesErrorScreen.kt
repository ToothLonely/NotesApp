package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.settings.impl.R

@Composable
fun SettingsPreferencesErrorScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics { liveRegion = LiveRegionMode.Assertive },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(DesignSystemR.drawable.ic_mood_bad_24),
            contentDescription = null,
            modifier = Modifier.size(NotesAppSizes.emptyStateIcon),
            tint = MaterialTheme.colorScheme.error,
        )
        Text(
            text = stringResource(R.string.settings_load_error),
            modifier = Modifier.padding(top = NotesAppSpacing.space4),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
        OutlinedButton(
            onClick = onRetry,
            modifier = Modifier.padding(top = NotesAppSpacing.space4),
        ) {
            Text(text = stringResource(R.string.settings_retry))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreferencesErrorScreenPreview() {
    NotesAppTheme {
        SettingsPreferencesErrorScreen(
            onRetry = {},
            modifier = Modifier.padding(NotesAppSpacing.space4),
        )
    }
}
