package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppAccentPalette
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.core.designsystem.theme.notesAppAccentOnPrimaryColor
import dev.toothlonely.notesapp.core.designsystem.theme.notesAppAccentPrimaryColor
import dev.toothlonely.notesapp.core.domain.model.AccentPreset

@Composable
fun AccentPaletteOption(
    accentPreset: AccentPreset,
    label: String,
    selected: Boolean,
    useDarkColors: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val designSystemPalette = accentPreset.toDesignSystemPalette()
    val swatchColor = notesAppAccentPrimaryColor(designSystemPalette, useDarkColors)
    val onSwatchColor = notesAppAccentOnPrimaryColor(designSystemPalette, useDarkColors)
    Column(
        modifier = modifier
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {},
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(NotesAppSizes.paletteTouchTarget),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(NotesAppSizes.paletteSwatch + NotesAppSpacing.space2)
                        .border(
                            width = NotesAppSizes.outlineWidth,
                            color = MaterialTheme.colorScheme.primary,
                            shape = NotesAppShapes.full,
                        ),
                )
            }
            Box(
                modifier = Modifier
                    .size(NotesAppSizes.paletteSwatch)
                    .border(
                        width = NotesAppSizes.outlineWidth,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        shape = NotesAppShapes.full,
                    )
                    .background(swatchColor, NotesAppShapes.full),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_check_24),
                        contentDescription = null,
                        tint = onSwatchColor,
                    )
                }
            }
        }
        Text(
            text = label,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurface.copy(alpha = DISABLED_CONTENT_ALPHA)
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AccentPaletteOptionPreview() {
    NotesAppTheme {
        AccentPaletteOption(
            accentPreset = AccentPreset.Raspberry,
            label = "Малина",
            selected = true,
            useDarkColors = false,
            enabled = true,
            onClick = {},
            modifier = Modifier.padding(NotesAppSpacing.space4),
        )
    }
}

private fun AccentPreset.toDesignSystemPalette(): NotesAppAccentPalette = when (this) {
    AccentPreset.Indigo -> NotesAppAccentPalette.Indigo
    AccentPreset.Teal -> NotesAppAccentPalette.Teal
    AccentPreset.Raspberry -> NotesAppAccentPalette.Raspberry
    AccentPreset.Amber -> NotesAppAccentPalette.Amber
}

private const val DISABLED_CONTENT_ALPHA = 0.38f
