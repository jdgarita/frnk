package dev.jdgarita.frnk.demo.navigation.modules

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.demo.ui.component.ComponentsListScreen
import dev.jdgarita.frnk.demo.ui.home.HomeScreen
import dev.jdgarita.frnk.demo.ui.settings.SettingsScreen
import dev.jdgarita.frnk.ui.mvi.CommonUiEffect
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.scaffolds.home.HomeEffect
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
fun nestedNavigationModule(
    backStack: NavBackStack<NavKey>,
    onRootNavigate: (navKey: NavKey) -> Unit
) = module {
    navigation<FrnkRoute.Home> {
        HomeScreen { uiEffect ->
            when (uiEffect) {
                is HomeEffect.ActionInvoked -> {
                    onRootNavigate(FrnkRootRoute.Paywall)
                }

                HomeEffect.NavigationInvoked -> {
                }

                CommonUiEffect.DidPressBack() -> backStack.back()
            }
        }
    }

    navigation<FrnkRoute.Custom> {
        ComponentsListScreen {}
    }

    navigation<FrnkRoute.Settings> {
        SettingsScreen(
            onNavigateAway = { backStack.back() },
            onNavigateToOnboarding = { backStack.back() }
        )
    }
}