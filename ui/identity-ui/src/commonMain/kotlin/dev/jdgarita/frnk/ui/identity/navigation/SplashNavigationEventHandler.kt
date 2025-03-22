package dev.jdgarita.frnk.ui.identity.navigation

import androidx.navigation.NavController
import dev.garita.frnk.ui.framework.ext.navigateSafe
import dev.garita.frnk.ui.framework.navigation.BaseNavigationEventHandler
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationEvent
import dev.jdgarita.frnk.presentation.framework.navigation.ScreenEntry
import dev.jdgarita.frnk.presentation.identity.navigation.SplashScreen

internal class SplashNavigationEventHandler : BaseNavigationEventHandler() {
    override val route: String = SplashDestination.ROUTE

    override fun canHandle(event: NavigationEvent): Boolean = event.screen.screen is SplashScreen

    override fun handleFullScreen(screenEntry: ScreenEntry, navController: NavController): Boolean {
        when (screenEntry.screen) {
            SplashScreen.Splash -> navController.navigateSafe(SplashDestination.Splash.name)
            else -> return false
        }
        return true
    }
}