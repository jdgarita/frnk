package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.nav.FrnkNavTab
import dev.jdgarita.frnk.ui.nav.FrnkRoute
import dev.jdgarita.frnk.ui.nav.FrnkTab
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.nav.frnkNestedNavConfig
import dev.jdgarita.frnk.ui.nav.rememberFrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import dev.jdgarita.frnk.ui.scaffolds.settings.FrnkSettingsScreen
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsEffect
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsSectionState
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg
import dev.jdgarita.frnk.ui.theme.spacingXl
import dev.jdgarita.frnk.ui.theme.stringSettings

/**
 * The toolkit's **one-call tabbed app** — a single composable that stands up a complete app: the
 * [FrnkTheme] wrap, the Navigation3 saved-state configuration, the fixed `Home · feature · Settings`
 * adaptive tabs, per-tab back stacks, the platform-adaptive bottom bar + nav3 multiple-back-stack
 * rendering (bar overlay, tab switching, back convention, full-screen hiding, bottom-inset), built-in
 * **Home** ([HomeScreen] with the host's [homeContent] slot), **Settings** ([rememberDefaultSettingsState]
 * catalogue + [FrnkSettingsConfig.extraSections]) and optional **Onboarding**
 * ([FrnkOnboardingConfig.pages]) destinations, and deep-linking ([pendingRoutes]). A host that used to
 * hand-wire ~150 lines of theme + nav + tab plumbing writes one call.
 *
 * **Config vs. parameters.** The host's declarative input — app identity, navigation shape, theme, and the
 * built-in tabs' configuration — is bundled in [config] ([FrnkTabbedNavConfig]). The composable keeps only
 * *behaviour* as parameters: the `@Composable` slots/factories ([homeContent], [settingsState],
 * [settingsEffects], [effects], [entries]), the event callback [onHomeEffect], and the runtime controllers
 * ([appearanceController], [pendingRoutes]) — none of which can live in an `@Immutable` config.
 *
 * **Bar + navigation.** Internally renders the fixed three-tab [FrnkBottomFloatingBar] — a Material3
 * *Expressive* `HorizontalFloatingToolbar` (floating pill) on Android and a native glassy `UITabBar`
 * (iOS 26+) / Material3 bar (older) on iOS — over a single [FrnkNavDisplay] driven by the active tab's back
 * stack. Tabs render through their `icon` (`ImageVector`, Android) / `iosSystemIcon` (SF-Symbol, iOS).
 * Tapping a tab switches its back stack; re-tapping the active tab pops it to its root; back from a
 * non-home tab root returns home ([FrnkTabbedBackHandler]). This is the **Material3 adaptive-bar** app by
 * design (the bar renders through this module's Material3 surface). A host that wants nav3
 * multiple-back-stack navigation **without** Material3 — or a tab shape other than the fixed
 * `Home · feature · Settings` — wires the lower-level primitives directly:
 * `rememberFrnkTabbedBackStacks` + `FrnkNavDisplay(backStack = tabbed.current)` + `FrnkTabbedBackHandler` +
 * a bar of its choosing.
 *
 * Assumes **Koin is already started** (the VM-backed scaffolds resolve through `koinViewModel`) —
 * `frnkUiModules()` (`:ui-app`) carries every scaffold module this composable needs. `:ui-app`'s
 * `FrnkAppScaffold` is the batteries-included layer over this (Koin assertion, live entitlement-aware
 * Settings, monetization handler, auto-mounted paywall); use this composable directly when your module
 * can't see `:ui-app` (as `:shared-demo` does) or when you want to supply those pieces yourself.
 *
 * The center [FrnkNavConfig.feature] tab is the host's one configurable tab — point it at the app's
 * signature surface (a "New X" flow, a capture screen, the main library). It is a real navigable tab, so
 * the host **must** register its root via [entries] (`entry(feature.route) { … }`), exactly like any other
 * host route; this composable owns only Home/Settings/Onboarding.
 *
 * [FrnkNavConfig.hideBarFor] returns `true` for routes that should hide the bar (full-screen pushes like an
 * onboarding flow or a paywall). It **defaults to `{ it is FrnkFullScreenRoute }`**, so a route declares
 * the intent on itself rather than the host maintaining a separate predicate.
 *
 * Host extension points, each handed the [FrnkAppScope] (the per-tab back stacks):
 *  - [homeContent] — the Home tab's body; items in a scaffold-owned scrolling column ([HomeScreen]).
 *  - [entries] — additional destinations (the feature tab's root, pushed details), registered on the
 *    `entryProvider`. **Do not** re-register [FrnkNavConfig.homeRoot], [FrnkNavConfig.settingsRoot],
 *    `FrnkRoute.Onboarding` (when [FrnkOnboardingConfig.pages] is non-empty) — nav3 throws on duplicates.
 *  - [effects] — composed inside the theme above the nav host; put the host's single `EffectCollector`
 *    here and drive navigation via `scope.navigateTo(route)`.
 *  - [onHomeEffect] / [settingsEffects] — the built-in tabs' effect handlers. The default Settings handler
 *    applies appearance changes and opens onboarding; everything else is a no-op until the host (or
 *    `FrnkAppScaffold`) supplies a handler.
 */
@Composable
fun FrnkTabbedNavScaffold(
    config: FrnkTabbedNavConfig,
    modifier: Modifier = Modifier,
    settingsEffects: (@Composable (FrnkAppScope) -> (SettingsEffect) -> Unit)? = null,
    effects: @Composable (FrnkAppScope) -> Unit = {},
    entries: EntryProviderScope<NavKey>.(FrnkAppScope) -> Unit = {},
    homeContent: @Composable ColumnScope.() -> Unit
) {
    FrnkTheme(config = config.theme, appearanceController = AppearanceController()) {
        val navState =
            rememberFrnkBottomNavState(
                homeRoot = config.nav.homeRoot,
                settingsRoot = config.nav.settingsRoot,
                feature = config.nav.feature
            )
        val backStackTabs = remember(navState) { navState.tabs.map { FrnkTab(key = it.key, root = it.root) } }
        val tabbed =
            rememberFrnkTabbedBackStacks(
                configuration = frnkNestedNavConfig(config.nav.hostRoutes),
                tabs = backStackTabs
            )
        val scope = remember(tabbed) { FrnkAppScope(tabbed) }

        // The host's collectors live above the nav host so one EffectCollector survives tab swaps.
        effects(scope)

        TabbedNavHost(
            tabbed = tabbed,
            tabs = navState.tabs,
            modifier = modifier.fillMaxSize(),
            hideBarFor = config.nav.hideBarFor,
            entryProvider =
                entryProvider {
                    entries(scope)
                    entry(config.nav.homeRoot) {
                        // TODO: restore Home destination — HomeScreen wiring stubbed during the
                        //  model-first MVI + two-level nav refactor (see commented block below).
                        Box(Modifier)
//                        val topBar = config.home.topBar ?: FrnkTopAppBarState(title = Theme[strings][stringNavHome])
//                        // Stable identity (remember-keyed on topBar) so HomeScreen's reactive sync only
//                        // fires when the chrome actually changed — e.g. a dynamic [home.topBar] action
//                        // hidden once Pro. HomeScreen merges it via HomeIntent.ConfigChanged; no re-key.
//                        val homeState = remember(topBar) { HomeScreenState(topBar = topBar) }
//                        HomeScreen(
//                            initialState = homeState,
//                            vmKey = config.home.vmKey,
//                            onEffect = { effect -> scope.onHomeEffect(effect) },
//                            content = homeContent
//                        )
                    }
                    entry(config.nav.settingsRoot) {
                        TabbedNavSettingsTab(
                            appVersion = config.app.version,
                            extraSections = config.settings.extraSections,
                            onboardingAvailable = config.onboarding.pages.isNotEmpty()
                        )
                    }
                    if (config.onboarding.pages.isNotEmpty()) {
                        entry(FrnkRoute.Onboarding) {
                            // TODO: restore Onboarding destination — OnboardingScreen wiring stubbed
                            //  during the model-first MVI + two-level nav refactor (see below).
                            Box(Modifier)
//                            OnboardingScreen(
//                                arguments =
//                                    remember(config.onboarding.pages) {
//                                        OnboardingArguments(pages = config.onboarding.pages)
//                                    },
//                                modifier = Modifier.fillMaxSize(),
//                                onEffect = { effect ->
//                                    when (effect) {
//                                        OnboardingEffect.CloseRequested,
//                                        OnboardingEffect.Completed
//                                        -> scope.back()
//                                    }
//                                }
//                            )
                        }
                    }
                }
        )
    }
}

/**
 * The nav3 multiple-back-stack render core: a single [FrnkNavDisplay] driven by the active tab's back
 * stack, the platform-adaptive bottom bar overlaid above it (so it persists across tab swaps), tab
 * switching + re-tap-to-root, the "back from a non-home tab root returns to home" convention
 * ([FrnkTabbedBackHandler]), full-screen-route bar hiding, and the bottom-inset bookkeeping that lets
 * content scroll behind the bar. Private — [FrnkTabbedNavScaffold] above is the sole public entry point;
 * it creates the [tabbed] state and the [entryProvider] and hands them here.
 *
 * One bar item per tab — the bar always shows exactly the [tabs] handed in (no FAB, no injected item).
 * The bar's reserved height is provided through [LocalFrnkBottomBarInset] only while it shows, so screens
 * built on `FrnkScreenScaffold` / `FrnkMviScreen` reserve it automatically (no per-screen `bottomInset`
 * threading).
 */
@Composable
private fun TabbedNavHost(
    tabbed: FrnkTabbedBackStacks,
    tabs: List<FrnkBottomNavTab>,
    modifier: Modifier = Modifier,
    hideBarFor: (NavKey) -> Boolean,
    entryProvider: (NavKey) -> NavEntry<NavKey>
) {
    // The fixed-three-tab scaffold is the batteries-included superset over the flexible
    // FrnkNestedNavScaffold render core: map the fixed FrnkBottomNavTab slots to the general FrnkNavTab
    // shape and delegate the bar + nav3 multiple-back-stack rendering to it.
    val navTabs =
        remember(tabs) {
            tabs.map {
                FrnkNavTab(key = it.key, root = it.root, icon = it.icon, label = it.label, iosSystemIcon = it.iosSystemIcon)
            }
        }
    FrnkNestedNavScaffold(
        tabbed = tabbed,
        tabs = navTabs,
        modifier = modifier,
        hideBarFor = hideBarFor,
        entryProvider = entryProvider
    )
}

/**
 * The built-in Settings tab — the proven `FrnkScreenScaffold("Settings") { SettingsScreen(...) }` shape:
 * a tab-root top bar (no back arrow) over the real [FrnkSettingsScreen], transparent backdrop (Settings paints
 * its own), and extra bottom padding so the footer clears the floating bar.
 */
@Composable
private fun TabbedNavSettingsTab(
    appVersion: String,
    extraSections: List<SettingsSectionState>,
    onboardingAvailable: Boolean
) {
    FrnkScreenScaffold(
        topBar = FrnkTopAppBarState(title = FrnkStringSource.Token(stringSettings)),
        // SettingsScreenContent paints its own colorBackground; keep the scaffold backdrop
        // transparent to avoid a redundant full-screen overdraw.
        containerColor = Color.Transparent,
        contentPadding =
            PaddingValues(
                start = Theme[spacing][spacingLg],
                top = Theme[spacing][spacingLg],
                end = Theme[spacing][spacingLg],
                bottom = Theme[spacing][spacingXl]
            )
    ) { padding ->
        // TODO: restore Settings destination content — stubbed during the model-first MVI + two-level
        //  nav refactor; re-wire SettingsScreen here.
        Box(Modifier)
    }
}