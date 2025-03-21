package dev.jdgarita.frnk.presentation.framework.navigation

import dev.jdgarita.frnk.presentation.mvi.ExternalEvent

/**
 * A [NavigationContextHandler] is responsible for handling a specific [NavigationContext].
 */
interface NavigationContextHandler {

    /**
     * The [NavigationContext] this handler is responsible for.
     */
    val navigationContext: NavigationContext

    /**
     * The [ExternalEvent] that starts the [NavigationContext].
     */
    val startEvent: ExternalEvent

    /**
     * A [NavigationRouter] provider for the [NavigationContext].
     */
    val navigationRouterProvider: () -> NavigationRouter

    /**
     * Returns true if this handler can handle the [ExternalEvent].
     */
    fun canHandle(externalEvent: ExternalEvent): Boolean
}