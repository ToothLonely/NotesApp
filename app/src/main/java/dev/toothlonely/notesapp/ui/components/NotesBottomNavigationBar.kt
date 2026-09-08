package dev.toothlonely.notesapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppMotion
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
    val indicatorPosition = animateFloatAsState(
        targetValue = AppDestination.entries.indexOf(selectedDestination).toFloat(),
        animationSpec = tween(NotesAppMotion.navigationIndicatorMillis),
        label = "navigationIndicatorPosition",
    )
    val indicatorColor = MaterialTheme.colorScheme.primaryContainer

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
                .drawBehind {
                    val itemWidth = size.width / AppDestination.entries.size
                    val indicatorWidth = minOf(
                        itemWidth,
                        NotesAppSizes.floatingBottomNavigationSelectedItemMaximumWidth.toPx(),
                    )
                    val indicatorHeight = minOf(
                        size.height,
                        NotesAppSizes.floatingBottomNavigationSelectedItemHeight.toPx(),
                    )
                    val start = itemWidth * indicatorPosition.value +
                        (itemWidth - indicatorWidth) / 2
                    val left = if (layoutDirection == LayoutDirection.Ltr) {
                        start
                    } else {
                        size.width - start - indicatorWidth
                    }
                    drawRoundRect(
                        color = indicatorColor,
                        topLeft = Offset(left, (size.height - indicatorHeight) / 2),
                        size = Size(indicatorWidth, indicatorHeight),
                        cornerRadius = CornerRadius(indicatorHeight / 2),
                    )
                }
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
