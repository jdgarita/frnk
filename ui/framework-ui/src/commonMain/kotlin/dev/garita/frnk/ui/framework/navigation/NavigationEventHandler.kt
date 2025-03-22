package dev.garita.frnk.ui.framework.navigation

import androidx.navigation.NavController
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationEvent

/**
 * Interface for handling navigation events.
 */
interface NavigationEventHandler {

    /**
     * Handles the given [NavigationEvent].
     *
     * @return true if the event was handled, false otherwise.
     */
    fun handle(event: NavigationEvent, navController: NavController): Boolean
}