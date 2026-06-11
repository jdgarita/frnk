package dev.jdgarita.frnk.ui.app

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.di.requireFrnkKoin
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.ui.FrnkPaywallDestination
import dev.jdgarita.frnk.monetization.ui.rememberFrnkSettingsHandler
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.bottomnav.FrnkAdaptiveNavEngine
import dev.jdgarita.frnk.ui.bottomnav.FrnkAdaptiveNavTab
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavPrimaryAction
import dev.jdgarita.frnk.ui.nav.FrnkFullScreenRoute
import dev.jdgarita.frnk.ui.nav.FrnkPendingRouteRequest
import dev.jdgarita.frnk.ui.nav.ToolkitRoute
import dev.jdgarita.frnk.ui.scaffolds.HomeEffect
import dev.jdgarita.frnk.ui.scaffolds.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.SettingsEffect
import dev.jdgarita.frnk.ui.scaffolds.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.rememberDefaultSettingsState
import dev.jdgarita.frnk.ui.scaffolds.rememberFeedbackEmailLauncher
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule
import org.koin.compose.koinInject

/**
 * The toolkit's **batteries-included app root** — `FrnkAppShell` plus the runtime-resolved wiring
 * no hand-built shell gets for free: a fail-fast check that `initializeFrnk(...)` ran
 * ([requireFrnkKoin]), a Settings tab driven by the **live**
 * [EntitlementManager] (the Subscription section flips Free↔Pro as entitlements change), the
 * monetization-aware Settings handler ([rememberFrnkSettingsHandler]: Upgrade → paywall, Restore,
 * god mode, Manage Subscription) with appearance / onboarding / feedback fallbacks, and the
 * auto-mounted [ToolkitRoute.Paywall] destination ([paywallFeatures]).
 *
 * Minimal host integration:
 *
 * ```kotlin
 * // Application.onCreate (Android) — iOS calls the common initializeFrnk(modules = …) before the VC.
 * initializeFrnk(context = this, modules = frnkUiModules() + databaseModule + …)
 *
 * // Activity / ComposeUIViewController:
 * setContent {
 *     FrnkAppScaffold(appName = "Still", appVersion = "v1.0") {
 *         // Home items — the scaffold owns the scrolling column.
 *     }
 * }
 * ```
 *
 * Without monetization modules installed (no [EntitlementManager] in the graph) the Settings tab
 * renders with `isPro = false` and monetization rows degrade to no-ops; the paywall entry is not mounted.
 * Extension points ([effects], [entries], [onHomeEffect], [middleTabs], …) are forwarded to
 * [FrnkAppShell] — see its docs for the contracts (notably: don't re-register the built-in routes).
 */
@Composable
fun FrnkAppScaffold(
    appName: String,
    appVersion: String,
    modifier: Modifier = Modifier,
    themeConfig: FrnkThemeConfig = FrnkThemeConfig.Default,
    appearanceController: AppearanceController? = null,
    middleTabs: List<FrnkAdaptiveNavTab> = emptyList(),
    hostRoutes: SerializersModule = EmptySerializersModule(),
    engine: FrnkAdaptiveNavEngine = FrnkAdaptiveNavEngine.Calf,
    primaryAction: FrnkNavPrimaryAction? = null,
    onPrimaryAction: (() -> Unit)? = null,
    hideBarFor: (NavKey) -> Boolean = { it is FrnkFullScreenRoute },
    pendingRoutes: FrnkPendingRouteRequest? = null,
    homeTopBar: FrnkTopAppBarState? = null,
    homeVmKey: String? = null,
    homePrimaryActionEnabled: Boolean = false,
    onHomeEffect: FrnkAppScope.(HomeEffect) -> Unit = {},
    settingsExtraSections: List<SettingsSectionState> = emptyList(),
    onboardingPages: List<OnboardingPageState> = emptyList(),
    paywallFeatures: List<String> = emptyList(),
    onMessage: (String) -> Unit = {},
    effects: @Composable (FrnkAppScope) -> Unit = {},
    entries: EntryProviderScope<NavKey>.(FrnkAppScope) -> Unit = {},
    homeContent: @Composable ColumnScope.() -> Unit,
) {
    val koin = remember { requireFrnkKoin() }
    // Monetization is optional (a host that installs no monetization modules has no
    // EntitlementManager); resolve leniently and degrade the Settings tab + paywall when absent.
    val entitlements: EntitlementManager? = remember(koin) { koin.getOrNull<EntitlementManager>() }
    val isPro by (entitlements?.isPro ?: remember { MutableStateFlow(false) }).collectAsStateWithLifecycle()

    FrnkAppShell(
        appVersion = appVersion,
        modifier = modifier,
        themeConfig = themeConfig,
        appearanceController = appearanceController,
        middleTabs = middleTabs,
        hostRoutes = hostRoutes,
        engine = engine,
        primaryAction = primaryAction,
        onPrimaryAction = onPrimaryAction,
        hideBarFor = hideBarFor,
        pendingRoutes = pendingRoutes,
        homeTopBar = homeTopBar ?: FrnkTopAppBarState(title = appName),
        homeVmKey = homeVmKey,
        homePrimaryActionEnabled = homePrimaryActionEnabled,
        onHomeEffect = onHomeEffect,
        settingsState = { _ ->
            rememberDefaultSettingsState(
                version = appVersion,
                appearance = LocalAppearanceController.current.appearance,
                isPro = isPro,
                // Blank in-content title — the shell's Settings top bar already shows the heading.
                title = "",
                extraSections = settingsExtraSections,
            )
        },
        // Re-seed the Settings VM when entitlement state flips so the Subscription section swaps
        // Upgrade↔Manage (the VM is seeded once via parametersOf; without a fresh key it would keep
        // the stale initial catalogue).
        settingsVmKey = "frnk-settings-$isPro",
        settingsEffects = { scope ->
            rememberFrnkAppSettingsHandler(
                scope = scope,
                appName = appName,
                appVersion = appVersion,
                entitlements = entitlements,
                onboardingAvailable = onboardingPages.isNotEmpty(),
                onMessage = onMessage,
            )
        },
        onboardingPages = onboardingPages,
        effects = effects,
        entries = { scope ->
            // The paywall ships with the toolkit — mounted automatically when monetization is in the
            // graph (the same ToolkitRoute.Paywall the settings handler targets on UpgradeToPro).
            if (entitlements != null) {
                entry(ToolkitRoute.Paywall) {
                    FrnkPaywallDestination(
                        features = paywallFeatures,
                        source = "settings",
                        onMessage = onMessage,
                        onClose = { scope.back() },
                    )
                }
            }
            entries(scope)
        },
        homeContent = homeContent,
    )
}

/**
 * The scaffold's Settings handler: the toolkit monetization wiring ([rememberFrnkSettingsHandler])
 * when an [EntitlementManager] is bound, with a fallback covering appearance changes, the built-in
 * onboarding flow, and Send Feedback (platform mail composer). Without monetization the fallback is
 * the whole handler.
 */
@Composable
private fun rememberFrnkAppSettingsHandler(
    scope: FrnkAppScope,
    appName: String,
    appVersion: String,
    entitlements: EntitlementManager?,
    onboardingAvailable: Boolean,
    onMessage: (String) -> Unit,
): (SettingsEffect) -> Unit {
    val controller = LocalAppearanceController.current
    val sendFeedback = rememberFeedbackEmailLauncher(appName = appName, appVersion = appVersion)
    val fallback: (SettingsEffect) -> Unit =
        remember(scope, controller, sendFeedback, onboardingAvailable) {
            { effect ->
                when (effect) {
                    is SettingsEffect.AppearanceChanged -> controller.appearance = effect.appearance
                    is SettingsEffect.ActionInvoked ->
                        when (effect.action) {
                            SettingsAction.ShowOnboarding ->
                                if (onboardingAvailable) scope.navigateTo(ToolkitRoute.Onboarding)
                            SettingsAction.SendFeedback -> sendFeedback()
                            else -> Unit
                        }
                    else -> Unit
                }
            }
        }
    if (entitlements == null) return fallback

    val analytics: AnalyticsTracker = koinInject()
    return rememberFrnkSettingsHandler(
        backStack = scope.tabbed.current,
        entitlements = entitlements,
        analytics = analytics,
        onMessage = onMessage,
        fallback = fallback,
    )
}
