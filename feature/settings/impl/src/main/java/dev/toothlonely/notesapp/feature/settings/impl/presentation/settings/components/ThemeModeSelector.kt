package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.feature.settings.impl.R

@Composable
fun ThemeModeSelector(
    selectedThemeMode: ThemeMode,
    isSaving: Boolean,
    enabled: Boolean,
    onThemeModeSelected: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val useVerticalLayout = LocalDensity.current.fontScale >= VERTICAL_LAYOUT_FONT_SCALE
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.settings_theme_section_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(NotesAppSizes.standardIcon),
                    strokeWidth = NotesAppSizes.outlineWidth,
                )
            }
        }
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = NotesAppSpacing.space3),
        ) {
            if (useVerticalLayout || maxWidth < HORIZONTAL_SELECTOR_MINIMUM_WIDTH) {
                Column(
                    modifier = Modifier.selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(NotesAppSpacing.space3),
                ) {
                    themeModesInDisplayOrder.forEach { themeMode ->
                        ThemeModeOption(
                            themeMode = themeMode,
                            selected = themeMode == selectedThemeMode,
                            enabled = enabled,
                            useFixedPreviewHeight = true,
                            onClick = { onThemeModeSelected(themeMode) },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectableGroup(),
                    horizontalArrangement = Arrangement.spacedBy(NotesAppSpacing.space2),
                ) {
                    themeModesInDisplayOrder.forEach { themeMode ->
                        ThemeModeOption(
                            themeMode = themeMode,
                            selected = themeMode == selectedThemeMode,
                            enabled = enabled,
                            useFixedPreviewHeight = false,
                            onClick = { onThemeModeSelected(themeMode) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeModeSelectorPreview() {
    NotesAppTheme {
        ThemeModeSelector(
            selectedThemeMode = ThemeMode.System,
            isSaving = false,
            enabled = true,
            onThemeModeSelected = {},
            modifier = Modifier.padding(NotesAppSpacing.space4),
        )
    }
}

internal val themeModesInDisplayOrder = listOf(
    ThemeMode.System,
    ThemeMode.Light,
    ThemeMode.Dark,
)

private val HORIZONTAL_SELECTOR_MINIMUM_WIDTH = 320.dp
private const val VERTICAL_LAYOUT_FONT_SCALE = 1.3f
