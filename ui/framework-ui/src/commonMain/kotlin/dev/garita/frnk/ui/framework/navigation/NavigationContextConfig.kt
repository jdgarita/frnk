package dev.garita.frnk.ui.framework.navigation

import androidx.navigation.NavGraphBuilder
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationContext
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationDestination
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationRouter
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
import dev.jdgarita.frnk.tabConfig.TabConfig

/**
 * Configuration of [NavigationContext].
 */
interface NavigationContextConfig {

    /**
     * [NavigationContext] that represents the configuration.
     */
    val navigationContext: NavigationContext

    /**
     * Returns [NavigationDestination] for the given [ExternalEvent].
     */
    fun getDestination(externalEvent: ExternalEvent): NavigationDestination?

    /**
     * Start event of the [NavigationContext].
     */
    val defaultStartEvent: ExternalEvent

    /**
     * Start destination of the [NavigationContext].
     */
    val startDestination: NavigationDestination
        get() = requireNotNull(getDestination(defaultStartEvent)) {
            "StartDestination not found for $defaultStartEvent"
        }

    /**
     * Adds navigation to the given [NavGraphBuilder] for the [NavigationContext].
     */
    fun addNavigation(
        navGraphBuilder: NavGraphBuilder,
        navigationRouter: NavigationRouter,
        startDestination: String
    )
}

/**
 * Configuration of [NavigationContext] that represents a tab.
 */
interface TabNavigationContextConfig : NavigationContextConfig {

    /**
     * [TabConfig] associated to the tab.
     */
    val tabConfig: TabConfig

    /**
     * Route of the tab.
     *
     * We may want to deeplink deeper into the tab to destination that
     * does not know that it's embedded in a tab. This is where this route comes in.
     * It tells the framework to create a route that is a combination of the tab
     * route and the destination route.
     */
    val route: String

    /**
     * Navigation context.
     */
    override val navigationContext: NavigationContext
        get() = tabConfig.navigationContext
}