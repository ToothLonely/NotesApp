package dev.toothlonely.notesapp.ui

import androidx.compose.foundation.background
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.CompositionLocalProvider
import dev.toothlonely.notesapp.feature.notes.impl.navigation.LocalNoteTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSizes
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppSpacing
import dev.toothlonely.notesapp.core.designsystem.theme.NotesAppTheme
import dev.toothlonely.notesapp.feature.notes.api.NoteEditorRoute
import dev.toothlonely.notesapp.feature.notes.impl.navigation.NotesDestinationState
import dev.toothlonely.notesapp.feature.notes.impl.navigation.notesEntries
import dev.toothlonely.notesapp.feature.settings.impl.navigation.settingsEntries
import dev.toothlonely.notesapp.feature.tasks.impl.navigation.tasksEntries
import dev.toothlonely.notesapp.navigation.AppDestination
import dev.toothlonely.notesapp.navigation.rememberAppNavigationState
import dev.toothlonely.notesapp.navigation.toEntries
import dev.toothlonely.notesapp.ui.components.NotesBottomNavigationBar
import dev.toothlonely.notesapp.ui.components.AnimatedSplashScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NotesApp(
    appThemeViewModel: AppThemeViewModel = koinViewModel(),
) {
    val userPreferences by appThemeViewModel.state.collectAsStateWithLifecycle()
    NotesAppTheme(
        themeMode = userPreferences.themeMode.toDesignSystemThemeMode(),
        accentPalette = userPreferences.accentPreset.toDesignSystemAccentPalette(),
    ) {
        var isSplashVisible by rememberSaveable { mutableStateOf(true) }
        val navigationState = rememberAppNavigationState()
        val notesDestinationState = remember { NotesDestinationState() }
        val density = LocalDensity.current
        val navigationBarBottomInset = with(density) {
            WindowInsets.navigationBars.getBottom(this).toDp()
        }
        val bottomNavigationPadding = PaddingValues(
            bottom = calculateBottomNavigationClearance(navigationBarBottomInset),
        )
        val entryProvider = entryProvider {
            notesEntries(
                bottomNavigationPadding = bottomNavigationPadding,
                destinationState = notesDestinationState,
                onCreateNote = { navigationState.navigate(NoteEditorRoute()) },
                onOpenNote = { noteId -> navigationState.navigate(NoteEditorRoute(noteId)) },
                onCloseEditor = navigationState::goBack,
            )
            tasksEntries(bottomNavigationPadding = bottomNavigationPadding)
            settingsEntries(bottomNavigationPadding = bottomNavigationPadding)
        }
        val showBottomNavigation = navigationState.shouldShowBottomNavigation

        Box(modifier = Modifier.fillMaxSize()) {
            SharedTransitionLayout(modifier = Modifier.fillMaxSize()) {
                CompositionLocalProvider(LocalNoteTransitionScope provides this) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background),
                    ) {
                        NavDisplay(
                            entries = navigationState.toEntries(entryProvider),
                            onBack = navigationState::goBack,
                            modifier = Modifier.fillMaxSize(),
                        )

                        if (showBottomNavigation) {
                            NotesBottomNavigationBar(
                                selectedDestination = navigationState.selectedDestination,
                                onSelectDestination = { destination ->
                                    navigationState.selectDestination(destination) { deactivatedDestination ->
                                        if (deactivatedDestination == AppDestination.Notes) {
                                            notesDestinationState.deactivate()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(
                                        horizontal = NotesAppSizes
                                            .floatingBottomNavigationHorizontalMargin,
                                    )
                                    .windowInsetsPadding(
                                        WindowInsets.navigationBars.only(WindowInsetsSides.Bottom),
                                    )
                                    .padding(
                                        bottom = NotesAppSizes.floatingBottomNavigationBottomGap,
                                    ),
                            )
                        }
                    }
                }
            }

            if (isSplashVisible) {
                AnimatedSplashScreen(
                    onAnimationFinished = { isSplashVisible = false },
                )
            }
        }
    }
}

internal fun calculateBottomNavigationClearance(
    navigationBarBottomInset: Dp,
): Dp = navigationBarBottomInset +
    NotesAppSizes.floatingBottomNavigationBottomGap +
    NotesAppSizes.floatingBottomNavigationHeight +
    NotesAppSpacing.space4
