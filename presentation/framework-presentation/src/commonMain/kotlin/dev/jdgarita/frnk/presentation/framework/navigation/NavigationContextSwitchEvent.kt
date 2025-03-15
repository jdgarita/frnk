package dev.jdgarita.frnk.presentation.framework.navigation

import dev.jdgarita.frnk.presentation.mvi.ExternalEvent

/**
 * Class that represents a navigation context switch event.
 *
 * @param navigationContext The [NavigationContext] that is being switched to.
 * @param externalEvent The [ExternalEvent] that triggered the switch.
 * @param finishOtherContexts If true, all other contexts will be finished if navigating to root context.
 * It most likely needs to be passed to the [NavigationRouter] so it can handle it.
 */
data class NavigationContextSwitchEvent(
    val navigationContext: NavigationContext,
    val externalEvent: ExternalEvent?,
    val finishOtherContexts: Boolean = false
)