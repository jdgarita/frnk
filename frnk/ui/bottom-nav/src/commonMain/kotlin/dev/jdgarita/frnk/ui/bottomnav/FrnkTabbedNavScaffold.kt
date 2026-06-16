package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.nav.FrnkFullScreenRoute
import dev.jdgarita.frnk.ui.nav.FrnkNavDisplay
import dev.jdgarita.frnk.ui.nav.FrnkPendingRouteRequest
import dev.jdgarita.frnk.ui.nav.FrnkTab
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackHandler
import dev.jdgarita.frnk.ui.nav.FrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.nav.ToolkitRoute
import dev.jdgarita.frnk.ui.nav.frnkNavConfiguration
import dev.jdgarita.frnk.ui.nav.rememberFrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.scaffolds.HomeEffect
import dev.jdgarita.frnk.ui.scaffolds.HomeScreen
import dev.jdgarita.frnk.ui.scaffolds.HomeScreenState
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import dev.jdgarita.frnk.ui.scaffolds.OnboardingEffect
import dev.jdgarita.frnk.ui.scaffolds.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.OnboardingScreen
import dev.jdgarita.frnk.ui.scaffolds.OnboardingScreenState
import dev.jdgarita.frnk.ui.scaffolds.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.SettingsEffect
import dev.jdgarita.frnk.ui.scaffolds.SettingsScreen
import dev.jdgarita.frnk.ui.scaffolds.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.rememberDefaultSettingsState
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkTheme
import dev.jdgarita.frnk.ui.theme.FrnkThemeConfig
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg
import dev.jdgarita.frnk.ui.theme.spacingXl
import dev.jdgarita.frnk.ui.theme.stringNavHome
import dev.jdgarita.frnk.ui.theme.stringSettings
import dev.jdgarita.frnk.ui.theme.strings
import kotlinx.serialization.modules.EmptySerializersModule
import kotlinx.serialization.modules.SerializersModule

/**
 * The toolkit's **one-call tabbed app** — a single composable that stands up a complete app: the
 * [FrnkTheme] wrap, the Navigation3 saved-state configuration, the fixed `Home · [feature] · Settings`
 * adaptive tabs, per-tab back stacks, the platform-adaptive bottom bar + nav3 multiple-back-stack
 * rendering (bar overlay, tab switching, back convention, full-screen hiding, bottom-inset), built-in
 * **Home** ([HomeScreen] with the host's [homeContent] slot), **Settings** ([rememberDefaultSettingsState]
 * catalogue + [settingsExtraSections]) and optional **Onboarding** ([onboardingPages]) destinations, and
 * deep-linking ([pendingRoutes]). A host that used to hand-wire ~150 lines of theme + nav + tab plumbing
 * writes one call.
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
 * The center **[feature]** tab is the host's one configurable tab — point it at the app's signature
 * surface (a "New X" flow, a capture screen, the main library). It is a real navigable tab, so the host
 * **must** register its root via [entries] (`entry(feature.route) { … }`), exactly like any other host
 * route; this composable owns only Home/Settings/Onboarding.
 *
 * [hideBarFor] returns `true` for routes that should hide the bar (full-screen pushes like an onboarding
 * flow or a paywall). It **defaults to `{ it is FrnkFullScreenRoute }`**, so a route declares the intent
 * on itself rather than the host maintaining a separate predicate.
 *
 * Host extension points, each handed the [FrnkAppScope] (the per-tab back stacks):
 *  - [homeContent] — the Home tab's body; items in a scaffold-owned scrolling column ([HomeScreen]).
 *  - [entries] — additional destinations (the [feature] tab's root, pushed details), registered on the
 *    `entryProvider`. **Do not** re-register [homeRoot], [settingsRoot], `ToolkitRoute.Onboarding` (when
 *    [onboardingPages] is non-empty) — nav3 throws on duplicates.
 *  - [effects] — composed inside the theme above the nav host; put the host's single `EffectCollector`
 *    here and drive navigation via `scope.navigateTo(route)`.
 *  - [onHomeEffect] / [settingsEffects] — the built-in tabs' effect handlers. The default Settings handler
 *    applies appearance changes and opens onboarding; everything else is a no-op until the host (or
 *    `FrnkAppScaffold`) supplies a handler.
 */
@Composable
fun FrnkTabbedNavScaffold(
    appVersion: String,
    modifier: Modifier = Modifier,
    themeConfig: FrnkThemeConfig = FrnkThemeConfig.Default,
    appearanceController: AppearanceController? = null,
    // Tabs / navigation.
    homeRoot: NavKey = ToolkitRoute.Home,
    settingsRoot: NavKey = ToolkitRoute.Settings,
    feature: FrnkFeatureItem,
    hostRoutes: SerializersModule = EmptySerializersModule(),
    hideBarFor: (NavKey) -> Boolean = { it is FrnkFullScreenRoute },
    pendingRoutes: FrnkPendingRouteRequest? = null,
    // Built-in Home tab.
    homeTopBar: FrnkTopAppBarState? = null,
    homeVmKey: String? = null,
    onHomeEffect: FrnkAppScope.(HomeEffect) -> Unit = {},
    // Built-in Settings tab.
    settingsState: (@Composable (FrnkAppScope) -> SettingsScreenState)? = null,
    settingsExtraSections: List<SettingsSectionState> = emptyList(),
    settingsVmKey: String? = null,
    settingsEffects: (@Composable (FrnkAppScope) -> (SettingsEffect) -> Unit)? = null,
    // Built-in Onboarding (registered only when pages are supplied).
    onboardingPages: List<OnboardingPageState> = emptyList(),
    // Host extension points.
    effects: @Composable (FrnkAppScope) -> Unit = {},
    entries: EntryProviderScope<NavKey>.(FrnkAppScope) -> Unit = {},
    homeContent: @Composable ColumnScope.() -> Unit,
) {
    val controller = appearanceController ?: remember { AppearanceController() }
    FrnkTheme(config = themeConfig, appearanceController = controller) {
        val navConfig = remember(hostRoutes) { frnkNavConfiguration(hostRoutes) }
        val navState =
            rememberFrnkBottomNavState(
                homeRoot = homeRoot,
                settingsRoot = settingsRoot,
                feature = feature,
            )
        val backStackTabs = remember(navState) { navState.tabs.map { FrnkTab(key = it.key, root = it.root) } }
        val tabbed = rememberFrnkTabbedBackStacks(configuration = navConfig, tabs = backStackTabs)
        val scope = remember(tabbed) { FrnkAppScope(tabbed) }

        // Deep-link signal: a route requested before (or while) the app is up navigates once and is
        // consumed. State-based (FrnkPendingRouteRequest), so an early request still delivers.
        if (pendingRoutes != null) {
            val pending by pendingRoutes.pending.collectAsStateWithLifecycle()
            LaunchedEffect(pending) {
                pending?.let { route ->
                    scope.navigateTo(route)
                    pendingRoutes.consume()
                }
            }
        }

        // The host's collectors live above the nav host so one EffectCollector survives tab swaps.
        effects(scope)

        TabbedNavHost(
            tabbed = tabbed,
            tabs = navState.tabs,
            modifier = modifier.fillMaxSize(),
            hideBarFor = hideBarFor,
            entryProvider =
                entryProvider {
                    entry(homeRoot) {
                        val topBar = homeTopBar ?: FrnkTopAppBarState(title = Theme[strings][stringNavHome])
                        HomeScreen(
                            initialState =
                                HomeScreenState(
                                    topBar = topBar,
                                ),
                            // The VM is seeded once via parametersOf — pass a fresh homeVmKey when a
                            // dynamic [homeTopBar] (e.g. an action hidden once Pro) must re-seed it.
                            vmKey = homeVmKey,
                            onEffect = { effect -> scope.onHomeEffect(effect) },
                            content = homeContent,
                        )
                    }
                    entry(settingsRoot) {
                        TabbedNavSettingsTab(
                            scope = scope,
                            appVersion = appVersion,
                            settingsState = settingsState,
                            extraSections = settingsExtraSections,
                            vmKey = settingsVmKey,
                            settingsEffects = settingsEffects,
                            onboardingAvailable = onboardingPages.isNotEmpty(),
                        )
                    }
                    if (onboardingPages.isNotEmpty()) {
                        entry(ToolkitRoute.Onboarding) {
                            OnboardingScreen(
                                initialState = remember(onboardingPages) { OnboardingScreenState(pages = onboardingPages) },
                                modifier = Modifier.fillMaxSize(),
                                onEffect = { effect ->
                                    when (effect) {
                                        OnboardingEffect.CloseRequested,
                                        OnboardingEffect.Completed,
                                        -> scope.back()
                                    }
                                },
                            )
                        }
                    }
                    entries(scope)
                },
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
    tabs: List<FrnkAdaptiveNavTab>,
    modifier: Modifier = Modifier,
    hideBarFor: (NavKey) -> Boolean,
    entryProvider: (NavKey) -> NavEntry<NavKey>,
) {
    // Back from a non-home tab's root returns to the home tab (rather than exiting the app); within-tab
    // pops and the home-root exit are handled by FrnkNavDisplay / the system.
    FrnkTabbedBackHandler(tabbed)

    // One bar item per tab — the bar always shows exactly the tabs handed in (no FAB, no injected item).
    val navBarItems =
        remember(tabs) {
            tabs.map {
                FrnkNavBarItem(key = it.key, icon = it.icon, iosSystemIcon = it.iosSystemIcon, label = it.label)
            }
        }

    val selectedIndex = navBarItems.indexOfFirst { it.key == tabbed.currentTabKey }

    // Re-tap the active tab → pop to its root; tap another → switch (multiple back stacks).
    val onItemSelected: (Int) -> Unit = { index ->
        val key = navBarItems[index].key
        if (key == tabbed.currentTabKey) tabbed.resetCurrentToRoot() else tabbed.switchTo(key)
    }

    // Hide the bar on full-screen routes (paywall, onboarding, …); also guard against an unknown tab.
    val top = tabbed.current.lastOrNull()
    val barVisible = (top == null || !hideBarFor(top)) && selectedIndex >= 0

    // Reserve the bar's footprint as the content's bottom inset (read unconditionally — it's a @Composable
    // getter) so screens on FrnkScreenScaffold / FrnkMviScreen pad their scrollable content above the bar
    // and the last item isn't hidden behind it. Provided as 0 while the bar is hidden.
    val reservedHeight = FrnkNavBarDefaults.reservedHeight
    val contentInset = if (barVisible) reservedHeight else 0.dp

    Box(modifier = modifier.fillMaxSize()) {
        CompositionLocalProvider(
            LocalFrnkBottomBarInset provides contentInset,
        ) {
            FrnkNavDisplay(
                backStack = tabbed.current,
                modifier = Modifier.fillMaxSize(),
                entryProvider = entryProvider,
            )
        }

        if (barVisible) {
            FrnkBottomFloatingBar(
                items = navBarItems,
                selectedIndex = selectedIndex,
                onItemSelected = onItemSelected,
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            )
        }
    }
}

/**
 * The built-in Settings tab — the proven `FrnkScreenScaffold("Settings") { SettingsScreen(...) }` shape:
 * a tab-root top bar (no back arrow) over the real [SettingsScreen], transparent backdrop (Settings paints
 * its own), and extra bottom padding so the footer clears the floating bar.
 */
@Composable
private fun TabbedNavSettingsTab(
    scope: FrnkAppScope,
    appVersion: String,
    settingsState: (@Composable (FrnkAppScope) -> SettingsScreenState)?,
    extraSections: List<SettingsSectionState>,
    vmKey: String?,
    settingsEffects: (@Composable (FrnkAppScope) -> (SettingsEffect) -> Unit)?,
    onboardingAvailable: Boolean,
) {
    val state =
        settingsState?.invoke(scope)
            ?: rememberDefaultSettingsState(
                version = appVersion,
                appearance = LocalAppearanceController.current.appearance,
                // Blank in-content title — the top bar below already shows the heading.
                title = "",
                extraSections = extraSections,
            )
    val onEffect = settingsEffects?.invoke(scope) ?: rememberDefaultSettingsHandler(scope, onboardingAvailable)

    FrnkScreenScaffold(
        topBar = FrnkTopAppBarState(title = Theme[strings][stringSettings]),
        // SettingsScreenContent paints its own colorBackground; keep the scaffold backdrop
        // transparent to avoid a redundant full-screen overdraw.
        containerColor = Color.Transparent,
        contentPadding =
            PaddingValues(
                start = Theme[spacing][spacingLg],
                top = Theme[spacing][spacingLg],
                end = Theme[spacing][spacingLg],
                bottom = Theme[spacing][spacingXl],
            ),
    ) { padding ->
        SettingsScreen(
            initialState = state,
            modifier = Modifier.fillMaxSize(),
            vmKey = vmKey,
            contentPadding = padding,
            onEffect = onEffect,
        )
    }
}

/**
 * The built-in default Settings effect handler: applies appearance changes to the ambient
 * [LocalAppearanceController] and opens the built-in onboarding flow. Every other effect is a no-op
 * — `FrnkAppScaffold` (or a host-supplied `settingsEffects`) wires monetization, feedback, legal, …
 */
@Composable
private fun rememberDefaultSettingsHandler(
    scope: FrnkAppScope,
    onboardingAvailable: Boolean,
): (SettingsEffect) -> Unit {
    val controller = LocalAppearanceController.current
    return remember(scope, controller, onboardingAvailable) {
        { effect ->
            when (effect) {
                is SettingsEffect.AppearanceChanged -> controller.appearance = effect.appearance
                is SettingsEffect.ActionInvoked ->
                    when (effect.action) {
                        SettingsAction.ShowOnboarding ->
                            if (onboardingAvailable) scope.navigateTo(ToolkitRoute.Onboarding)
                        else -> Unit
                    }
                else -> Unit
            }
        }
    }
}
