package dev.jdgarita.frnk.presentation.framework.navigation

/**
 * Configuration for the navigation system. Every app has to provide a [NavigationConfiguration] based on
 * which the navigation structure will be built.
 *
 * @param startNavigationContext The [NavigationContext] that is used to start the app.
 * @param bottomTabNavigationContexts The [NavigationContext]s that are used for bottom tab navigation.
 * @param navigationContexts The [NavigationContext]s that are used outside of bottom tab
 * @param bottomTabResetPolicy The reset policy for the bottom tab.
 */
data class NavigationConfiguration(
    val startNavigationContext: NavigationContext,
    val bottomTabNavigationContexts: Set<NavigationContext>,
    val navigationContexts: Set<NavigationContext>,
    val bottomTabResetPolicy: ResetPolicy = ResetPolicy.NONE
) {

    companion object {
        // This method is required to help iOS as we are not able to properly
        // initialize a set from there with NavigationContexts as they are not Hashable.
        fun getNavigationConfigurationForIos(
            bottomTabNavigationContexts: List<NavigationContext>,
            bottomTabResetPolicy: ResetPolicy
        ): NavigationConfiguration = NavigationConfiguration(
            object : NavigationContext {},
            bottomTabNavigationContexts.toSet(),
            emptySet(),
            bottomTabResetPolicy
        )
    }

    /**
     * Defines the reset policy for the bottom tab.
     */
    enum class ResetPolicy {
        /**
         * Default behaviour. No reset will be performed. Only tapping on the active tab
         * will reset the navigation stack.
         */
        NONE,

        /**
         * The first tab will be always reset.
         */
        FIRST_BOTTOM_TAB,

        /**
         * All tabs will always be reset.
         */
        BOTTOM_TAB
    }
}