package dev.jdgarita.frnk.sdk

import com.swiftly.platform.framework.config.AppConfiguration
import com.swiftly.platform.framework.config.InternalConfiguration
import com.swiftly.platform.framework.ui.navigation.NavigationConfiguration

/**
 * The Swiftly SDK.
 */
interface FrnkSdk {

    /**
     * Initialize the SDK with the given [appConfiguration] and [navigationConfiguration].
     * It will initialize Koin with all the default modules. Optionally, additional Koin [modules] can be loaded.
     */
    fun initialize(
        appConfiguration: AppConfiguration,
        internalConfiguration: InternalConfiguration,
        navigationConfiguration: NavigationConfiguration
    ): FrnkSdk
}