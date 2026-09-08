package dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.core.designsystem.theme.notesAppNeutralBackgroundColor
import dev.toothlonely.notesapp.core.domain.model.ThemeMode
import dev.toothlonely.notesapp.feature.settings.impl.R

@Composable
fun ThemeModeOption(
    themeMode: ThemeMode,
    selected: Boolean,
    enabled: Boolean,
    useFixedPreviewHeight: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val label = stringResource(themeMode.labelResource)
    val cardShape = MaterialTheme.shapes.large
    val previewShape = MaterialTheme.shapes.extraSmall
    val lightBackground = notesAppNeutralBackgroundColor(useDarkColors = false)
    val darkBackground = notesAppNeutralBackgroundColor(useDarkColors = true)
    val previewBrush = when (themeMode) {
        ThemeMode.System -> Brush.verticalGradient(listOf(lightBackground, darkBackground))
        ThemeMode.Light -> Brush.verticalGradient(listOf(lightBackground, lightBackground))
        ThemeMode.Dark -> Brush.verticalGradient(listOf(darkBackground, darkBackground))
    }
    val previewModifier = if (useFixedPreviewHeight) {
        Modifier
            .fillMaxWidth()
            .height(NotesAppSizes.themePreviewHeight)
    } else {
        Modifier
            .fillMaxWidth()
            .aspectRatio(THEME_PREVIEW_ASPECT_RATIO)
    }
    Column(
        modifier = modifier
            .alpha(if (enabled) ENABLED_CONTENT_ALPHA else DISABLED_CONTENT_ALPHA)
            .clip(cardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                width = if (selected) {
                    NotesAppSizes.selectedOutlineWidth
                } else {
                    NotesAppSizes.outlineWidth
                },
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                shape = cardShape,
            )
            .selectable(
                selected = selected,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .semantics(mergeDescendants = true) {}
            .padding(NotesAppSpacing.space2),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = previewModifier
                .clip(previewShape)
                .background(previewBrush)
                .border(
                    width = NotesAppSizes.outlineWidth,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = previewShape,
                ),
        )
        Text(
            text = label,
            modifier = Modifier.padding(top = NotesAppSpacing.space2),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ThemeModeOptionPreview() {
    NotesAppTheme {
        ThemeModeOption(
            themeMode = ThemeMode.System,
            selected = true,
            enabled = true,
            useFixedPreviewHeight = true,
            onClick = {},
            modifier = Modifier.padding(NotesAppSpacing.space4),
        )
    }
}

private val ThemeMode.labelResource: Int
    get() = when (this) {
        ThemeMode.System -> R.string.settings_theme_system
        ThemeMode.Light -> R.string.settings_theme_light
        ThemeMode.Dark -> R.string.settings_theme_dark
    }

private const val THEME_PREVIEW_ASPECT_RATIO = 1.4f
private const val ENABLED_CONTENT_ALPHA = 1f
private const val DISABLED_CONTENT_ALPHA = 0.38f
