package dev.jdgarita.frnk.sdk

import dev.jdgarita.frnk.domain.config.AppConfiguration
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationConfiguration
/**
 * The Frnk SDK.
 */
interface FrnkSdk {

    /**
     * Initialize the SDK with the given [appConfiguration] and [navigationConfiguration].
     * It will initialize Koin with all the default modules. Optionally, additional Koin [modules] can be loaded.
     */
    fun initialize(
        appConfiguration: AppConfiguration,
        navigationConfiguration: NavigationConfiguration
    ): FrnkSdk
}