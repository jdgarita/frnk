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
import dev.jdgarita.frnk.ui.app.ext.toTabbedNavConfig
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.bottomnav.FrnkAppScope
import dev.jdgarita.frnk.ui.bottomnav.FrnkFirstLaunchOnboardingEffect
import dev.jdgarita.frnk.ui.bottomnav.FrnkTabbedNavScaffold
import dev.jdgarita.frnk.ui.nav.FrnkPendingRouteRequest
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.scaffolds.home.HomeEffect
import dev.jdgarita.frnk.ui.scaffolds.onboarding.rememberOnboardingGate
import dev.jdgarita.frnk.ui.scaffolds.rememberFeedbackEmailLauncher
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsEffect
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.settings.rememberDefaultSettingsState
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject

/**
 * The toolkit's **batteries-included app root** — [FrnkTabbedNavScaffold] plus the runtime-resolved wiring
 * no hand-built shell gets for free: a fail-fast check that `initializeFrnk(...)` ran
 * ([requireFrnkKoin]), a Settings tab driven by the **live**
 * [EntitlementManager] (the Subscription section flips Free↔Pro as entitlements change), the
 * monetization-aware Settings handler ([rememberFrnkSettingsHandler]: Upgrade → paywall, Restore,
 * god mode, Manage Subscription) with appearance / onboarding / feedback fallbacks, the
 * auto-mounted [FrnkRoute.Paywall] destination ([FrnkMonetizationConfig.paywallFeatures]), and
 * **first-launch onboarding** — when [FrnkOnboardingConfig.pages][dev.jdgarita.frnk.ui.bottomnav.FrnkOnboardingConfig.pages]
 * are supplied and [FrnkOnboardingConfig.showOnFirstLaunch][dev.jdgarita.frnk.ui.bottomnav.FrnkOnboardingConfig.showOnFirstLaunch]
 * is true, the onboarding flow is presented once on first open (persisted via a `KeyValueStore`-backed
 * [rememberOnboardingGate]; no-op when no store is bound).
 *
 * The host's declarative input is bundled in [config] ([FrnkAppConfig]); the composable keeps only
 * *behaviour* as parameters (the `@Composable` slots [homeContent]/[effects]/[entries], the event
 * callbacks [onMessage]/[onHomeEffect], and the runtime controllers [appearanceController]/[pendingRoutes]).
 *
 * [settingsState] / [settingsEffects] are optional **Settings overrides**: leave them null to keep the
 * batteries-included default (the live [EntitlementManager]-driven catalogue + monetization-aware
 * handler), or supply them to inject a host-owned catalogue/handler (e.g. a Developer section) while
 * still getting the Koin check, auto-mounted paywall, and first-launch onboarding for free. This is the
 * same pair [FrnkTabbedNavScaffold] exposes, surfaced here so a host need not drop to the lower scaffold
 * just to customize Settings.
 *
 * Minimal host integration:
 *
 * ```kotlin
 * // Application.onCreate (Android) — iOS calls the common initializeFrnk(modules = …) before the VC.
 * initializeFrnk(context = this, modules = frnkUiModules() + databaseModule + prefsModule + …)
 *
 * // Activity / ComposeUIViewController:
 * setContent {
 *     FrnkAppScaffold(
 *         config = FrnkAppConfig(
 *             app = FrnkAppInfo(name = "Still", version = "v1.0"),
 *             nav = FrnkNavConfig(feature = FrnkFeatureItem(…)),
 *         ),
 *         entries = { scope -> entry(featureRoute) { … } },
 *     ) {
 *         // Home items — the scaffold owns the scrolling column.
 *     }
 * }
 * ```
 *
 * Without monetization modules installed (no [EntitlementManager] in the graph) the Settings tab
 * renders with `isPro = false` and monetization rows degrade to no-ops; the paywall entry is not mounted.
 * Extension points ([effects], [entries], [onHomeEffect], …) are forwarded to [FrnkTabbedNavScaffold] —
 * see its docs for the contracts (notably: register the feature tab's route in [entries], and don't
 * re-register the built-in routes).
 */
@Composable
fun FrnkAppScaffold(
    config: FrnkAppConfig,
    modifier: Modifier = Modifier,
    appearanceController: AppearanceController? = null,
    pendingRoutes: FrnkPendingRouteRequest? = null,
    onMessage: (String) -> Unit = {},
    onHomeEffect: FrnkAppScope.(HomeEffect) -> Unit = {},
    settingsState: (@Composable (FrnkAppScope) -> SettingsScreenState)? = null,
    settingsEffects: (@Composable (FrnkAppScope) -> (SettingsEffect) -> Unit)? = null,
    effects: @Composable (FrnkAppScope) -> Unit = {},
    entries: EntryProviderScope<NavKey>.(FrnkAppScope) -> Unit = {},
    homeContent: @Composable ColumnScope.() -> Unit
) {
    val koin = remember { requireFrnkKoin() }
    // Monetization is optional (a host that installs no monetization modules has no
    // EntitlementManager); resolve leniently and degrade the Settings tab + paywall when absent.
    val entitlements: EntitlementManager? = remember(koin) { koin.getOrNull<EntitlementManager>() }
    val isPro by (entitlements?.isPro ?: remember { MutableStateFlow(false) }).collectAsStateWithLifecycle()
    // First-launch gate: present onboarding once on first open. Resolved leniently (null when no
    // KeyValueStore is bound), same degrade-gracefully pattern as [entitlements].
    val onboardingGate = rememberOnboardingGate()

    // Project the host config onto the lower scaffold, injecting the one battery this layer owns: the
    // Home top bar defaults to the app name (vs. the lower scaffold's generic "Home" string). Settings
    // passes through verbatim — the Settings VM reacts to isPro on its own (the recomputed catalogue
    // below flows down through settingsState and is merged in via SettingsIntent.ConfigChanged), so no
    // vmKey re-keying is needed. The remember stays keyed on isPro so rememberDefaultSettingsState
    // rebuilds the Subscription rows (Free↔Pro) when entitlements flip.
    val tabbedConfig =
        remember(config, isPro) {
            config.toTabbedNavConfig().copy(
                home =
                    if (config.home.topBar == null) {
                        config.home.copy(topBar = FrnkTopAppBarState(title = config.app.name))
                    } else {
                        config.home
                    }
            )
        }

    FrnkTabbedNavScaffold(
        config = tabbedConfig,
        modifier = modifier,
        appearanceController = appearanceController,
        pendingRoutes = pendingRoutes,
        onHomeEffect = onHomeEffect,
        // Host override when supplied; otherwise the batteries-included default — the live
        // EntitlementManager-driven catalogue (NOT the lower scaffold's bare default). A host that
        // injects its own catalogue (e.g. an extra Developer section) still gets the Koin check,
        // auto-paywall and first-launch onboarding this layer owns.
        settingsState =
            settingsState ?: { _ ->
                rememberDefaultSettingsState(
                    version = config.app.version,
                    appearance = LocalAppearanceController.current.appearance,
                    isPro = isPro,
                    // Blank in-content title — the shell's Settings top bar already shows the heading.
                    title = FrnkStringSource.Raw(""),
                    extraSections = config.settings.extraSections
                )
            },
        settingsEffects =
            settingsEffects ?: { scope ->
                rememberFrnkAppSettingsHandler(
                    scope = scope,
                    appName = config.app.name,
                    appVersion = config.app.version,
                    entitlements = entitlements,
                    onboardingAvailable = config.onboarding.pages.isNotEmpty(),
                    onMessage = onMessage
                )
            },
        effects = { scope ->
            // Auto-present onboarding on first launch (mounted only when pages are supplied), then
            // run the host's own collectors.
            FrnkFirstLaunchOnboardingEffect(
                scope = scope,
                gate = onboardingGate,
                enabled = config.onboarding.showOnFirstLaunch && config.onboarding.pages.isNotEmpty()
            )
            effects(scope)
        },
        entries = { scope ->
            // The paywall ships with the toolkit — mounted automatically when monetization is in the
            // graph (the same FrnkRoute.Paywall the settings handler targets on UpgradeToPro).
            if (entitlements != null) {
                entry(FrnkRoute.Paywall) {
                    FrnkPaywallDestination(
                        features = config.monetization.paywallFeatures,
                        source = "settings",
                        onMessage = onMessage,
                        onClose = { scope.back() }
                    )
                }
            }
            entries(scope)
        },
        homeContent = homeContent
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
    onMessage: (String) -> Unit
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
                                if (onboardingAvailable) scope.navigateTo(FrnkRoute.Onboarding)
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
        fallback = fallback
    )
}