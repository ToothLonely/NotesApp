package dev.toothlonely.notesapp.navigation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

class AppNavigationState internal constructor(
    val startRoute: NavKey,
    selectedRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>,
) {
    var selectedRoute: NavKey by selectedRoute
        private set

    val selectedDestination: AppDestination
        get() = AppDestination.fromRoute(selectedRoute)

    val currentBackStack: NavBackStack<NavKey>
        get() = checkNotNull(backStacks[selectedRoute]) {
            "No back stack registered for $selectedRoute"
        }

    val currentRoute: NavKey
        get() = currentBackStack.last()

    val stacksInUse: List<NavKey>
        get() = listOf(selectedRoute)

    val shouldShowBottomNavigation: Boolean
        get() = currentRoute in backStacks.keys

    fun selectDestination(
        destination: AppDestination,
        onDestinationDeactivated: (AppDestination) -> Unit = {},
    ) {
        val route = destination.route
        require(route in backStacks) { "No back stack registered for $route" }

        if (route == selectedRoute) {
            currentBackStack.popToRoot()
        } else {
            onDestinationDeactivated(selectedDestination)
            selectedRoute = route
        }
    }

    fun navigate(route: NavKey) {
        currentBackStack.add(route)
    }

    fun goBack() {
        if (currentBackStack.size > 1) {
            currentBackStack.removeLastOrNull()
        }
    }

    private fun NavBackStack<NavKey>.popToRoot() {
        while (size > 1) {
            removeLastOrNull()
        }
    }
}
