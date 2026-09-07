package dev.toothlonely.notesapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppShapes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.navigation.AppDestination

@Composable
fun NotesBottomNavigationBar(
    selectedDestination: AppDestination,
    onSelectDestination: (AppDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(NotesAppSizes.floatingBottomNavigationHeight),
        shape = NotesAppShapes.floatingNavigation,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(
            width = NotesAppSizes.outlineWidth,
            color = MaterialTheme.colorScheme.outlineVariant,
        ),
        shadowElevation = NotesAppSizes.floatingBottomNavigationElevation,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppDestination.entries.forEach { destination ->
                val selected = destination == selectedDestination
                val interactionSource = remember(destination) {
                    MutableInteractionSource()
                }
                val contentColor = if (selected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .selectable(
                            selected = selected,
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onSelectDestination(destination) },
                            role = Role.Tab,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier
                            .widthIn(
                                min = NotesAppSizes.minimumTouchTarget,
                                max = NotesAppSizes
                                    .floatingBottomNavigationSelectedItemMaximumWidth,
                            )
                            .fillMaxWidth()
                            .height(
                                NotesAppSizes.floatingBottomNavigationSelectedItemHeight,
                            )
                            .clip(NotesAppShapes.full)
                            .background(
                                if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                },
                            ),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            painter = painterResource(
                                if (selected) {
                                    destination.selectedIconResource
                                } else {
                                    destination.iconResource
                                },
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(NotesAppSizes.standardIcon),
                            tint = contentColor,
                        )
                        Spacer(modifier = Modifier.height(NotesAppSpacing.space1))
                        Text(
                            text = stringResource(destination.labelResource),
                            color = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                contentColor
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotesBottomNavigationBarPreview() {
    NotesAppTheme {
        NotesBottomNavigationBar(
            selectedDestination = AppDestination.Notes,
            onSelectDestination = {},
        )
    }
}
