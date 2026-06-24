package dev.jdgarita.frnk.demo.navigation.modules

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.composeunstyled.Text
import dev.jdgarita.frnk.demo.navigation.NestedNavScaffold
import dev.jdgarita.frnk.demo.ui.onboarding.OnboardingScreen
import dev.jdgarita.frnk.ui.mvi.CommonUiEffect
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.clearAndNavigateTo
import dev.jdgarita.frnk.ui.nav.frnkNestedNavConfig
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingEffect
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
            NestedNavScaffold(
                onSavedStateConfiguration = { frnkNestedNavConfig() },
                onNestedNavigationModule = { nestedBackStack ->
                    nestedNavigationModule(nestedBackStack) { navKey ->
                        backStack.navigateTo(screen = navKey)
                    }
                }
            ) {
                backStack.back()
            }
        }

        navigation<FrnkRootRoute.Paywall> {
            // TODO: mount the real paywall here (FrnkPaywallDestination / PaywallScreen) — placeholder Text.
            Text(
                text = "Paywall"
            )
        }
    }