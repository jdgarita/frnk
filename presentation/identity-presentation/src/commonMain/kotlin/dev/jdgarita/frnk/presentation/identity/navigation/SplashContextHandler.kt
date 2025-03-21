package dev.jdgarita.frnk.presentation.identity.navigation

import dev.jdgarita.frnk.presentation.framework.navigation.NavigationContext
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationContextHandler
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationRouter
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent

internal class SplashContextHandler(
    override val navigationRouterProvider: () -> NavigationRouter
) : NavigationContextHandler {
    override val navigationContext: NavigationContext = SplashContext
    override val startEvent: ExternalEvent = SplashStartEvent
    override fun canHandle(externalEvent: ExternalEvent): Boolean = externalEvent is SplashStartEvent
}

object SplashContext : NavigationContext
data object SplashStartEvent : ExternalEvent