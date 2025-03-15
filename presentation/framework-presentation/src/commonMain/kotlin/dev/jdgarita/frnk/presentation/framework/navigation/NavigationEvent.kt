package dev.jdgarita.frnk.presentation.framework.navigation

/**
 * This class contains all the necessary information to perform a navigation
 * [screen] - The screen the navigation is being performed on
 * [navigationType] - The type of navigation. @see [NavigationType]
 */
data class NavigationEvent(
    val screen: ScreenEntry,
    val navigationType: NavigationType
)

/**
 * A structure to hold information about where to navigate to
 */
data class ScreenEntry(
    val id: String,
    val screen: NavigationScreen
) {
    fun matchesName(screen: NavigationScreen): Boolean = this.screen.screenName == screen.screenName
}

fun ScreenEntry?.matchesName(screen: NavigationScreen): Boolean = this?.matchesName(screen) ?: false

/**
 * Contains the possible different UI elements to display
 */
enum class NavigationType {
    FULL_SCREEN,
    DISMISS,
    CLEAR_STACK
}