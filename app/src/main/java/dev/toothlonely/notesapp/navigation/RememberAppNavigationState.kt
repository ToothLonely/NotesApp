package dev.toothlonely.notesapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer

@Composable
fun rememberAppNavigationState(
    startDestination: AppDestination = AppDestination.Notes,
): AppNavigationState {
    val destinations = AppDestination.entries
    val topLevelRoutes = destinations.map(AppDestination::route).toSet()
    val selectedRoute = rememberSerializable(
        startDestination,
        topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer()),
    ) {
        mutableStateOf(startDestination.route)
    }
    val backStacks = destinations.associate { destination ->
        destination.route to rememberNavBackStack(destination.route)
    }

    return remember(startDestination, selectedRoute, backStacks) {
        AppNavigationState(
            startRoute = startDestination.route,
            selectedRoute = selectedRoute,
            backStacks = backStacks,
        )
    }
}
