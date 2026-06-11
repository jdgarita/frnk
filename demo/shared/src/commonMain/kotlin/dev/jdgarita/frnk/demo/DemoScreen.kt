package dev.jdgarita.frnk.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.Component
import com.composables.icons.lucide.Crown
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Search
import com.composables.icons.lucide.Settings
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.backend.AnalyticsTracker
import dev.jdgarita.frnk.demo.generated.resources.Res
import dev.jdgarita.frnk.demo.generated.resources.frnk_demo_components
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.ui.FrnkPaywallDestination
import dev.jdgarita.frnk.monetization.ui.GOD_MODE_TOGGLE_ID
import dev.jdgarita.frnk.monetization.ui.rememberFrnkSettingsHandler
import dev.jdgarita.frnk.ui.app.FrnkAppScope
import dev.jdgarita.frnk.ui.app.FrnkAppShell
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
import dev.jdgarita.frnk.ui.bottomnav.FrnkAdaptiveNavEngine
import dev.jdgarita.frnk.ui.bottomnav.FrnkAdaptiveNavTab
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
import dev.jdgarita.frnk.ui.nav.ToolkitRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.navigateTo
import dev.jdgarita.frnk.ui.organisms.FrnkListSection
import dev.jdgarita.frnk.ui.organisms.FrnkListSectionState
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeader
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeaderState
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.scaffolds.HomeEffect
import dev.jdgarita.frnk.ui.scaffolds.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.SettingsEffect
import dev.jdgarita.frnk.ui.scaffolds.SettingsScreen
import dev.jdgarita.frnk.ui.scaffolds.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.SettingsToggleRowState
import dev.jdgarita.frnk.ui.scaffolds.rememberDefaultSettingsState
import dev.jdgarita.frnk.ui.scaffolds.rememberFeedbackEmailLauncher
import dev.jdgarita.frnk.ui.theme.AppearanceController
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Smoke harness for the toolkit — and the reference integration of **[FrnkAppShell]**, the one-call
 * app root. The shell owns the theme wrap, the nav3 saved-state config, the Home + Components +
 * Settings adaptive tabs with per-tab back stacks, the persistent bottom bar (tab switching, back
 * conventions, full-screen hiding, bottom-inset), the built-in Home / Settings / Onboarding
 * destinations, and the primary-action registry; the demo supplies only its content:
 *  - **Home** (`ToolkitRoute.Home`, the shell's built-in `HomeScreen`) — the toolkit showcase via
 *    [HomeTabContent] in the `homeContent` slot; the crown Upgrade action + the bar's primary-action
 *    button arrive as `HomeEffect`s.
 *  - **Components** ([DemoRoute.Components], the demo's middle tab) — a gallery of every `Frnk*`
 *    atom; tapping a row pushes [DemoRoute.ComponentDetail] (a type-safe `name` argument).
 *  - **Settings** (`ToolkitRoute.Settings`, the shell's built-in tab) — the default catalogue with
 *    the demo's extra sections injected via `extraSections`, effects handled by
 *    [demoSettingsHandler] (the toolkit monetization wiring + demo fallbacks).
 *
 * Navigation stays MVI-faithful: the shared [DemoViewModel] is resolved **once at this host scope**,
 * and its single one-shot effect stream is consumed by **one** [EffectCollector] in the shell's
 * `effects` slot, routing navigation via `scope.navigateTo(route)` ([routeDemoEffect]) and forwarding
 * the rest to the host's [onEffect]. On Android system/predictive back pops automatically; on iOS
 * every pushed screen also carries an on-screen back affordance. The only manual back handling left
 * is closing the Components search field before a pop.
 *
 * The host integration story: a real app passes its own [dev.jdgarita.frnk.ui.theme.FrnkThemeConfig]
 * and binds a real [dev.jdgarita.frnk.monetization.EntitlementManager] (e.g. RevenueCat). A host that
 * *can* depend on `:shared` uses `FrnkAppScaffold` instead, which layers the Koin assertion + live
 * entitlement-driven Settings + auto-mounted paywall over this same shell.
 *
 * **Bottom-bar A/B (POC).** A segmented control on the Home tab flips `state.navEngine` between
 * `FrnkAdaptiveNavEngine.Calf` (native iOS `UITabBar`, no primary-action button) and `.AdaptiveNavBar`
 * (with the built-in primary-action button — FAB on Android / inline on iOS). The button is
 * **screen-routed** through the shell's registry: the Home tab claims it (`homePrimaryActionEnabled`),
 * so it shows there and hides on the other tabs with no host-level conditional. See
 * `docs/spikes/adaptive-bottom-nav.md`; note the Android resource-packaging workaround in
 * `demo/android-app/src/main/assets/composeResources/`.
 */
@Composable
fun DemoScreen(
    appearanceController: AppearanceController? = null,
    onEffect: (DemoEffect) -> Unit = {},
) {
    val vm: DemoViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    // The demo's only remaining host routes: the middle "Components" tab root + its pushed detail.
    // Home / Settings / Onboarding / Paywall are the toolkit-owned ToolkitRoute defaults now.
    val hostRoutes =
        remember {
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(DemoRoute.Components::class, DemoRoute.Components.serializer())
                    subclass(DemoRoute.ComponentDetail::class, DemoRoute.ComponentDetail.serializer())
                }
            }
        }
    // One declaration per tab, carrying BOTH icon forms so the same list feeds either bar engine in the
    // A/B (the ImageVector for Calf, plus a DrawableResource + SF-Symbol for adaptive-nav-bar). The
    // shell supplies the Home/Settings bookends (icons/labels from theme tokens + the toolkit's bundled
    // resources); the host only adds its middle "Components" tab, bundling its own resource icon
    // (frnk_demo_components) the way a real host would.
    val middleTabs =
        remember {
            listOf(
                FrnkAdaptiveNavTab(
                    key = "components",
                    root = DemoRoute.Components,
                    label = "Components",
                    icon = Lucide.Component,
                    androidIcon = Res.drawable.frnk_demo_components,
                    iosSystemIcon = "square.grid.2x2",
                ),
            )
        }

    // Entry point #1: a top-right "Upgrade to Pro" action on Home, hidden once the user is Pro. Built
    // with a plain Lucide icon — DemoScreen sits outside the theme (FrnkAppShell installs it).
    val homeTopBar =
        remember(state.isPro) {
            FrnkTopAppBarState(
                title = "frnk",
                actions =
                    if (state.isPro) {
                        emptyList()
                    } else {
                        listOf(FrnkTopAppBarAction(icon = Lucide.Crown, contentDescription = "Upgrade to Pro", key = "upgrade"))
                    },
            )
        }

    FrnkAppShell(
        appVersion = "v${Frnk.VERSION}",
        modifier = Modifier.fillMaxSize(),
        themeConfig = demoPurpleThemeConfig(),
        appearanceController = appearanceController,
        middleTabs = middleTabs,
        hostRoutes = hostRoutes,
        // POC A/B: the engine is host-state, flipped live from the segmented control on the Home tab.
        engine = state.navEngine,
        homeTopBar = homeTopBar,
        // The Home VM is seeded once via parametersOf; re-key it when isPro flips so the Upgrade
        // action appears/disappears (same trick as the Settings VM below).
        homeVmKey = "home-${state.isPro}",
        // The Home tab claims the bar's primary-action button (adaptive-nav-bar engine only) through
        // the registry — replacing the old `tabbed.currentTabKey` conditional at the host root. The
        // other tabs hold no claim, so the button hides there automatically.
        homePrimaryActionEnabled = true,
        onHomeEffect = { effect ->
            when (effect) {
                is HomeEffect.ActionInvoked -> if (effect.key == "upgrade") vm.send(DemoIntent.RequestUpgrade)
                HomeEffect.PrimaryActionInvoked -> onEffect(DemoEffect.Toast("New item tapped"))
                HomeEffect.NavigationInvoked -> Unit
            }
        },
        settingsState = { _ ->
            demoSettingsState(LocalAppearanceController.current.appearance, state.isPro, state.isGodMode)
        },
        // Re-seed the settings VM when entitlement state changes so the Subscription section swaps
        // Upgrade↔Manage (and the god-mode toggle reflects the current value). The VM is seeded once
        // via parametersOf, so without a fresh key it'd keep the stale initial state.
        settingsVmKey = "settings-${state.isPro}-${state.isGodMode}",
        settingsEffects = { scope -> demoSettingsHandler(scope, onEffect) },
        onboardingPages = demoOnboardingPages,
        // Single central collector for the shared VM's one-shot effects (the channel is single-
        // consumer): navigation effects push onto the current tab's back stack; everything else is
        // forwarded to the host. Lives in the shell's `effects` slot so one lifecycle-aware collector
        // survives tab swaps.
        effects = { scope ->
            EffectCollector(vm.effects) { effect ->
                routeDemoEffect(effect, { route -> scope.navigateTo(route) }, onEffect)
            }
        },
        // Host destinations, registered on the shell's entryProvider. The demo's screens share the one
        // host-scoped DemoViewModel rather than per-entry Koin VMs, hence the inline entries. The
        // paywall is mounted here because :shared-demo can't see :shared (whose FrnkAppScaffold
        // auto-mounts it) — any host on the bare shell registers it the same way.
        entries = { scope ->
            entry<DemoRoute.Components> {
                ComponentsListScreen(
                    state = state,
                    onIntent = vm::send,
                    onOpenComponent = { name -> scope.navigateTo(DemoRoute.ComponentDetail(name)) },
                )
            }
            entry<DemoRoute.ComponentDetail> { route ->
                ComponentDetailScreen(
                    name = route.name,
                    onBack = { scope.back() },
                ) {
                    ComponentContent(route.name, state, vm::send, onEffect)
                }
            }
            // Entry points #1/#2/#3 all land here, on the toolkit-owned `ToolkitRoute.Paywall`. The
            // toolkit owns the paywall screen + VM (offerings, purchase/restore via the frnk
            // EntitlementManager); the demo just mounts the destination + handles close/messages.
            entry<ToolkitRoute.Paywall> {
                FrnkPaywallDestination(
                    features =
                        listOf(
                            "Unlimited everything",
                            "No ads",
                            "Priority support",
                        ),
                    source = "demo",
                    onMessage = { message -> onEffect(DemoEffect.Toast(message)) },
                    onClose = { scope.back() },
                )
            }
        },
    ) {
        HomeTabContent(state = state, onIntent = vm::send)
    }
}

/**
 * The demo's Settings effect handler — Entry point #2: the toolkit's monetization wiring
 * ([rememberFrnkSettingsHandler]: Upgrade → paywall, Restore, god mode, Manage Subscription) with a
 * fallback for the non-monetization effects (appearance, the shell's built-in onboarding flow,
 * feedback). A composable factory because it's built inside the shell's settings entry, where the
 * ambient theme and the [FrnkAppScope] exist.
 */
@Composable
private fun demoSettingsHandler(
    scope: FrnkAppScope,
    onEffect: (DemoEffect) -> Unit,
): (SettingsEffect) -> Unit {
    val appearanceController = LocalAppearanceController.current
    val entitlements: EntitlementManager = koinInject()
    val analytics: AnalyticsTracker = koinInject()
    // Opens the platform mail composer prefilled with app + OS diagnostics. A real host passes its
    // own app name/version and may override `recipient` to route feedback to its own inbox.
    val sendFeedback =
        rememberFeedbackEmailLauncher(
            appName = "frnk",
            appVersion = "v${Frnk.VERSION}",
        )
    return rememberFrnkSettingsHandler(
        backStack = scope.tabbed.current,
        entitlements = entitlements,
        analytics = analytics,
        onMessage = { message -> onEffect(DemoEffect.Toast(message)) },
        fallback = { effect ->
            when (effect) {
                is SettingsEffect.AppearanceChanged -> appearanceController.appearance = effect.appearance
                is SettingsEffect.ActionInvoked ->
                    when (effect.action) {
                        SettingsAction.ShowOnboarding -> scope.navigateTo(ToolkitRoute.Onboarding)
                        SettingsAction.SendFeedback -> sendFeedback()
                        else -> onEffect(DemoEffect.Toast("${effect.action} tapped"))
                    }
                is SettingsEffect.ToggleChanged -> onEffect(DemoEffect.Toast("${effect.id} = ${effect.checked}"))
            }
        },
    )
}

/**
 * Home tab body — the toolkit showcase, rendered inside the shell's built-in `HomeScreen` slot (the
 * scaffold owns the pinned top bar + the scrolling column + the merged padding; this just supplies
 * the items). The top-bar Upgrade action and the bar's primary-action button arrive as `HomeEffect`s
 * handled in [DemoScreen]'s `onHomeEffect`.
 */
@Composable
private fun HomeTabContent(
    state: DemoState,
    onIntent: (DemoIntent) -> Unit,
) {
    Section(title = "0. Bottom nav engine (A/B POC)") {
        FrnkText(
            state =
                FrnkTextState.BodySmall(
                    text =
                        "Flip the bottom bar live. Calf = native iOS UITabBar (no add button). " +
                            "AdaptiveNavBar = the new lib with a built-in add button (FAB on Android, " +
                            "inline on iOS) — tap it for a toast.",
                    color = colorOnSurfaceVariant,
                ),
        )
        FrnkSegmentedControl(
            state =
                FrnkSegmentedControlState.Content(
                    options = listOf("Calf", "AdaptiveNavBar"),
                    selectedIndex = if (state.navEngine == FrnkAdaptiveNavEngine.Calf) 0 else 1,
                ),
            onOptionSelected = { onIntent(DemoIntent.NavEngineChanged(it)) },
        )
    }

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

    Section(title = "2. Persistence (DemoDB — ${state.notes.size} saved)") {
        FrnkText(
            state =
                FrnkTextState.Body(
                    text =
                        "Demo-owned NoteStore over the demo's SQLDelight DemoDB, built through " +
                            ":data-db-api's SqlDriverFactory like a real host schema. Android runs the " +
                            "real driver (databaseModule + demoNotesModule); DemoKit/iOS binds an " +
                            "in-memory fake so the framework stays cinterop-free.",
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
                        "AnalyticsTracker + CrashReporter (:shared:backend:api), a backend-independent " +
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
                            "(installed with firebaseObservabilityModule) reports it symbolicated; on Android " +
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
                            "FrnkNavDisplay — Request Upgrade pushes the Paywall; the bottom bar switches tabs.",
                    color = colorOnSurfaceVariant,
                ),
        )
    }
}

/**
 * Components tab list — every `Frnk*` atom by name under a searchable top bar. Tapping a row pushes
 * that component's detail destination via [onOpenComponent]. All state (search + the interactive atoms'
 * values) is hoisted into [DemoViewModel]; this composable is stateless. Physical/gesture back closes an
 * open search field, otherwise it falls through to the `FrnkNavDisplay` (which pops the tab back to Home).
 */
@Composable
private fun ComponentsListScreen(
    state: DemoState,
    onIntent: (DemoIntent) -> Unit,
    onOpenComponent: (String) -> Unit,
) {
    val searchActive = state.searchActive
    val query = state.searchQuery

    val trimmedQuery = query.trim()
    val matches = componentNames.filter { it.contains(trimmedQuery, ignoreCase = true) }

    // Only intercept back to close an open search field; when inactive the FrnkNavDisplay handles back.
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
 * Back is handled by the `FrnkNavDisplay` (system back / swipe-back pop the stack automatically); the top
 * bar's back arrow calls [onBack].
 */
@Composable
private fun ComponentDetailScreen(
    name: String,
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
            // Exercises the extraSections injection API (default placement: before the Legal section).
            extraSections = demoExtraSettingsSections(),
        )
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
    return remember(baseSettings, developerSection) {
        baseSettings.copy(
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

/**
 * The shell's built-in onboarding flow ([FrnkAppShell]'s `onboardingPages`) — plain data built with
 * Lucide icons directly (it's constructed outside the theme, so token lookups aren't available; a
 * host that wants token-driven icons builds the pages inside its own themed composable instead).
 */
private val demoOnboardingPages: List<OnboardingPageState> =
    listOf(
        OnboardingPageState(
            title = FrnkTextState.Title(text = "Welcome to Frnk"),
            description =
                FrnkTextState.Body(
                    text = "A Kotlin Multiplatform toolkit to ship polished apps in days, not weeks.",
                ),
            icon =
                FrnkIconState.Content(
                    imageVector = Lucide.Check,
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
                    imageVector = Lucide.Search,
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
                    imageVector = Lucide.Settings,
                    contentDescription = null,
                    size = FrnkIconSize.xxl,
                    tint = colorPrimary,
                ),
        ),
    )

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
 * iOS interactive swipe-back — to an in-app action. The `FrnkNavDisplay` already pops its back stack on
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
