package dev.jdgarita.frnk.ui.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.ui.bottomnav.FrnkCustomTab
import dev.jdgarita.frnk.ui.bottomnav.FrnkNestedNavScaffold
import dev.jdgarita.frnk.ui.nav.FrnkRootRoute
import dev.jdgarita.frnk.ui.nav.FrnkTabRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.clearAndNavigateTo
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.scaffolds.onboarding.rememberOnboardingGate
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation

/**
 * The navigator handed to the tab content slots of [frnkTabbedRootModule]. It hides the two-back-stack
 * reality (the root stack for cross-shell flows, the active tab's stack for within-tab pushes):
 *  - [open] / [back] drive the **active tab's** back stack (push a detail, pop it),
 *  - [openPaywall] / [showOnboarding] drive the **root** back stack (the full-screen flows).
 */
class FrnkTabNavigator internal constructor(
    private val rootBackStack: NavBackStack<NavKey>,
    private val tabBackStack: NavBackStack<NavKey>
) {
    /** Push [route] onto the active tab's back stack (e.g. a detail screen). */
    fun open(route: NavKey) = tabBackStack.navigateTo(route)

    /** Pop the active tab's back stack. */
    fun back() = tabBackStack.back()

    /** Navigate to the root-level paywall ([FrnkRootRoute.Paywall]). */
    fun openPaywall() = rootBackStack.navigateTo(FrnkRootRoute.Paywall)

    /** Navigate to the root-level onboarding flow ([FrnkRootRoute.Onboarding]). */
    fun showOnboarding() = rootBackStack.navigateTo(FrnkRootRoute.Onboarding)
}

/**
 * DSL scope for [frnkTabbedRootModule]. Collects the destinations behind the fixed
 * `Home · <custom> · Settings` tabs (all three required) plus the optional full-screen onboarding and
 * paywall. The tab slots receive a [FrnkTabNavigator]; onboarding/paywall get plain `onComplete`/`onClose`
 * callbacks the helper wires to the root back stack.
 */
class FrnkTabbedRootScope internal constructor() {
    internal var homeContent: (@Composable (nav: FrnkTabNavigator) -> Unit)? = null
    internal var customContent: (@Composable (route: FrnkTabRoute.Custom, nav: FrnkTabNavigator) -> Unit)? = null
    internal var settingsContent: (@Composable (nav: FrnkTabNavigator) -> Unit)? = null
    internal var onboardingContent: (@Composable (onComplete: () -> Unit) -> Unit)? = null
    internal var paywallContent: (@Composable (onClose: () -> Unit) -> Unit)? = null

    /** The Home tab ([FrnkTabRoute.Home]). Required. */
    fun home(content: @Composable (nav: FrnkTabNavigator) -> Unit) {
        homeContent = content
    }

    /**
     * The middle host tab + everything pushed onto it ([FrnkTabRoute.Custom]). Required. The `route.id`
     * distinguishes the tab root from pushed sub-destinations (e.g. `nav.open(FrnkTabRoute.Custom(detailId))`).
     */
    fun custom(content: @Composable (route: FrnkTabRoute.Custom, nav: FrnkTabNavigator) -> Unit) {
        customContent = content
    }

    /** The Settings tab ([FrnkTabRoute.Settings]). Required. */
    fun settings(content: @Composable (nav: FrnkTabNavigator) -> Unit) {
        settingsContent = content
    }

    /** The full-screen onboarding flow ([FrnkRootRoute.Onboarding]). Optional; `onComplete` lands on the tab shell. */
    fun onboarding(content: @Composable (onComplete: () -> Unit) -> Unit) {
        onboardingContent = content
    }

    /** The full-screen paywall ([FrnkRootRoute.Paywall]). Optional; `onClose` pops back to the previous screen. */
    fun paywall(content: @Composable (onClose: () -> Unit) -> Unit) {
        paywallContent = content
    }
}

/**
 * Builds the root navigation module for the **common tabbed app shape** — onboarding → tab shell
 * (`Home · <custom> · Settings`) → paywall — and returns the `(rootBackStack) -> Module` lambda
 * [FrnkApp]'s `onNavigationModule` expects. It collapses what a host otherwise hand-writes (a root Koin
 * module + a nested module + the cross-level navigation threading + the onboarding→Tab / paywall→back
 * conventions + the `navigation<…> { }` DSL + the `KoinExperimentalAPI` opt-in) into the [content] DSL.
 *
 * ```kotlin
 * FrnkApp(
 *     onSavedStateConfiguration = { frnkRootNavConfig() },
 *     startRoute = rememberFrnkRootStartRoute(),
 *     onNavigationModule = frnkTabbedRootModule(customTab) {
 *         onboarding { onComplete -> MyOnboarding(onComplete) }
 *         home     { nav -> MyHome(onUpgrade = nav::openPaywall) }
 *         custom   { route, nav -> /* tab root vs detail by route.id */ }
 *         settings { nav -> MySettings(onShowOnboarding = nav::showOnboarding) }
 *         paywall  { onClose -> MyPaywall(onClose) }
 *     },
 * )
 * ```
 *
 * [home], [custom][FrnkTabbedRootScope.custom] and [settings] are required (the bar has three tabs);
 * onboarding and paywall are optional — omit them and don't register those root routes. For full control
 * over the graph, use the lower-level [FrnkApp] `onNavigationModule` directly instead.
 *
 * @param customTab the host-provided middle tab (route + icon + SF-Symbol + label).
 */
@OptIn(KoinExperimentalAPI::class)
fun frnkTabbedRootModule(
    customTab: FrnkCustomTab,
    content: FrnkTabbedRootScope.() -> Unit
): (rootBackStack: NavBackStack<NavKey>) -> Module {
    val scope = FrnkTabbedRootScope().apply(content)
    val home = requireNotNull(scope.homeContent) { "frnkTabbedRootModule requires a home { … } slot." }
    val custom = requireNotNull(scope.customContent) { "frnkTabbedRootModule requires a custom { … } slot." }
    val settings = requireNotNull(scope.settingsContent) { "frnkTabbedRootModule requires a settings { … } slot." }
    val onboarding = scope.onboardingContent
    val paywall = scope.paywallContent

    return { rootBackStack ->
        module {
            onboarding?.let { onboardingSlot ->
                navigation<FrnkRootRoute.Onboarding> {
                    // Mark first-launch onboarding "seen" the moment it's presented (see rememberOnboardingGate).
                    val gate = rememberOnboardingGate()
                    LaunchedEffect(gate) { gate?.markSeen() }
                    onboardingSlot { rootBackStack.clearAndNavigateTo(FrnkRootRoute.Tab) }
                }
            }

            navigation<FrnkRootRoute.Tab> {
                FrnkNestedNavScaffold(
                    customTab = customTab,
                    onNestedNavigationModule = { tabBackStack ->
                        val nav = FrnkTabNavigator(rootBackStack = rootBackStack, tabBackStack = tabBackStack)
                        module {
                            navigation<FrnkTabRoute.Home> { home(nav) }
                            navigation<FrnkTabRoute.Custom> { route -> custom(route, nav) }
                            navigation<FrnkTabRoute.Settings> { settings(nav) }
                        }
                    }
                )
            }

            paywall?.let { paywallSlot ->
                navigation<FrnkRootRoute.Paywall> {
                    paywallSlot { rootBackStack.back() }
                }
            }
        }
    }
}

/**
 * First-launch-aware start route for the tabbed app: [FrnkRootRoute.Onboarding] until the
 * [OnboardingGate][dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingGate] is marked seen, then
 * [FrnkRootRoute.Tab]. Pass to [FrnkApp]'s `startRoute`. With no `KeyValueStore` bound (no `prefsModule`)
 * the gate is absent, so onboarding shows every launch.
 *
 * @param showOnboarding set `false` to always start on the tab shell (e.g. an app with no onboarding).
 */
@Composable
fun rememberFrnkRootStartRoute(showOnboarding: Boolean = true): FrnkRootRoute {
    val gate = rememberOnboardingGate()
    return if (showOnboarding && gate?.seen != true) FrnkRootRoute.Onboarding else FrnkRootRoute.Tab
}