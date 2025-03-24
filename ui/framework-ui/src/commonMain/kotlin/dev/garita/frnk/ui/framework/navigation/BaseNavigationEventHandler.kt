package dev.garita.frnk.ui.framework.navigation

import androidx.navigation.NavController
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationEvent
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationType
import dev.jdgarita.frnk.presentation.framework.navigation.ScreenEntry

abstract class BaseNavigationEventHandler : NavigationEventHandler {

    /**
     * The route that this handler can handle.
     */
    abstract val route: String

    override fun handle(event: NavigationEvent, navController: NavController): Boolean {
        if (!canHandle(event)) return false

        return when (event.navigationType) {
            NavigationType.FULL_SCREEN -> handleFullScreen(event.screen, navController)
            NavigationType.DISMISS -> handleDismiss(navController)
            NavigationType.CLEAR_STACK -> handleClearStack(navController)
        }
    }

    /**
     * Returns true if this handler can handle the given [NavigationEvent].
     */
    abstract fun canHandle(event: NavigationEvent): Boolean

    /**
     * Handles a [NavigationType.FULL_SCREEN].
     */
    abstract fun handleFullScreen(screenEntry: ScreenEntry, navController: NavController): Boolean

    /**
     * Handles a [NavigationType.DISMISS].
     */
    protected open fun handleDismiss(navController: NavController): Boolean {
        navController.popBackStack()
        return true
    }

    /**
     * Handles a [NavigationType.CLEAR_STACK].
     */
    protected open fun handleClearStack(navController: NavController): Boolean {
        navController.popBackStack(route, inclusive = true)
        return true
    }
}