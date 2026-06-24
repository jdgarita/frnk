package dev.jdgarita.frnk.demo.navigation.modules

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.composables.icons.lucide.Component
import com.composables.icons.lucide.House
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Settings
import dev.jdgarita.frnk.demo.ui.onboarding.OnboardingScreen
import dev.jdgarita.frnk.monetization.ui.FrnkPaywallDestination
import dev.jdgarita.frnk.ui.bottomnav.FrnkNestedNavScaffold
import dev.jdgarita.frnk.ui.mvi.CommonUiEffect
import dev.jdgarita.frnk.ui.nav.FrnkNavTab
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.clearAndNavigateTo
import dev.jdgarita.frnk.ui.nav.frnkNestedNavConfig
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingEffect
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

// The demo's three nested tabs (each its own back stack via FrnkNestedNavScaffold). The center
// "Components" tab is the demo's signature surface; Home/Settings are the bookends.
private val demoNestedTabs =
    listOf(
        FrnkNavTab("Home", FrnkRoute.Home, Lucide.House, "Home", "house"),
        FrnkNavTab("Components", FrnkRoute.Custom("Components"), Lucide.Component, "Component", "square.grid.2x2"),
        FrnkNavTab("Settings", FrnkRoute.Settings, Lucide.Settings, "Settings", "gearshape")
    )

@OptIn(KoinExperimentalAPI::class)
fun rootNavigationModule(backStack: NavBackStack<NavKey>) =
    module {
        navigation<FrnkRootRoute.Onboarding> {
            OnboardingScreen { uiEffect ->
                when (uiEffect) {
                    OnboardingEffect.CloseRequested -> backStack.back()
                    OnboardingEffect.Completed -> {
                        backStack.clearAndNavigateTo(FrnkRootRoute.Tab)
                    }

                    CommonUiEffect.DidPressBack() -> backStack.back()
                }
            }
        }

        navigation<FrnkRootRoute.Tab> {
            FrnkNestedNavScaffold(
                tabs = demoNestedTabs,
                onSavedStateConfiguration = { frnkNestedNavConfig() },
                onNestedNavigationModule = { nestedBackStack ->
                    nestedNavigationModule(nestedBackStack) { navKey ->
                        backStack.navigateTo(screen = navKey)
                    }
                }
            )
        }

        navigation<FrnkRootRoute.Paywall> {
            FrnkPaywallDestination(
                features =
                    listOf(
                        "Unlimited everything",
                        "No ads",
                        "Priority support"
                    ),
                source = "demo",
                onClose = { backStack.back() }
            )
        }
    }