package dev.toothlonely.notesapp.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import dev.toothlonely.notesapp.R
import dev.toothlonely.notesapp.core.designsystem.R as DesignSystemR
import dev.toothlonely.notesapp.feature.notes.api.NotesRoute
import dev.toothlonely.notesapp.feature.settings.api.SettingsRoute
import dev.toothlonely.notesapp.feature.tasks.api.TasksRoute

enum class AppDestination(
    val route: NavKey,
    @param:StringRes val labelResource: Int,
    @param:DrawableRes val iconResource: Int,
    @param:DrawableRes val selectedIconResource: Int,
) {
    Notes(
        route = NotesRoute,
        labelResource = R.string.navigation_notes,
        iconResource = DesignSystemR.drawable.ic_note_24,
        selectedIconResource = DesignSystemR.drawable.ic_note_filled_24,
    ),
    Tasks(
        route = TasksRoute,
        labelResource = R.string.navigation_tasks,
        iconResource = DesignSystemR.drawable.ic_checkbox_24,
        selectedIconResource = DesignSystemR.drawable.ic_checkbox_filled_24,
    ),
    Settings(
        route = SettingsRoute,
        labelResource = R.string.navigation_settings,
        iconResource = DesignSystemR.drawable.ic_settings_24,
        selectedIconResource = DesignSystemR.drawable.ic_settings_filled_24,
    ),
    ;

    companion object {
        fun fromRoute(route: NavKey): AppDestination = entries.first { destination ->
            destination.route == route
        }
    }
}
