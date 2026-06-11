package dev.jdgarita.frnk.ui.app

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.bottomnav.FrnkAdaptiveNavEngine
import dev.jdgarita.frnk.ui.bottomnav.FrnkAdaptiveNavTab
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavPrimaryAction
import dev.jdgarita.frnk.ui.bottomnav.FrnkTabbedNavScaffold
import dev.jdgarita.frnk.ui.bottomnav.rememberFrnkAdaptiveNavTabs
import dev.jdgarita.frnk.ui.nav.FrnkFullScreenRoute
import dev.jdgarita.frnk.ui.nav.FrnkPendingRouteRequest
import dev.jdgarita.frnk.ui.nav.FrnkPrimaryActionRegistry
import dev.jdgarita.frnk.ui.nav.FrnkTab
import dev.jdgarita.frnk.ui.nav.ToolkitRoute
import dev.jdgarita.frnk.ui.nav.frnkNavConfiguration
import dev.jdgarita.frnk.ui.nav.rememberFrnkTabbedBackStacks
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.scaffolds.HomeEffect
import dev.jdgarita.frnk.ui.scaffolds.HomeScreen
import dev.jdgarita.frnk.ui.scaffolds.HomeScreenState
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
 * The toolkit's **app shell**: one composable that stands up a complete tabbed app — [FrnkTheme]
 * wrap, the Navigation3 saved-state configuration, the Home + [middleTabs] + Settings adaptive tabs,
 * per-tab back stacks, [FrnkTabbedNavScaffold] (bar, back conventions, full-screen hiding,
 * bottom-inset), built-in **Home** ([HomeScreen] with the host's [homeContent] slot), **Settings**
 * ([rememberDefaultSettingsState] catalogue + [settingsExtraSections]) and optional **Onboarding**
 * ([onboardingPages]) destinations, deep-linking ([pendingRoutes]), and the primary-action registry.
 * A host that used to hand-wire ~150 lines of theme + nav + tab plumbing writes one call.
 *
 * Assumes **Koin is already started** (the VM-backed scaffolds resolve through `koinViewModel`) —
 * `frnkUiModules()` (`:ui-app`) carries every scaffold module the shell needs. `:ui-app`'s
 * `FrnkAppScaffold` is the batteries-included layer over this shell (Koin assertion, live
 * entitlement-aware Settings, monetization handler, auto-mounted paywall); use the shell directly
 * when your module can't see `:ui-app` (as `:shared-demo` does) or when you want to supply those
 * pieces yourself.
 *
 * Host extension points, each handed the [FrnkAppScope] (back stacks + primary-action registry):
 *  - [homeContent] — the Home tab's body; items in a scaffold-owned scrolling column ([HomeScreen]).
 *  - [entries] — additional destinations (middle-tab roots, pushed details), registered on the
 *    shell's `entryProvider`. **Do not** re-register [homeRoot], [settingsRoot],
 *    `ToolkitRoute.Onboarding` (when [onboardingPages] is non-empty) — nav3 throws on duplicates.
 *  - [effects] — composed inside the theme above the scaffold; put the host's single
 *    `EffectCollector` here and drive navigation via `scope.navigateTo(route)`.
 *  - [onHomeEffect] / [settingsEffects] — the built-in tabs' effect handlers. The default Settings
 *    handler applies appearance changes and opens onboarding; everything else is a no-op until the
 *    host (or `FrnkAppScaffold`) supplies a handler.
 *
 * The bar's primary-action button (the "Create/Add" FAB; [FrnkAdaptiveNavEngine.AdaptiveNavBar]
 * engine only) is screen-routed: set [homePrimaryActionEnabled] so the Home tab claims it (taps
 * arrive as [HomeEffect.PrimaryActionInvoked]), and any other destination can claim it with
 * `FrnkPrimaryActionHandler { … }`. [onPrimaryAction] remains the host-level fallback.
 */
@Composable
fun FrnkAppShell(
    appVersion: String,
    modifier: Modifier = Modifier,
    themeConfig: FrnkThemeConfig = FrnkThemeConfig.Default,
    appearanceController: AppearanceController? = null,
    // Tabs / navigation.
    homeRoot: NavKey = ToolkitRoute.Home,
    settingsRoot: NavKey = ToolkitRoute.Settings,
    middleTabs: List<FrnkAdaptiveNavTab> = emptyList(),
    hostRoutes: SerializersModule = EmptySerializersModule(),
    engine: FrnkAdaptiveNavEngine = FrnkAdaptiveNavEngine.Calf,
    primaryAction: FrnkNavPrimaryAction? = null,
    onPrimaryAction: (() -> Unit)? = null,
    hideBarFor: (NavKey) -> Boolean = { it is FrnkFullScreenRoute },
    pendingRoutes: FrnkPendingRouteRequest? = null,
    // Built-in Home tab.
    homeTopBar: FrnkTopAppBarState? = null,
    homeVmKey: String? = null,
    homePrimaryActionEnabled: Boolean = false,
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
        val navTabs =
            rememberFrnkAdaptiveNavTabs(
                homeRoot = homeRoot,
                settingsRoot = settingsRoot,
                middleTabs = middleTabs,
            )
        val backStackTabs = remember(navTabs) { navTabs.map { FrnkTab(key = it.key, root = it.root) } }
        val tabbed = rememberFrnkTabbedBackStacks(configuration = navConfig, tabs = backStackTabs)
        val primaryActions = remember { FrnkPrimaryActionRegistry() }
        val scope = remember(tabbed, primaryActions) { FrnkAppScope(tabbed, primaryActions) }

        // Deep-link signal: a route requested before (or while) the shell is up navigates once and
        // is consumed. State-based (FrnkPendingRouteRequest), so an early request still delivers.
        if (pendingRoutes != null) {
            val pending by pendingRoutes.pending.collectAsStateWithLifecycle()
            LaunchedEffect(pending) {
                pending?.let { route ->
                    scope.navigateTo(route)
                    pendingRoutes.consume()
                }
            }
        }

        // The host's collectors live above the scaffold so one EffectCollector survives tab swaps.
        effects(scope)

        FrnkTabbedNavScaffold(
            tabbed = tabbed,
            tabs = navTabs,
            modifier = modifier.fillMaxSize(),
            engine = engine,
            primaryAction = primaryAction,
            onPrimaryAction = onPrimaryAction,
            primaryActionRegistry = primaryActions,
            hideBarFor = hideBarFor,
            entryProvider =
                entryProvider {
                    entry(homeRoot) {
                        val topBar = homeTopBar ?: FrnkTopAppBarState(title = Theme[strings][stringNavHome])
                        HomeScreen(
                            initialState =
                                HomeScreenState(
                                    topBar = topBar,
                                    primaryActionEnabled = homePrimaryActionEnabled,
                                ),
                            // The VM is seeded once via parametersOf — pass a fresh homeVmKey when a
                            // dynamic [homeTopBar] (e.g. an action hidden once Pro) must re-seed it.
                            vmKey = homeVmKey,
                            onEffect = { effect -> scope.onHomeEffect(effect) },
                            content = homeContent,
                        )
                    }
                    entry(settingsRoot) {
                        FrnkAppSettingsTab(
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
 * The shell's Settings tab — the proven `FrnkScreenScaffold("Settings") { SettingsScreen(...) }`
 * shape lifted from the demo: a tab-root top bar (no back arrow) over the real [SettingsScreen],
 * transparent backdrop (Settings paints its own), and extra bottom padding so the footer clears the
 * floating bar.
 */
@Composable
private fun FrnkAppSettingsTab(
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
    val onEffect = settingsEffects?.invoke(scope) ?: rememberShellSettingsHandler(scope, onboardingAvailable)

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
 * The shell's default Settings effect handler: applies appearance changes to the ambient
 * [LocalAppearanceController] and opens the built-in onboarding flow. Every other effect is a no-op
 * — `FrnkAppScaffold` (or a host-supplied `settingsEffects`) wires monetization, feedback, legal, …
 */
@Composable
private fun rememberShellSettingsHandler(
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
