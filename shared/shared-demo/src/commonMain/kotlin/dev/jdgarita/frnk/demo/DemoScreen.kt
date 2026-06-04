package dev.jdgarita.frnk.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.composables.icons.lucide.Component
import com.composables.icons.lucide.Lucide
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.ui.GOD_MODE_TOGGLE_ID
import dev.jdgarita.frnk.monetization.ui.frnkPaywallDestination
import dev.jdgarita.frnk.monetization.ui.rememberFrnkSettingsHandler
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBar
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBarState
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkButtonVariant
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.atoms.FrnkDividerState
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconButton
import dev.jdgarita.frnk.ui.atoms.FrnkIconButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControl
import dev.jdgarita.frnk.ui.atoms.FrnkSegmentedControlState
import dev.jdgarita.frnk.ui.atoms.FrnkSkeleton
import dev.jdgarita.frnk.ui.atoms.FrnkSwitch
import dev.jdgarita.frnk.ui.atoms.FrnkSwitchState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.bottomnav.FrnkAdaptiveBottomNavBar
import dev.jdgarita.frnk.ui.molecules.FrnkEmptyState
import dev.jdgarita.frnk.ui.molecules.FrnkEmptyStateState
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValue
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueOrientation
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueState
import dev.jdgarita.frnk.ui.molecules.FrnkListRow
import dev.jdgarita.frnk.ui.molecules.FrnkListRowState
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeAction
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeBehavior
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeDirection
import dev.jdgarita.frnk.ui.molecules.FrnkSwipeableState
import dev.jdgarita.frnk.ui.mvi.EffectCollector
import dev.jdgarita.frnk.ui.mvi.FrnkMviScreen
import dev.jdgarita.frnk.ui.nav.FrnkNavHost
import dev.jdgarita.frnk.ui.nav.FrnkNavOptions
import dev.jdgarita.frnk.ui.nav.frnkComposable
import dev.jdgarita.frnk.ui.nav.rememberFrnkNavController
import dev.jdgarita.frnk.ui.nav.rememberFrnkNavigator
import dev.jdgarita.frnk.ui.organisms.FrnkListSection
import dev.jdgarita.frnk.ui.organisms.FrnkListSectionState
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeader
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeaderState
import dev.jdgarita.frnk.ui.scaffolds.BottomNavTab
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.scaffolds.OnboardingEffect
import dev.jdgarita.frnk.ui.scaffolds.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.OnboardingScreen
import dev.jdgarita.frnk.ui.scaffolds.OnboardingScreenState
import dev.jdgarita.frnk.ui.scaffolds.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.SettingsEffect
import dev.jdgarita.frnk.ui.scaffolds.SettingsScreen
import dev.jdgarita.frnk.ui.scaffolds.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.SettingsToggleRowState
import dev.jdgarita.frnk.ui.scaffolds.rememberBottomNavScaffoldState
import dev.jdgarita.frnk.ui.scaffolds.rememberDefaultSettingsState
import dev.jdgarita.frnk.ui.scaffolds.rememberFeedbackEmailLauncher
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import dev.jdgarita.frnk.ui.theme.colorOnBackground
import dev.jdgarita.frnk.ui.theme.colorOnPrimaryContainer
import dev.jdgarita.frnk.ui.theme.colorOnSuccess
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSuccess
import dev.jdgarita.frnk.ui.theme.colorSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconBack
import dev.jdgarita.frnk.ui.theme.iconCheck
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.iconError
import dev.jdgarita.frnk.ui.theme.iconNotifications
import dev.jdgarita.frnk.ui.theme.iconPrivacy
import dev.jdgarita.frnk.ui.theme.iconRestore
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.iconUpgrade
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.rememberFrnkRipple
import dev.jdgarita.frnk.ui.theme.shapeCard
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import dev.jdgarita.frnk.utils.Frnk
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Smoke harness for the toolkit, structured as a real app would be on the toolkit navigation layer:
 * a host-owned [rememberFrnkNavController] drives a [FrnkNavHost] whose three tab roots are top-level
 * destinations, with a single floating [FrnkBottomNavBar] overlaid above the host so it persists
 * across tab swaps. Tabs (each keeping its own saved back stack via [DemoTabSwitchOptions]):
 *  - **Home** ([DemoRoute.Home]) — the toolkit showcase (theming + atoms, FeatureGate, MVI engine).
 *  - **Components** ([DemoRoute.Components]) — a gallery of every `Frnk*` atom (with a search field);
 *    tapping a row pushes [DemoRoute.ComponentDetail] (a type-safe `name` argument).
 *  - **Settings** ([DemoRoute.Settings]) — the real `SettingsScreen` scaffold; Onboarding is launched
 *    from here as a pushed [DemoRoute.Onboarding].
 *
 * Navigation is MVI-faithful: the shared [DemoViewModel] is resolved **once at this host scope** (so
 * its cross-tab state isn't forked per `NavBackStackEntry`), and its single one-shot effect stream is
 * consumed by **one** [EffectCollector] above the host that routes navigation into the `FrnkNavigator`
 * via [routeDemoEffect] and forwards the rest to the host's [onEffect]. On Android the system back
 * button and predictive-back gesture pop the `FrnkNavHost`'s back stack automatically; on iOS the
 * back-gesture support depends on the Compose Multiplatform runtime, so every pushed screen also
 * carries an on-screen back affordance (the detail/paywall back arrow, the onboarding close-X). The
 * only manual back handling left is closing the Components search field before a pop.
 *
 * The host integration story: a real app passes its own [dev.jdgarita.frnk.ui.theme.FrnkThemeConfig]
 * and binds a real [dev.jdgarita.frnk.monetization.EntitlementManager] (e.g. RevenueCat).
 */
@Composable
fun DemoScreen(onEffect: (DemoEffect) -> Unit = {}) {
    val vm: DemoViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    val navController = rememberFrnkNavController()
    val navigator = rememberFrnkNavigator(navController)

    // Single central collector for the shared VM's one-shot effects (the channel is single-consumer):
    // navigation effects drive the navigator; everything else is forwarded to the host. EffectCollector
    // is lifecycle-aware and rememberUpdatedState-wraps the handler, so it survives destination swaps.
    EffectCollector(vm.effects) { effect -> routeDemoEffect(effect, navigator, onEffect) }

    val appearanceController = LocalAppearanceController.current

    // Opens the platform mail composer prefilled with app + OS diagnostics. A real host passes its
    // own app name/version and may override `recipient` to route feedback to its own inbox.
    val sendFeedback =
        rememberFeedbackEmailLauncher(
            appName = "frnk",
            appVersion = "v${Frnk.VERSION}",
        )

    // Entry point #2: Settings rows (Upgrade / Restore / god-mode toggle) handled by the toolkit's
    // monetization wiring — navigation to the paywall, restore, and setGodMode all come for free; the
    // demo only supplies a fallback for the non-monetization effects (theme, onboarding, feedback).
    val entitlements: EntitlementManager = koinInject()
    val analytics: AnalyticsTracker = koinInject()
    val settingsHandler =
        rememberFrnkSettingsHandler(
            navigator = navigator,
            entitlements = entitlements,
            analytics = analytics,
            onMessage = { message -> onEffect(DemoEffect.Toast(message)) },
            fallback = { effect ->
                when (effect) {
                    is SettingsEffect.AppearanceChanged -> appearanceController.appearance = effect.appearance
                    is SettingsEffect.ActionInvoked ->
                        when (effect.action) {
                            SettingsAction.ShowOnboarding -> navigator.navigate(DemoRoute.Onboarding)
                            SettingsAction.SendFeedback -> sendFeedback()
                            else -> onEffect(DemoEffect.Toast("${effect.action} tapped"))
                        }
                    is SettingsEffect.ToggleChanged -> onEffect(DemoEffect.Toast("${effect.id} = ${effect.checked}"))
                }
            },
        )

    // Bottom-nav tabs and their routes — one source of truth, in the order rememberBottomNavScaffoldState
    // builds (Home, middle, Settings); tabRoutes pairs each tab with the route it switches to.
    val navState =
        rememberBottomNavScaffoldState(
            middleTab =
                BottomNavTab(
                    key = "components",
                    icon = Lucide.Component,
                    label = "Components",
                ),
        )
    val tabRoutes = remember { listOf<DemoRoute>(DemoRoute.Home, DemoRoute.Components, DemoRoute.Settings) }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    // The tab route that owns the current destination — drives the pill highlight (its index in
    // tabRoutes) and whether the bar shows at all. Pushed full screens (Onboarding / Paywall) own no
    // tab → null → the bar is hidden. ComponentDetail belongs to the Components tab: a flat nav graph
    // with this explicit mapping is a deliberate choice over nested per-tab graphs — with a single
    // pushed child it's simpler than a nested-graph DSL; revisit if pushed destinations multiply.
    val selectedTabRoute: DemoRoute? =
        currentDestination?.let { dest ->
            when {
                dest.hasRoute<DemoRoute.Home>() -> DemoRoute.Home
                dest.hasRoute<DemoRoute.Components>() || dest.hasRoute<DemoRoute.ComponentDetail>() -> DemoRoute.Components
                dest.hasRoute<DemoRoute.Settings>() -> DemoRoute.Settings
                else -> null
            }
        }
    val selectedTabIndex: Int? = selectedTabRoute?.let { route -> tabRoutes.indexOf(route).takeIf { it >= 0 } }

    // Destinations under the opaque adaptive bottom bar reserve its full height so the last item clears
    // it; full-screen pushes (Onboarding / Paywall) own no tab and hide the bar. The adaptive bar sits
    // ABOVE the system navigation bar (Material3 NavigationBar consumes WindowInsets.navigationBars), so
    // its real height is the bar body PLUS that inset — reserving only the body clips the last item on
    // devices with a non-zero bottom inset (3-button nav, etc.).
    val barInset = DemoBottomBarHeight + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Box(modifier = Modifier.fillMaxSize()) {
        FrnkNavHost(
            navController = navController,
            startRoute = DemoRoute.Home,
            modifier = Modifier.fillMaxSize(),
        ) {
            frnkComposable<DemoRoute.Home> {
                HomeTab(
                    vm = vm,
                    bottomInset = barInset,
                    onEffect = onEffect,
                )
            }
            frnkComposable<DemoRoute.Components> {
                ComponentsListScreen(
                    state = state,
                    onIntent = vm::send,
                    bottomInset = barInset,
                    onOpenComponent = { name -> navigator.navigate(DemoRoute.ComponentDetail(name)) },
                )
            }
            frnkComposable<DemoRoute.ComponentDetail> { route ->
                ComponentDetailScreen(
                    name = route.name,
                    bottomInset = barInset,
                    onBack = { navigator.popBackStack() },
                ) {
                    ComponentContent(route.name, state, vm::send, onEffect)
                }
            }
            frnkComposable<DemoRoute.Settings> {
                SettingsTab(
                    initialState = demoSettingsState(appearanceController.appearance, state.isPro, state.isGodMode),
                    bottomInset = barInset,
                    onEffect = settingsHandler,
                    // Re-seed the settings VM when entitlement state changes so the Subscription section
                    // swaps Upgrade↔Manage (and the god-mode toggle reflects the current value). The VM is
                    // seeded once via parametersOf, so without a fresh key it'd keep the stale initial state.
                    vmKey = "settings-${state.isPro}-${state.isGodMode}",
                )
            }
            frnkComposable<DemoRoute.Onboarding> {
                OnboardingScreen(
                    initialState = demoOnboardingState(),
                    modifier = Modifier.fillMaxSize(),
                    // A pushed destination gets a fresh NavBackStackEntry (and ViewModelStoreOwner) per
                    // push, so OnboardingScreen's koinViewModel is naturally fresh — no vmKey dance needed.
                    onEffect = { effect ->
                        when (effect) {
                            OnboardingEffect.CloseRequested,
                            OnboardingEffect.Completed,
                            -> navigator.popBackStack()
                        }
                    },
                )
            }
            // Entry points #1/#2/#3 all land here, on the toolkit-owned `ToolkitRoute.Paywall`. The
            // toolkit owns the paywall screen + VM (offerings, purchase/restore via the frnk
            // EntitlementManager); the demo just mounts the destination + handles close/messages. This is
            // the exact one-liner real hosts use (and the same route `rememberFrnkSettingsHandler` targets).
            frnkPaywallDestination(
                features =
                    listOf(
                        "Unlimited everything",
                        "No ads",
                        "Priority support",
                    ),
                source = "demo",
                onMessage = { message -> onEffect(DemoEffect.Toast(message)) },
                onClose = { navigator.popBackStack() },
            )
        }

        // The toolkit's platform-adaptive bottom bar (native UITabBar on iOS / Material3 NavigationBar on
        // Android), pinned at the bottom and persisting across tab swaps. Hidden on full-screen pushes
        // (Onboarding / Paywall, which own no tab). The demo wires its own navigation here — the lower-level
        // FrnkAdaptiveBottomNavBar override path; simpler hosts use FrnkAdaptiveBottomNavScaffold instead.
        if (selectedTabIndex != null) {
            FrnkAdaptiveBottomNavBar(
                items = navState.tabs.map { FrnkBottomNavItem(key = it.key, icon = it.icon, label = it.label) },
                selectedIndex = selectedTabIndex,
                onItemSelected = { index ->
                    val route = tabRoutes[index]
                    if (index == selectedTabIndex) {
                        // Re-tapping the active tab returns to its root, popping any pushed child
                        // (e.g. ComponentDetail) off this tab's stack rather than restoring it.
                        navigator.navigate(
                            route,
                            FrnkNavOptions(popUpTo = FrnkNavOptions.PopUpTo(route), launchSingleTop = true),
                        )
                    } else {
                        navigator.navigate(route, DemoTabSwitchOptions)
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
            )
        }
    }
}

// Body height of the opaque adaptive bottom bar (Material3 NavigationBar's 80.dp). The bottom
// system-bar/safe-area inset the bar sits above is added separately at the reserve site (`barInset`),
// via WindowInsets.navigationBars, so destinations reserve the bar's *full* occupied height.
private val DemoBottomBarHeight = 80.dp

/**
 * Home tab — the toolkit showcase. Dogfoods [FrnkMviScreen] (the state-hosting primitive host apps use)
 * over the shared [DemoViewModel]; `onEffect` is left null there because the demo's single effect stream
 * is already collected centrally in [DemoScreen] (the channel is single-consumer). The few direct toasts
 * below go through the captured [onEffect] lambda, which bypasses the VM channel.
 */
@Composable
private fun HomeTab(
    vm: DemoViewModel,
    bottomInset: Dp,
    onEffect: (DemoEffect) -> Unit,
) {
    val homeState by vm.state.collectAsState()
    val upgradeAction =
        FrnkTopAppBarAction(
            icon = Theme[icons][iconUpgrade],
            contentDescription = "Upgrade to Pro",
            key = "upgrade",
        )
    FrnkMviScreen(
        viewModel = vm,
        // Entry point #1: a top-right "Upgrade to Pro" action on Home, hidden once the user is Pro.
        topBar =
            FrnkTopAppBarState(
                title = "frnk",
                actions = if (homeState.isPro) emptyList() else listOf(upgradeAction),
            ),
        bottomInset = bottomInset,
        onActionClick = { action -> if (action.key == upgradeAction.key) vm.send(DemoIntent.RequestUpgrade) },
    ) { state, onIntent, padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding),
            verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
        ) {
            Section(title = "1. FeatureGate (Pro = ${state.isPro} via ${state.proSource})") {
                Column(verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                        // Entry point #1 mirror: open the toolkit paywall (also reachable from the
                        // top-right crown action and from Settings).
                        FrnkButton(
                            state = FrnkButtonState.Content(text = "Open Paywall"),
                            onClick = { onIntent(DemoIntent.RequestUpgrade) },
                        )
                        FrnkButton(
                            state =
                                FrnkButtonState.Content(
                                    text = "Restore",
                                    variant = FrnkButtonVariant.Outlined,
                                ),
                            onClick = { onIntent(DemoIntent.RestorePurchases) },
                        )
                    }
                    // God mode: a frnk-level Pro override (independent of RevenueCat), normally reached
                    // via Settings → tap version 7× → Developer. Surfaced here too for demo discoverability.
                    FrnkButton(
                        state =
                            FrnkButtonState.Content(
                                text = if (state.isGodMode) "God mode: ON" else "God mode: OFF",
                                variant = FrnkButtonVariant.Outlined,
                            ),
                        onClick = { onIntent(DemoIntent.ToggleGodMode) },
                    )
                }
            }

            FrnkDivider(state = FrnkDividerState.Horizontal())

            Section(title = "2. Persistence (FrnkDB — ${state.notes.size} saved)") {
                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text =
                                "NoteStore (shared-database-api) over SQLDelight's FrnkDB. The demo binds an " +
                                    "in-memory fake so DemoKit stays cinterop-free; the real driver path is " +
                                    "covered by NoteStoreRoundTripTest.",
                            color = colorOnSurfaceVariant,
                        ),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                    FrnkButton(
                        state = FrnkButtonState.Content(text = "Add note"),
                        onClick = { onIntent(DemoIntent.AddNote) },
                    )
                    FrnkButton(
                        state =
                            FrnkButtonState.Content(
                                text = "Clear",
                                variant = FrnkButtonVariant.Outlined,
                                enabled = state.notes.isNotEmpty(),
                            ),
                        onClick = { onIntent(DemoIntent.ClearNotes) },
                    )
                }
                if (state.notes.isEmpty()) {
                    FrnkText(
                        state =
                            FrnkTextState.BodySmall(
                                text = "No notes yet — tap Add note to persist one.",
                                color = colorOnSurfaceVariant,
                            ),
                    )
                } else {
                    state.notes.forEach { note ->
                        FrnkText(state = FrnkTextState.Body(text = "• $note"))
                    }
                }
            }

            FrnkDivider(state = FrnkDividerState.Horizontal())

            Section(title = "3. Analytics & Crash") {
                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text =
                                "AnalyticsTracker + CrashReporter (shared-backend-api), a backend-independent " +
                                    "axis (ObservabilityChoice). The demo binds logging fakes so DemoKit stays " +
                                    "SDK-free; androidDemoApp installs the real firebaseObservabilityModule.",
                            color = colorOnSurfaceVariant,
                        ),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                    FrnkButton(
                        state = FrnkButtonState.Content(text = "Track event"),
                        onClick = { onIntent(DemoIntent.TrackEvent) },
                    )
                    FrnkButton(
                        state = FrnkButtonState.Content(text = "User property", variant = FrnkButtonVariant.Outlined),
                        onClick = { onIntent(DemoIntent.SetUserProperty) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                    FrnkButton(
                        state = FrnkButtonState.Content(text = "Log breadcrumb", variant = FrnkButtonVariant.Outlined),
                        onClick = { onIntent(DemoIntent.LogBreadcrumb) },
                    )
                    FrnkButton(
                        state = FrnkButtonState.Content(text = "Record non-fatal", variant = FrnkButtonVariant.Outlined),
                        onClick = { onIntent(DemoIntent.RecordTestCrash) },
                    )
                }
                FrnkText(
                    state =
                        FrnkTextState.BodySmall(
                            text =
                                "Force crash throws an UNHANDLED Kotlin exception — on iOS the CrashKiOS hook " +
                                    "(installed with ObservabilityChoice.Firebase) reports it symbolicated; on Android " +
                                    "the Crashlytics SDK catches it. This terminates the app.",
                            color = colorOnSurfaceVariant,
                        ),
                )
                FrnkButton(
                    state = FrnkButtonState.Content(text = "Force crash (unhandled)", variant = FrnkButtonVariant.Outlined),
                    onClick = { onIntent(DemoIntent.ForceUnhandledCrash) },
                )
            }

            FrnkDivider(state = FrnkDividerState.Horizontal())

            Section(title = "4. MVI + Navigation") {
                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text =
                                "DemoViewModel = MviViewModel<DemoState, DemoIntent, DemoEffect>.\n" +
                                    "Every interaction above flows: Composable → send(Intent) → reducer → State → " +
                                    "recomposition. Navigation is a one-shot effect routed into the toolkit's " +
                                    "FrnkNavHost — Request Upgrade pushes the Paywall; the bottom bar switches tabs.",
                            color = colorOnSurfaceVariant,
                        ),
                )
            }
        }
    }
}

/**
 * Components tab list — every `Frnk*` atom by name under a searchable top bar. Tapping a row pushes
 * that component's detail destination via [onOpenComponent]. All state (search + the interactive atoms'
 * values) is hoisted into [DemoViewModel]; this composable is stateless. Physical/gesture back closes an
 * open search field, otherwise it falls through to the `FrnkNavHost` (which pops the tab back to Home).
 */
@Composable
private fun ComponentsListScreen(
    state: DemoState,
    onIntent: (DemoIntent) -> Unit,
    bottomInset: Dp,
    onOpenComponent: (String) -> Unit,
) {
    val searchActive = state.searchActive
    val query = state.searchQuery

    val trimmedQuery = query.trim()
    val matches = componentNames.filter { it.contains(trimmedQuery, ignoreCase = true) }

    // Only intercept back to close an open search field; when inactive the FrnkNavHost handles back.
    DemoBackHandler(enabled = searchActive) { onIntent(DemoIntent.SearchClosed) }

    FrnkScreenScaffold(
        topBar =
            FrnkTopAppBarState(
                title = "Components",
                actions =
                    listOf(
                        FrnkTopAppBarAction(icon = Theme[icons][iconSearch], contentDescription = "Search"),
                    ),
                isSearchActive = searchActive,
                searchQuery = query,
                searchPlaceholder = "Search components",
            ),
        bottomInset = bottomInset,
        onActionClick = { onIntent(DemoIntent.SearchOpened) },
        onSearchQueryChange = { onIntent(DemoIntent.SearchQueryChanged(it)) },
        onSearchClose = { onIntent(DemoIntent.SearchClosed) },
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding),
        ) {
            if (matches.isEmpty()) {
                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text = "No components match \"$trimmedQuery\".",
                            color = colorOnSurfaceVariant,
                        ),
                )
            }

            matches.forEachIndexed { index, name ->
                if (index > 0) {
                    FrnkDivider(state = FrnkDividerState.Horizontal())
                }
                ComponentRow(name = name, onClick = { onOpenComponent(name) })
            }
        }
    }
}

/**
 * A tappable row in the Components list: the component's name with a trailing chevron. Tapping it pushes
 * that component's [ComponentDetailScreen].
 */
@Composable
private fun ComponentRow(
    name: String,
    onClick: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = FrnkSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FrnkText(
            state = FrnkTextState.TitleMedium(text = name),
            modifier = Modifier.weight(1f),
        )
        FrnkIcon(
            state =
                FrnkIconState.Content(
                    imageVector = Theme[icons][iconChevronRight],
                    contentDescription = null,
                    size = FrnkIconSize.md,
                    tint = colorOnSurfaceVariant,
                ),
        )
    }
}

/**
 * Pushed detail destination for a single component — its name in the top bar over a scrollable list of
 * all that component's variants. Uses the same [FrnkScreenScaffold] template as every other screen.
 * Back is handled by the `FrnkNavHost` (system back / swipe-back pop the stack automatically); the top
 * bar's back arrow calls [onBack].
 */
@Composable
private fun ComponentDetailScreen(
    name: String,
    bottomInset: Dp,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    FrnkScreenScaffold(
        topBar =
            FrnkTopAppBarState(
                title = name,
                navigationIcon = Theme[icons][iconBack],
                navigationContentDescription = "Back",
            ),
        bottomInset = bottomInset,
        onNavigationClick = onBack,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding),
            verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
        ) {
            content()
        }
    }
}

/**
 * Settings tab — a top bar over the real [SettingsScreen] scaffold (no back arrow: it's a tab root,
 * switched via the bottom bar). [initialState] uses a blank title so the bar's "Settings" heading
 * isn't doubled.
 */
@Composable
private fun SettingsTab(
    initialState: SettingsScreenState,
    bottomInset: Dp,
    onEffect: (SettingsEffect) -> Unit,
    vmKey: String? = null,
) {
    FrnkScreenScaffold(
        topBar = FrnkTopAppBarState(title = "Settings"),
        // SettingsScreenContent paints its own colorBackground (so it works standalone too), so let the
        // scaffold's backdrop be transparent here to avoid a redundant full-screen overdraw.
        containerColor = Color.Transparent,
        bottomInset = bottomInset,
        // Extra bottom padding so the footer/version clears the floating bottom nav bar with breathing
        // room (the default lg leaves it sitting right on the bar; xl gives a more comfortable gap).
        contentPadding =
            PaddingValues(
                start = FrnkSpacing.lg,
                top = FrnkSpacing.lg,
                end = FrnkSpacing.lg,
                bottom = FrnkSpacing.xl,
            ),
    ) { padding ->
        SettingsScreen(
            initialState = initialState,
            modifier = Modifier.fillMaxSize(),
            vmKey = vmKey,
            contentPadding = padding,
            onEffect = onEffect,
        )
    }
}

/**
 * The Components catalog in display order. The list destination renders these names; the detail
 * destination resolves one name's content via [ComponentContent]. Single source of truth for both.
 */
private val componentNames =
    listOf(
        "FrnkText",
        "FrnkButton",
        "FrnkIcon / FrnkIconButton",
        "FrnkDivider",
        "FrnkSwitch",
        "FrnkSegmentedControl",
        "FrnkBottomNavBar",
        "Ripple",
        "FrnkListRow",
        "FrnkSwipeable",
        "FrnkLabeledValue",
        "FrnkEmptyState",
        "FrnkListSection",
        "FrnkProfileHeader",
    )

/**
 * Renders the variants for a single component [name] — the body of [ComponentDetailScreen]. A `when`
 * over the catalog so the detail destination composes **only** the requested component's content
 * (not the whole gallery). Reads interactive values from [state] and dispatches [onIntent] / [onEffect],
 * so the atoms stay live. Unknown names render a fallback rather than crash.
 */
@Composable
private fun ComponentContent(
    name: String,
    state: DemoState,
    onIntent: (DemoIntent) -> Unit,
    onEffect: (DemoEffect) -> Unit,
) {
    when (name) {
        "FrnkText" -> {
            FrnkText(state = FrnkTextState.HeadlineSmall(text = "HeadlineSmall"))
            FrnkText(state = FrnkTextState.Title(text = "Title"))
            FrnkText(state = FrnkTextState.TitleMedium(text = "TitleMedium"))
            FrnkText(state = FrnkTextState.Body(text = "Body"))
            FrnkText(state = FrnkTextState.BodyMedium(text = "BodyMedium"))
            FrnkText(
                state = FrnkTextState.BodySmall(text = "BodySmall", color = colorOnSurfaceVariant),
            )
            FrnkText(state = FrnkTextState.AppName(annotated = buildAnnotatedString { append("FrnkKit") }))
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkText(
                state = FrnkTextState.Title(text = "Loading title", skeleton = FrnkSkeleton(enabled = true)),
            )
            FrnkText(
                state =
                    FrnkTextState.Body(
                        text = "Loading a longer body line of text",
                        skeleton = FrnkSkeleton(enabled = true),
                    ),
            )
        }
        "FrnkButton" -> {
            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                FrnkButton(state = FrnkButtonState.Content(text = "Filled"), onClick = {})
                FrnkButton(
                    state = FrnkButtonState.Content(text = "Outlined", variant = FrnkButtonVariant.Outlined),
                    onClick = {},
                )
                FrnkButton(
                    state = FrnkButtonState.Content(text = "Ghost", variant = FrnkButtonVariant.Ghost),
                    onClick = {},
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                FrnkButton(
                    state = FrnkButtonState.Content(text = "Filled", enabled = false),
                    onClick = {},
                )
                FrnkButton(
                    state =
                        FrnkButtonState.Content(
                            text = "Outlined",
                            variant = FrnkButtonVariant.Outlined,
                            enabled = false,
                        ),
                    onClick = {},
                )
            }
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                FrnkButton(
                    state = FrnkButtonState.Skeleton,
                    onClick = {},
                )
                FrnkButton(
                    state =
                        FrnkButtonState.Skeleton,
                    onClick = {},
                )
            }
        }
        "FrnkIcon / FrnkIconButton" -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FrnkIcon(
                    state =
                        FrnkIconState.Content(
                            imageVector = Theme[icons][iconSearch],
                            contentDescription = "Search",
                            size = FrnkIconSize.md,
                            tint = colorPrimary,
                        ),
                )
                FrnkIcon(
                    state =
                        FrnkIconState.Content(
                            imageVector = Theme[icons][iconCheck],
                            contentDescription = "Check",
                            size = FrnkIconSize.lg,
                            tint = colorPrimary,
                        ),
                )
                FrnkIconButton(
                    state =
                        FrnkIconButtonState.Content(
                            imageVector = Theme[icons][iconSettings],
                            contentDescription = "Settings",
                            tint = colorOnBackground,
                        ),
                    onClick = { onEffect(DemoEffect.Toast("Icon button tapped")) },
                )
            }
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            Row(
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FrnkIcon(
                    state =
                        FrnkIconState.Skeleton(),
                )
                FrnkIconButton(
                    state =
                        FrnkIconButtonState.Skeleton,
                    onClick = {},
                )
            }
        }
        "FrnkDivider" -> {
            FrnkDivider(state = FrnkDividerState.Horizontal())
            Row(
                modifier = Modifier.height(24.dp),
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FrnkText(state = FrnkTextState.BodySmall(text = "Left"))
                FrnkDivider(state = FrnkDividerState.Vertical())
                FrnkText(state = FrnkTextState.BodySmall(text = "Right"))
            }
        }
        "FrnkSwitch" -> {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FrnkSwitch(
                    state = FrnkSwitchState.Content(checked = state.gallerySwitchOn),
                    onCheckedChange = { onIntent(DemoIntent.GallerySwitchChanged(it)) },
                )
                FrnkText(state = FrnkTextState.BodySmall(text = if (state.gallerySwitchOn) "On" else "Off"))
                FrnkSwitch(
                    state = FrnkSwitchState.Content(checked = true, enabled = false),
                    onCheckedChange = {},
                )
                FrnkText(state = FrnkTextState.BodySmall(text = "Disabled"))
            }
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkSwitch(
                state = FrnkSwitchState.Skeleton,
                onCheckedChange = {},
            )
        }
        "FrnkSegmentedControl" -> {
            FrnkSegmentedControl(
                state =
                    FrnkSegmentedControlState.Content(
                        options = listOf("One", "Two", "Three"),
                        selectedIndex = state.gallerySegmentIndex,
                    ),
                onOptionSelected = { onIntent(DemoIntent.GallerySegmentChanged(it)) },
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkSegmentedControl(
                state =
                    FrnkSegmentedControlState.Skeleton,
                onOptionSelected = {},
            )
        }
        "FrnkBottomNavBar" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text = "The atom standalone — the bar at the foot of this screen is the same atom.",
                        color = colorOnSurfaceVariant,
                    ),
            )
            FrnkBottomNavBar(
                state =
                    FrnkBottomNavBarState(
                        items =
                            listOf(
                                FrnkBottomNavItem("a", Theme[icons][iconSearch], "Search"),
                                FrnkBottomNavItem("b", Theme[icons][iconCheck], "Check"),
                                FrnkBottomNavItem("c", Theme[icons][iconSettings], "Settings"),
                            ),
                        selectedIndex = state.galleryNavIndex,
                    ),
                onItemSelected = { onIntent(DemoIntent.GalleryNavChanged(it)) },
            )
        }
        "Ripple" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Every interactive atom above ripples on press by default — FrnkTheme installs " +
                                "the ripple as LocalIndication. Host apps apply the same ripple to their own " +
                                "components with rememberFrnkRipple().",
                        color = colorOnSurfaceVariant,
                    ),
            )
            val boundedRipple = remember { MutableInteractionSource() }
            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(Theme[shapes][shapeCard])
                        .background(Theme[colors][colorSurfaceVariant])
                        .clickable(
                            interactionSource = boundedRipple,
                            indication = rememberFrnkRipple(),
                        ) { onEffect(DemoEffect.Toast("Bounded ripple")) }
                        .padding(FrnkSpacing.md),
            ) {
                FrnkText(state = FrnkTextState.Body(text = "Custom card — bounded ripple (content color)"))
            }
            val unboundedRipple = remember { MutableInteractionSource() }
            Box(
                modifier =
                    Modifier
                        .clip(Theme[shapes][shapeCard])
                        .clickable(
                            interactionSource = unboundedRipple,
                            indication = rememberFrnkRipple(color = Theme[colors][colorPrimary], bounded = false),
                        ) { onEffect(DemoEffect.Toast("Unbounded ripple")) }
                        .padding(FrnkSpacing.md),
            ) {
                FrnkText(state = FrnkTextState.Body(text = "Tap for an unbounded, primary-colored ripple"))
            }
        }
        "FrnkListRow" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Molecule: leading icon + title/subtitle + trailing slot. Tap a row for a " +
                                "ripple + haptic; the whole row collapses to a skeleton while loading.",
                        color = colorOnSurfaceVariant,
                    ),
            )
            FrnkListRow(
                state =
                    FrnkListRowState.Content(
                        title = "Notifications",
                        subtitle = "Push, email and in-app alerts",
                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null),
                    ),
                onClick = { onEffect(DemoEffect.Toast("Tapped Notifications")) },
                trailing = {
                    FrnkIcon(
                        state =
                            FrnkIconState.Content(
                                imageVector = Theme[icons][iconChevronRight],
                                contentDescription = null,
                                tint = colorOnSurfaceVariant,
                            ),
                    )
                },
            )
            FrnkListRow(
                state = FrnkListRowState.Content(title = "Title only, non-interactive"),
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkListRow(
                state =
                    FrnkListRowState.Skeleton,
                onClick = {},
            )
        }
        "FrnkSwipeable" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Optional swipe-to-action on any row. Reveal holds open a row of buttons " +
                                "(tap one, or tap the row to close); Dismiss fires on release past the " +
                                "threshold then snaps back. Headless — no Material3.",
                        color = colorOnSurfaceVariant,
                    ),
            )
            val deleteAction =
                FrnkSwipeAction(
                    icon = FrnkIconState.Content(Theme[icons][iconError], contentDescription = "Delete"),
                    label = "Delete",
                )
            val archiveAction =
                FrnkSwipeAction(
                    icon = FrnkIconState.Content(Theme[icons][iconRestore], contentDescription = "Archive"),
                    containerColor = colorSuccess,
                    contentColor = colorOnSuccess,
                    label = "Archive",
                )
            FrnkText(state = FrnkTextState.BodySmall(text = "Reveal (drag left)", color = colorOnSurfaceVariant))
            FrnkListRow(
                state =
                    FrnkListRowState.Content(
                        title = "Project Apollo",
                        subtitle = "Swipe left to reveal actions",
                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null),
                    ),
                onClick = { onEffect(DemoEffect.Toast("Tapped Project Apollo")) },
                swipe =
                    FrnkSwipeableState(
                        behavior = FrnkSwipeBehavior.Reveal,
                        direction = FrnkSwipeDirection.Right,
                        rightActions = listOf(archiveAction, deleteAction),
                    ),
                onSwipeAction = { onEffect(DemoEffect.Toast("${it.key} (reveal)")) },
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Dismiss (drag left)", color = colorOnSurfaceVariant))
            FrnkListRow(
                state =
                    FrnkListRowState.Content(
                        title = "Swipe-to-delete",
                        subtitle = "Release past the threshold to fire",
                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null),
                    ),
                swipe =
                    FrnkSwipeableState(
                        behavior = FrnkSwipeBehavior.Dismiss,
                        direction = FrnkSwipeDirection.Right,
                        rightActions = listOf(deleteAction),
                    ),
                onSwipeAction = { onEffect(DemoEffect.Toast("${it.key} (dismiss)")) },
            )
        }
        "FrnkLabeledValue" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Molecule: a muted label paired with a value. Inline pushes the value to the " +
                                "end; Stacked sits it below. The value carries the skeleton while loading.",
                        color = colorOnSurfaceVariant,
                    ),
            )
            FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Plan", value = "Pro"))
            FrnkDivider(state = FrnkDividerState.Horizontal())
            FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Renews", value = "Jun 2026"))
            FrnkDivider(state = FrnkDividerState.Horizontal())
            FrnkLabeledValue(
                state =
                    FrnkLabeledValueState.Content(
                        label = "Storage used",
                        value = "4.2 GB",
                        orientation = FrnkLabeledValueOrientation.Stacked,
                    ),
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkLabeledValue(
                state =
                    FrnkLabeledValueState.Skeleton,
            )
        }
        "FrnkEmptyState" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Molecule: centered icon + title + subtitle + optional action button. A terminal " +
                                "zero-content state, so it has no skeleton (you'd skeletonize the eventual content " +
                                "instead). The action button brings its own ripple + haptic.",
                        color = colorOnSurfaceVariant,
                    ),
            )
            FrnkEmptyState(
                state =
                    FrnkEmptyStateState(
                        icon =
                            FrnkIconState.Content(
                                imageVector = Theme[icons][iconSearch],
                                contentDescription = null,
                                size = FrnkIconSize.emptyState,
                                tint = colorOnSurfaceVariant,
                            ),
                        title = "No results",
                        subtitle = "Try adjusting your search to find what you're looking for.",
                        actionLabel = "Clear search",
                    ),
                onActionClick = { onEffect(DemoEffect.Toast("Cleared search")) },
            )
        }
        "FrnkListSection" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Organism: an optional title + a surface card stacking FrnkListRow molecules " +
                                "separated by dividers, animating its height as rows change. Tap a row for a " +
                                "ripple + haptic; per-row skeletons collapse each row independently.",
                        color = colorOnSurfaceVariant,
                    ),
            )
            val sectionRows =
                listOf(
                    FrnkListRowState.Content(
                        title = "Notifications",
                        subtitle = "Push, email and in-app alerts",
                        icon = FrnkIconState.Content(Theme[icons][iconNotifications], contentDescription = null),
                    ),
                    FrnkListRowState.Content(
                        title = "Preferences",
                        subtitle = "Theme, language and units",
                        icon = FrnkIconState.Content(Theme[icons][iconSettings], contentDescription = null),
                    ),
                )
            FrnkListSection(
                state =
                    FrnkListSectionState(
                        title = "Account",
                        rows = sectionRows,
                        footnote = "Manage how you're notified across devices.",
                    ),
                onRowClick = { index -> onEffect(DemoEffect.Toast("Tapped row $index")) },
                trailing = {
                    FrnkIcon(
                        state =
                            FrnkIconState.Content(
                                imageVector = Theme[icons][iconChevronRight],
                                contentDescription = null,
                                tint = colorOnSurfaceVariant,
                            ),
                    )
                },
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkListSection(
                state =
                    FrnkListSectionState(
                        title = "Account",
                        rows = List(3) { FrnkListRowState.Skeleton },
                    ),
            )
        }
        "FrnkProfileHeader" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "Organism: a circular avatar + name/subtitle, with an even row of " +
                                "FrnkLabeledValue stat tiles below. The skeleton flag passes through to every " +
                                "child (avatar, name, subtitle, each stat value).",
                        color = colorOnSurfaceVariant,
                    ),
            )
            val avatar =
                FrnkIconState.Content(
                    imageVector = Theme[icons][iconSettings],
                    contentDescription = null,
                    size = FrnkIconSize.lg,
                    tint = colorOnPrimaryContainer,
                )
            val stats =
                listOf(
                    FrnkLabeledValueState.Content(label = "Projects", value = "12"),
                    FrnkLabeledValueState.Content(label = "Streak", value = "48d"),
                    FrnkLabeledValueState.Content(label = "Plan", value = "Pro"),
                )
            FrnkProfileHeader(
                state =
                    FrnkProfileHeaderState.Content(
                        name = "Juan Diego",
                        subtitle = "juandiego@example.com",
                        avatar = avatar,
                        stats = stats,
                    ),
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "No stats", color = colorOnSurfaceVariant))
            FrnkProfileHeader(
                state =
                    FrnkProfileHeaderState.Content(
                        name = "Juan Diego",
                        subtitle = "Free plan",
                        avatar = avatar,
                    ),
            )
            FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
            FrnkProfileHeader(
                state =
                    FrnkProfileHeaderState.Skeleton,
            )
        }
        else ->
            FrnkText(
                state =
                    FrnkTextState.Body(
                        text = "Unknown component \"$name\".",
                        color = colorOnSurfaceVariant,
                    ),
            )
    }
}

/**
 * The demo's Settings state: the toolkit's default catalog (blank title — the top bar already shows
 * "Settings") extended with demo-only Preferences/Account sections so the screen overflows the viewport
 * on tall devices (otherwise it fits and the collapsing bars never engage).
 */
@Composable
private fun demoSettingsState(
    appearance: dev.jdgarita.frnk.ui.theme.Appearance,
    isPro: Boolean,
    isGodMode: Boolean,
): SettingsScreenState {
    val baseSettings =
        rememberDefaultSettingsState(
            version = "v${Frnk.VERSION}",
            appearance = appearance,
            isPro = isPro,
            title = "",
        )
    val extraSettings = demoExtraSettingsSections()
    val godModeIcon = Theme[icons][iconUpgrade]
    // "Developer" section holding the god-mode toggle. The demo always shows it (showDeveloperSection =
    // true) so it stays stable across the isPro/isGodMode VM re-key (toggling god mode flips isPro, which
    // re-seeds the settings VM). Real apps can instead leave it hidden and use the 7-tap version-footer
    // reveal gesture (the toolkit supports both).
    val developerSection =
        remember(godModeIcon, isGodMode) {
            SettingsSectionState(
                title = "Developer",
                rows =
                    listOf(
                        SettingsToggleRowState(
                            id = GOD_MODE_TOGGLE_ID,
                            icon = FrnkIconState.Content(imageVector = godModeIcon, contentDescription = null, tint = colorPrimary),
                            title = "God mode",
                            subtitle = "Force Pro on this device (testing)",
                            checked = isGodMode,
                        ),
                    ),
            )
        }
    return remember(baseSettings, extraSettings, developerSection) {
        baseSettings.copy(
            sections = baseSettings.sections + extraSettings,
            developerSection = developerSection,
            showDeveloperSection = true,
        )
    }
}

/**
 * Demo-only extra Settings sections appended to the default catalog. Their sole purpose is to push the
 * Settings screen's content beyond the viewport on tall devices so the collapsing top/bottom bars
 * actually engage there — the toolkit's default catalog alone fits on a large phone. Toggles and rows
 * are fully wired through the [SettingsScreen] ViewModel (they toast via the demo's effect handler).
 */
@Composable
private fun demoExtraSettingsSections(): List<SettingsSectionState> {
    // Haptic feedback lives in the toolkit's *default* catalog now (a real `LocalFrnkHaptics`-backed
    // toggle), so the demo no longer hand-rolls a haptics row — it just appends a demo-only Privacy
    // section to keep the Settings screen long enough that the collapsing bars engage.
    val analyticsIcon = Theme[icons][iconPrivacy]
    return remember(analyticsIcon) {
        fun rowIcon(vector: ImageVector) = FrnkIconState.Content(imageVector = vector, contentDescription = null, tint = colorPrimary)
        listOf(
            SettingsSectionState(
                title = "Privacy",
                rows =
                    listOf(
                        SettingsToggleRowState(
                            id = "analytics",
                            icon = rowIcon(analyticsIcon),
                            title = "Share analytics",
                            subtitle = "Help improve the app",
                            checked = true,
                        ),
                    ),
            ),
        )
    }
}

@Composable
private fun demoOnboardingState(): OnboardingScreenState {
    // Resolve icon tokens once per composition; keys are stable across recompositions unless
    // the host swaps its FrnkThemeConfig.iconOverrides, so remember rarely invalidates.
    val checkIcon = Theme[icons][iconCheck]
    val searchIcon = Theme[icons][iconSearch]
    val settingsIcon = Theme[icons][iconSettings]
    return remember(checkIcon, searchIcon, settingsIcon) {
        OnboardingScreenState(
            pages =
                listOf(
                    OnboardingPageState(
                        title = FrnkTextState.Title(text = "Welcome to Frnk"),
                        description =
                            FrnkTextState.Body(
                                text = "A Kotlin Multiplatform toolkit to ship polished apps in days, not weeks.",
                            ),
                        icon =
                            FrnkIconState.Content(
                                imageVector = checkIcon,
                                contentDescription = null,
                                size = FrnkIconSize.xxl,
                                tint = colorPrimary,
                            ),
                    ),
                    OnboardingPageState(
                        title = FrnkTextState.Title(text = "Search everything"),
                        description =
                            FrnkTextState.Body(
                                text = "Typed, paginated, offline-ready data access across every source.",
                            ),
                        icon =
                            FrnkIconState.Content(
                                imageVector = searchIcon,
                                contentDescription = null,
                                size = FrnkIconSize.xxl,
                                tint = colorPrimary,
                            ),
                    ),
                    OnboardingPageState(
                        title = FrnkTextState.Title(text = "Ready when you are"),
                        description =
                            FrnkTextState.Body(text = "Tap Get Started to begin your first session."),
                        icon =
                            FrnkIconState.Content(
                                imageVector = settingsIcon,
                                contentDescription = null,
                                size = FrnkIconSize.xxl,
                                tint = colorPrimary,
                            ),
                    ),
                ),
        )
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
        FrnkText(state = FrnkTextState.Title(text = title))
        content()
    }
}

/**
 * Bridges the platform back signal — the Android system back button + predictive-back gesture, and the
 * iOS interactive swipe-back — to an in-app action. The `FrnkNavHost` already pops its back stack on
 * system back, so this is now used in exactly one place: intercepting back to close the Components
 * search field (when [enabled]) before it would otherwise pop the destination.
 *
 * Confines the opt-in for the still-`@ExperimentalComposeUiApi` — and, as of Compose Multiplatform
 * 1.11, soft-deprecated in favour of `androidx.navigationevent`'s `NavigationEventHandler` —
 * [BackHandler] to this one place.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Suppress("DEPRECATION")
@Composable
private fun DemoBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit,
) {
    BackHandler(enabled = enabled, onBack = onBack)
}
