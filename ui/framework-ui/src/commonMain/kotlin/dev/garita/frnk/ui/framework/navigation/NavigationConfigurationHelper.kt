package dev.garita.frnk.ui.framework.navigation

import dev.jdgarita.frnk.presentation.framework.navigation.NavigationConfiguration
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationContext
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationContextSwitchEvent
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationDestination
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent

/**
 * Helper class that provides util methods to easily mange [NavigationConfiguration] and [NavigationContextConfig]s.
 */
class NavigationConfigurationHelper(
    private val navigationConfiguration: NavigationConfiguration,
    navigationContextConfigs: Set<NavigationContextConfig>
) {

    private val navigationContextConfigMap: Map<NavigationContext, NavigationContextConfig> =
        navigationConfiguration.bottomTabNavigationContexts.plus(navigationConfiguration.navigationContexts)
            .associateWith { context -> navigationContextConfigs.first { it.navigationContext == context } }

    /**
     * Returns all [NavigationContext]s that are configured in the [NavigationConfiguration].
     */
    val navigationContexts: Set<NavigationContext>
        get() = navigationConfiguration.bottomTabNavigationContexts.plus(navigationConfiguration.navigationContexts)

    /**
     * Returns [NavigationContextConfig]s for [NavigationContext]s that are configured as bottom tabs.
     */
    val tabNavigationContextConfigs: List<TabNavigationContextConfig>
        get() = navigationConfiguration.bottomTabNavigationContexts.map {
            navigationContextConfigMap.getValue(it) as TabNavigationContextConfig
        }

    /**
     * Returns [NavigationContextConfig]s for [NavigationContext]s that are *not* configured as bottom tabs.
     */
    val rootNavigationContextConfigs: List<NavigationContextConfig>
        get() = navigationConfiguration.navigationContexts.map { navigationContextConfigMap.getValue(it) }

    fun getFirstTabRoute() = if (navigationConfiguration.bottomTabNavigationContexts.isEmpty()) {
        TABS_DESTINATION
    } else {
        val firstTabRoute = navigationConfiguration.bottomTabNavigationContexts.first()
        navigationContextConfigMap[firstTabRoute]?.startDestination?.route ?: TABS_DESTINATION
    }

    /**
     * Returns the global start destination.
     */
    fun getStartDestination(): String {
        val navigationContext = navigationConfiguration.startNavigationContext

        return if (isBottomTabContext(navigationContext)) {
            TABS_DESTINATION
        } else {
            getStartDestination(navigationContext)
                ?.let {
                    createRootRoute(it.route)
                } ?: TABS_DESTINATION
        }
    }

    /**
     * Returns the start destination for the given [NavigationContext].
     */
    fun getStartDestination(navigationContext: NavigationContext): NavigationDestination? =
        navigationContextConfigMap[navigationContext]?.startDestination

    /**
     * Returns the [NavigationContextConfig] for the given [NavigationContextSwitchEvent].
     */
    fun getNavigationContextConfig(navigationSwitchEvent: NavigationContextSwitchEvent): NavigationContextConfig? =
        navigationContextConfigMap[navigationSwitchEvent.navigationContext]

    /**
     * Returns the [NavigationContextConfig] for the given [NavigationContext].
     */
    fun getNavigationContextConfig(navigationContext: NavigationContext): NavigationContextConfig? =
        navigationContextConfigMap[navigationContext]

    /**
     * Returns the [NavigationDestination] for the given [ExternalEvent].
     */
    fun getDestination(contextConfig: NavigationContextConfig, externalEvent: ExternalEvent): NavigationDestination? =
        contextConfig.getDestination(externalEvent)

    /**
     * Returns true if the given [NavigationContext] is configured as a bottom tab.
     */
    fun isBottomTabContext(navigationContext: NavigationContext): Boolean =
        navigationConfiguration.bottomTabNavigationContexts.contains(navigationContext)

    /**
     * Returns the tab route for the given tab [NavigationContext]. Make sure to pass
     * context that is configured as a bottom tab.
     */
    fun getTabContextRoute(navigationContext: NavigationContext): String {
        val config = navigationContextConfigMap[navigationContext]
        require(config is TabNavigationContextConfig) {
            "NavigationContext $navigationContext is not configured as a bottom tab."
        }

        return config.route
    }
}