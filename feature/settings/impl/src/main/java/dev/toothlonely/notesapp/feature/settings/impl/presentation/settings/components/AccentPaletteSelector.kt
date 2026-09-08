package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import dev.toothlonely.notesapp.core.domain.model.AccentPreset
import dev.toothlonely.notesapp.feature.settings.impl.R

@Composable
fun AccentPaletteSelector(
    selectedAccentPreset: AccentPreset,
    useDarkColors: Boolean,
    isSaving: Boolean,
    enabled: Boolean,
    onAccentPresetSelected: (AccentPreset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val labels = mapOf(
        AccentPreset.Indigo to stringResource(R.string.settings_accent_indigo),
        AccentPreset.Teal to stringResource(R.string.settings_accent_teal),
        AccentPreset.Raspberry to stringResource(R.string.settings_accent_raspberry),
        AccentPreset.Amber to stringResource(R.string.settings_accent_amber),
    )
    val useTwoRows = LocalDensity.current.fontScale >= TWO_ROW_FONT_SCALE
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.settings_accent_section_title),
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
            val presets = AccentPreset.entries
            if (useTwoRows || maxWidth < FOUR_COLUMN_SELECTOR_MINIMUM_WIDTH) {
                Column {
                    presets.chunked(PRESETS_PER_ADAPTIVE_ROW).forEachIndexed { index, rowPresets ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    top = if (index == 0) 0.dp else NotesAppSpacing.space3,
                                ),
                        ) {
                            rowPresets.forEach { accentPreset ->
                                AccentPaletteOption(
                                    accentPreset = accentPreset,
                                    label = labels.getValue(accentPreset),
                                    selected = accentPreset == selectedAccentPreset,
                                    useDarkColors = useDarkColors,
                                    enabled = enabled,
                                    onClick = { onAccentPresetSelected(accentPreset) },
                                    modifier = Modifier.weight(1f),
                                )
                            }
                        }
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth()) {
                    presets.forEach { accentPreset ->
                        AccentPaletteOption(
                            accentPreset = accentPreset,
                            label = labels.getValue(accentPreset),
                            selected = accentPreset == selectedAccentPreset,
                            useDarkColors = useDarkColors,
                            enabled = enabled,
                            onClick = { onAccentPresetSelected(accentPreset) },
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
private fun AccentPaletteSelectorPreview() {
    NotesAppTheme {
        AccentPaletteSelector(
            selectedAccentPreset = AccentPreset.Indigo,
            useDarkColors = false,
            isSaving = false,
            enabled = true,
            onAccentPresetSelected = {},
            modifier = Modifier.padding(NotesAppSpacing.space4),
        )
    }
}

private val FOUR_COLUMN_SELECTOR_MINIMUM_WIDTH = 320.dp
private const val TWO_ROW_FONT_SCALE = 1.3f
private const val PRESETS_PER_ADAPTIVE_ROW = 2
