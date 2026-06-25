package dev.jdgarita.frnk.demo

import androidx.compose.runtime.Composable
import dev.jdgarita.frnk.demo.ui.component.ComponentDetailScreen
import dev.jdgarita.frnk.demo.ui.component.ComponentScreen
import dev.jdgarita.frnk.demo.ui.component.ComponentsListScreen
import dev.jdgarita.frnk.demo.ui.home.HomeScreen
import dev.jdgarita.frnk.demo.ui.onboarding.OnboardingScreen
import dev.jdgarita.frnk.demo.ui.settings.SettingsScreen
import dev.jdgarita.frnk.monetization.ui.FrnkPaywallDestination
import dev.jdgarita.frnk.ui.app.FrnkApp
import dev.jdgarita.frnk.ui.app.frnkTabbedRootModule
import dev.jdgarita.frnk.ui.app.rememberFrnkRootStartRoute
import dev.jdgarita.frnk.ui.bottomnav.FrnkCustomTab
import dev.jdgarita.frnk.ui.mvi.CommonUiEffect
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.frnkRootNavConfig
import dev.jdgarita.frnk.ui.scaffolds.home.HomeEffect
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingEffect
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.iconNavComponent

/**
 * The demo's app root — the single shared composable both `demo-android`'s `MainActivity` and the iOS
 * `MainViewController` call. It uses the toolkit's batteries-included tabbed-app helper
 * ([frnkTabbedRootModule]) so the whole onboarding → tab shell → paywall graph is declared inline, with
 * first-launch onboarding gating via [rememberFrnkRootStartRoute]. The middle tab is the demo's
 * "Components" gallery.
 */
@Composable
fun FrnkDemoApp() {
    FrnkApp(
        onSavedStateConfiguration = { frnkRootNavConfig },
        startRoute = rememberFrnkRootStartRoute(),
        onNavigationModule =
            frnkTabbedRootModule(
                customTab =
                    FrnkCustomTab(
                        route = FrnkRoute.Custom("Components"),
                        icon = FrnkIconSource.Token(iconNavComponent),
                        iosSystemIcon = "square.grid.2x2",
                        label = "Components"
                    )
            ) {
                onboarding { onComplete ->
                    OnboardingScreen { uiEffect ->
                        when (uiEffect) {
                            OnboardingEffect.Completed -> onComplete()
                            OnboardingEffect.CloseRequested -> onComplete()
                            CommonUiEffect.DidPressBack() -> onComplete()
                        }
                    }
                }

                home { nav ->
                    HomeScreen { uiEffect ->
                        when (uiEffect) {
                            is HomeEffect.ActionInvoked -> nav.openPaywall()
                            HomeEffect.NavigationInvoked -> Unit
                        }
                    }
                }

                custom { route, nav ->
                    // FrnkRoute.Custom doubles as the Components tab root ("Components") and each component's
                    // detail (id = the component name pushed on top of the Components tab's own back stack).
                    if (route.id == "Components") {
                        ComponentsListScreen(onOpenComponent = { name -> nav.open(FrnkRoute.Custom(name)) })
                    } else {
                        ComponentDetailScreen(name = route.id, onBack = { nav.back() }) {
                            ComponentScreen(name = route.id)
                        }
                    }
                }

                settings { nav ->
                    SettingsScreen(onNavigateToOnboarding = { nav.showOnboarding() })
                }

                paywall { onClose ->
                    FrnkPaywallDestination(
                        features =
                            listOf(
                                "Unlimited everything",
                                "No ads",
                                "Priority support"
                            ),
                        source = "demo",
                        onClose = onClose
                    )
                }
            }
    )
}