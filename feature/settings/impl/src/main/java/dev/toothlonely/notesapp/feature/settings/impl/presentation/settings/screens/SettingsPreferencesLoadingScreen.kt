package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.settings.impl.R

@Composable
fun SettingsPreferencesLoadingScreen(
    modifier: Modifier = Modifier,
) {
    val loadingDescription = stringResource(R.string.settings_loading)
    Column(
        modifier = modifier.semantics {
            liveRegion = LiveRegionMode.Polite
            contentDescription = loadingDescription
        },
    ) {
        Text(
            text = stringResource(R.string.settings_accent_section_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NotesAppSpacing.space3),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            repeat(ACCENT_PRESET_COUNT) {
                Surface(
                    modifier = Modifier.size(NotesAppSizes.paletteSwatch),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = NotesAppShapes.full,
                ) {}
            }
        }
        Text(
            text = stringResource(R.string.settings_theme_section_title),
            modifier = Modifier.padding(top = NotesAppSpacing.space6),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NotesAppSpacing.space3),
            horizontalArrangement = Arrangement.spacedBy(NotesAppSpacing.space2),
        ) {
            repeat(THEME_MODE_COUNT) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(NotesAppSizes.themePreviewHeight),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.large,
                ) {}
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreferencesLoadingScreenPreview() {
    NotesAppTheme {
        SettingsPreferencesLoadingScreen(modifier = Modifier.padding(NotesAppSpacing.space4))
    }
}

private const val ACCENT_PRESET_COUNT = 4
private const val THEME_MODE_COUNT = 3
