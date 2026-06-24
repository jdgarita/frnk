package dev.jdgarita.frnk.demo.navigation.modules

import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.demo.ui.component.ComponentDetailScreen
import dev.jdgarita.frnk.demo.ui.component.ComponentScreen
import dev.jdgarita.frnk.demo.ui.component.ComponentsListScreen
import dev.jdgarita.frnk.demo.ui.home.HomeScreen
import dev.jdgarita.frnk.demo.ui.settings.SettingsScreen
import dev.jdgarita.frnk.ui.mvi.CommonUiEffect
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.scaffolds.home.HomeEffect
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

@OptIn(KoinExperimentalAPI::class)
fun nestedNavigationModule(
    tabbed: FrnkTabbedBackStacks,
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

                CommonUiEffect.DidPressBack() -> tabbed.current.back()
            }
        }
    }

    navigation<FrnkRoute.Custom> { route ->
        // FrnkRoute.Custom doubles as the Components tab root ("Components") and each component's detail
        // (id = the component name pushed on top of the Components tab's own back stack).
        if (route.id == "Components") {
            ComponentsListScreen(onOpenComponent = { name -> tabbed.current.navigateTo(FrnkRoute.Custom(name)) })
        } else {
            ComponentDetailScreen(name = route.id, onBack = { tabbed.current.back() }) {
                ComponentScreen(name = route.id)
            }
        }
    }

    navigation<FrnkRoute.Settings> {
        SettingsScreen(
            onNavigateAway = { tabbed.current.back() },
            onNavigateToOnboarding = { tabbed.current.back() }
        )
    }
}