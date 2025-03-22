package dev.jdgarita.frnk.presentation.identity.navigation

import dev.jdgarita.frnk.domain.framework.ScreenName
import dev.jdgarita.frnk.domain.framework.ScreenNavigationTracker
import dev.jdgarita.frnk.presentation.framework.navigation.BaseNavigationRouter
import dev.jdgarita.frnk.presentation.framework.navigation.EmptyNavigationResult
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationContextSwitcher
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationRouterResult
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationScreen
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationType
import dev.jdgarita.frnk.presentation.identity.api.AuthNavigationResult
import dev.jdgarita.frnk.presentation.identity.splash.SplashExternalEvent
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
import kotlinx.coroutines.CoroutineDispatcher

class SplashNavigationRouter(
    coroutineDispatcher: CoroutineDispatcher,
    private val navigationContextSwitcher: NavigationContextSwitcher,
    screenNavigationTracker: ScreenNavigationTracker,
    private val authorizedFlowStartEvent: ExternalEvent
) : BaseNavigationRouter<NavigationRouterResult>(
    coroutineDispatcher = coroutineDispatcher,
    navigationContextSwitcher = navigationContextSwitcher,
    screenNavigationTracker = screenNavigationTracker,
    navigationResult = AuthNavigationResult.Succeeded
) {

    override fun processStartEvent(externalEvent: ExternalEvent) {
        if (externalEvent is SplashStartEvent) {
            pushScreen(SplashScreen.Splash, NavigationType.FULL_SCREEN)
        }
    }

    override fun processExternalEvent(externalEvent: ExternalEvent) {
        when (externalEvent) {
            is SplashExternalEvent.DidAuthenticate -> {
                popAllAndChangeContext(authorizedFlowStartEvent)
            }
        }
    }

    override fun processChildNavigationResult(result: NavigationRouterResult) {}

    private fun popAllAndChangeContext(externalEvent: ExternalEvent) {
        popAllAndChangeContext(
            result = EmptyNavigationResult,
            externalEvent = externalEvent,
            finishOtherContexts = true
        )
    }
}

sealed class SplashScreen(override val screenName: ScreenName) : NavigationScreen {
    data object Splash : SplashScreen(ScreenName.Splash)
}