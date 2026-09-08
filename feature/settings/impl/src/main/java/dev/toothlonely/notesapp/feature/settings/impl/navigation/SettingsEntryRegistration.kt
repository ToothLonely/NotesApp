package dev.toothlonely.notesapp.feature.settings.impl.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.toothlonely.notesapp.feature.settings.api.SettingsRoute
import dev.toothlonely.notesapp.feature.settings.impl.presentation.settings.SettingsRoute as SettingsRouteContent

fun EntryProviderScope<NavKey>.settingsEntries(
    bottomNavigationPadding: PaddingValues,
) {
    entry<SettingsRoute> {
        SettingsRouteContent(bottomNavigationPadding = bottomNavigationPadding)
    }
}
