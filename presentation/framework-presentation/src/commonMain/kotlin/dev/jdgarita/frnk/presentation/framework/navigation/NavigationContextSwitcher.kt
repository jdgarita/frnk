package dev.jdgarita.frnk.presentation.framework.navigation

import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
import kotlinx.coroutines.flow.SharedFlow

/**
 * A [NavigationContextSwitcher] is responsible for switching between different navigation contexts.
 *
 * A navigation context is a set of screens that have a single main [NavigationRouter], so there
 * is a one-to-one relation between a [NavigationContext] and a parent [NavigationRouter].
 * For example, a navigation context could be the authentication flow, or a tab in the main screen.
 *
 */
interface NavigationContextSwitcher {

    val eventFlow: SharedFlow<NavigationContextSwitchEvent>

    val activeContext: NavigationContext?

    var deeplinkUri: String?

    val authorizedFlowStartEvent: ExternalEvent

    /**
     * Sets the initial [NavigationContext].
     */
    fun start(navigationContext: NavigationContext)

    /**
     * Returns true if the [NavigationContext] is started.
     */
    fun isContextStarted(externalEvent: ExternalEvent): Boolean

    /**
     * Switches to the navigation context that can handle the [ExternalEvent].
     * @param finishOtherContexts If true, all other contexts will be finished if navigating to root context.
     */
    fun switchContext(externalEvent: ExternalEvent, finishOtherContexts: Boolean = false)

    /**
     * Switches to the navigation context.
     */
    fun switchContext(navigationContext: NavigationContext)

    /**
     * Finishes the navigation context.
     *
     * This shouldn't be called directly, instead, it should be called from the [NavigationRouter]
     * @param shouldTriggerContextChange Flag to control if the context change should be triggered automatically.
     */
    fun finishContext(navigationContext: NavigationContext, shouldTriggerContextChange: Boolean = true)

    /**
     * Finishes all navigation contexts. Call when the app is being closed.
     */
    fun finishAllContexts()

    /**
     * Returns the [NavigationRouter] for the [NavigationContext].
     */
    fun getNavigationRouter(navigationContext: NavigationContext): NavigationRouter

    /**
     * Returns the default start event for the [NavigationContext] that can handle given [externalEvent].
     */
    fun getDefaultStartEvent(externalEvent: ExternalEvent): ExternalEvent?

    /**
     * Registers a [NavigationContextHandler].
     */
    fun registerNavigationContextHandler(navigationContextHandler: NavigationContextHandler) = Unit
}