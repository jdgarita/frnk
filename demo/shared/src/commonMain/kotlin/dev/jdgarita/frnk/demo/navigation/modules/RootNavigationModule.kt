package dev.jdgarita.frnk.demo.navigation.modules

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.demo.ui.onboarding.OnboardingScreen
import dev.jdgarita.frnk.monetization.ui.FrnkPaywallDestination
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavBarItemModel
import dev.jdgarita.frnk.ui.bottomnav.FrnkNestedNavArguments
import dev.jdgarita.frnk.ui.bottomnav.FrnkNestedNavScaffold
import dev.jdgarita.frnk.ui.mvi.CommonUiEffect
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.clearAndNavigateTo
import dev.jdgarita.frnk.ui.nav.frnkNestedNavConfig
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingEffect
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.iconNavComponent
import dev.jdgarita.frnk.ui.theme.iconNavHome
import dev.jdgarita.frnk.ui.theme.iconNavSettings
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

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
                nestedNavArguments =
                    FrnkNestedNavArguments(
                        items =
                            listOf(
                                FrnkNavBarItemModel(
                                    key = "Home",
                                    icon = FrnkIconSource.Token(iconNavHome),
                                    iosSystemIcon = "house",
                                    label = "Home",
                                    route = FrnkRoute.Home
                                ),
                                FrnkNavBarItemModel(
                                    key = "Components",
                                    icon = FrnkIconSource.Token(iconNavComponent),
                                    iosSystemIcon = "square.grid.2x2",
                                    label = "Components",
                                    route = FrnkRoute.Custom("Components")
                                ),
                                FrnkNavBarItemModel(
                                    key = "Settings",
                                    icon = FrnkIconSource.Token(iconNavSettings),
                                    iosSystemIcon = "gearshape",
                                    label = "Settings",
                                    route = FrnkRoute.Settings
                                )
                            )
                    ),
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