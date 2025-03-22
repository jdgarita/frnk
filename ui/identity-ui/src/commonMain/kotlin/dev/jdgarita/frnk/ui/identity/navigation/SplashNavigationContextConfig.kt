package dev.jdgarita.frnk.ui.identity.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import dev.garita.frnk.ui.framework.FrnkScreen
import dev.garita.frnk.ui.framework.navigation.NavigationContextConfig
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationContext
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationDestination
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationRouter
import dev.jdgarita.frnk.presentation.identity.navigation.SplashContext
import dev.jdgarita.frnk.presentation.identity.navigation.SplashStartEvent
import dev.jdgarita.frnk.presentation.identity.splash.SplashArguments
import dev.jdgarita.frnk.presentation.identity.splash.SplashViewModelWrapper
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
import dev.jdgarita.frnk.ui.identity.splash.SplashScreen
import org.koin.compose.viewmodel.koinViewModel

/**
 * This navigation context config is required for SessionContextNavigationRouter.
 * We do not actually navigating to any destination here, but we need to register the navigation context.
 * It's used to check session state and switch context to AuthNavigationContext if session is not valid.
 */

class SplashNavigationContextConfig : NavigationContextConfig {
    override val navigationContext: NavigationContext = SplashContext
    override val defaultStartEvent: ExternalEvent = SplashStartEvent

    override fun getDestination(externalEvent: ExternalEvent): NavigationDestination? = when {
        externalEvent is SplashStartEvent -> SplashDestination.Splash
        else -> null
    }

    override fun addNavigation(
        navGraphBuilder: NavGraphBuilder,
        navigationRouter: NavigationRouter,
        startDestination: String
    ) {

        navGraphBuilder.navigation(
            startDestination = SplashDestination.Splash.name,
            route = SplashDestination.ROUTE
        ) {
            composable(SplashDestination.Splash.name) {
                val viewModel = koinViewModel<SplashViewModelWrapper>()
                FrnkScreen(
                    viewModel = viewModel,
                    arguments = SplashArguments,
                    navigationRouter = navigationRouter
                ) {
                    SplashScreen(viewModel = viewModel)
                }
            }
        }
    }
}

sealed class SplashDestination(
    override val name: String,
    override val route: String = ROUTE
) : NavigationDestination {
    data object Splash : SplashDestination("$ROUTE/splash")
    companion object {
        const val ROUTE = "session_check"
    }
}