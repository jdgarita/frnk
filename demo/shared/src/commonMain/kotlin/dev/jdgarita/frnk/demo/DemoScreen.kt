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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
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
import dev.jdgarita.frnk.demo.ext.demoFeatureEntries
import dev.jdgarita.frnk.monetization.EntitlementManager
import dev.jdgarita.frnk.monetization.ui.FrnkPaywallDestination
import dev.jdgarita.frnk.monetization.ui.GOD_MODE_TOGGLE_ID
import dev.jdgarita.frnk.monetization.ui.rememberFrnkSettingsHandler
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
import dev.jdgarita.frnk.ui.bottomnav.FrnkAppInfo
import dev.jdgarita.frnk.ui.bottomnav.FrnkAppScope
import dev.jdgarita.frnk.ui.bottomnav.FrnkBottomFloatingBar
import dev.jdgarita.frnk.ui.bottomnav.FrnkFeatureItem
import dev.jdgarita.frnk.ui.bottomnav.FrnkFirstLaunchOnboardingEffect
import dev.jdgarita.frnk.ui.bottomnav.FrnkHomeConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavBarItem
import dev.jdgarita.frnk.ui.bottomnav.FrnkNavConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkOnboardingConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkSettingsConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkTabbedNavConfig
import dev.jdgarita.frnk.ui.bottomnav.FrnkTabbedNavScaffold
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
import dev.jdgarita.frnk.ui.organisms.FrnkListSection
import dev.jdgarita.frnk.ui.organisms.FrnkListSectionState
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeader
import dev.jdgarita.frnk.ui.organisms.FrnkProfileHeaderState
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.scaffolds.home.HomeEffect
import dev.jdgarita.frnk.ui.scaffolds.onboarding.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.onboarding.rememberOnboardingGate
import dev.jdgarita.frnk.ui.scaffolds.rememberFeedbackEmailLauncher
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsEffect
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreen
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.settings.SettingsToggleRowState
import dev.jdgarita.frnk.ui.scaffolds.settings.rememberDefaultSettingsState
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
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
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Smoke harness for the toolkit — and the reference integration of **[FrnkTabbedNavScaffold]**, the
 * one-call app root. It owns the theme wrap, the nav3 saved-state config, the fixed Home · Components ·
 * Settings adaptive tabs with per-tab back stacks, the persistent bottom bar (tab switching, back
 * conventions, full-screen hiding, bottom-inset), and the built-in Home / Settings / Onboarding
 * destinations; the demo supplies only its content:
 *  - **Home** (`ToolkitRoute.Home`, the shell's built-in `HomeScreen`) — the toolkit showcase via
 *    [DemoHomeContent] in the `homeContent` slot; the crown Upgrade action arrives as a `HomeEffect`.
 *  - **Components** ([DemoRoute.Components], the demo's center `feature` tab) — a gallery of every
 *    `Frnk*` atom; tapping a row pushes [DemoRoute.ComponentDetail] (a type-safe `name` argument).
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
 * *can* depend on `:ui-app` uses `FrnkAppScaffold` instead (as demo-android's `MainActivity` does,
 * feeding it these same shared builders), which layers the Koin assertion + live entitlement-driven
 * Settings + auto-mounted paywall over this same shell.
 *
 * **Bottom bar.** The adaptive bar always shows exactly three tabs — Home · Components · Settings —
 * with the center "Components" tab supplied as the shell's `feature` item. The adaptive-nav spike
 * evaluation is recorded in the MobiAI brain (`mobiai brain search "adaptive bottom nav"`).
 */
@Composable
fun DemoScreen(
    appearanceController: AppearanceController? = null,
    onEffect: (DemoEffect) -> Unit = {},
) {
    val vm: DemoViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    // The demo's center "feature" tab + its host routes are scaffold-agnostic — shared verbatim with the
    // Android FrnkAppScaffold host (demo-android's MainActivity). demoFeatureItem is a stable top-level
    // val; demoHostRoutes() builds a fresh SerializersModule (no value equality), so it MUST be
    // remembered once and held stable, else the config compares unequal each frame and the nav config
    // rebuilds. See [demoHostRoutes].
    val hostRoutes = remember { demoHostRoutes() }
    // Entry point #1: a top-right "Upgrade to Pro" action on Home, hidden once the user is Pro.
    val homeTopBar = remember(state.isPro) { demoHomeTopBar(state.isPro) }

    // The host's declarative config, bundled by feature. hostRoutes/homeTopBar are remembered above and
    // demoFeatureItem/demoPurpleThemeConfig()/demoOnboardingPages are stable, so this builds an equal
    // FrnkTabbedNavConfig across recompositions (keeping the scaffold skippable). No vmKey re-keying:
    // the Home and Settings VMs react to isPro/isGodMode on their own (the recomputed homeTopBar /
    // rememberDemoSettingsState flow down and are merged in via *Intent.ConfigChanged), so the Subscription
    // section swaps Upgrade↔Manage and the Home Upgrade action appears/disappears while a single VM
    // (and its in-session toggle/dev-reveal state) lives on.
    val config =
        remember(state.isPro, state.isGodMode, hostRoutes, homeTopBar) {
            FrnkTabbedNavConfig(
                app = FrnkAppInfo(name = "frnk", version = "v${Frnk.VERSION}"),
                nav = FrnkNavConfig(feature = demoFeatureItem, hostRoutes = hostRoutes),
                theme = demoPurpleThemeConfig(),
                home = FrnkHomeConfig(topBar = homeTopBar),
                settings = FrnkSettingsConfig(),
                onboarding = FrnkOnboardingConfig(pages = demoOnboardingPages),
            )
        }

    FrnkTabbedNavScaffold(
        config = config,
        modifier = Modifier.fillMaxSize(),
        appearanceController = appearanceController,
        onHomeEffect = { effect -> demoHandleHomeEffect(vm, effect) },
        settingsState = { _ ->
            rememberDemoSettingsState(LocalAppearanceController.current.appearance, state.isPro, state.isGodMode)
        },
        settingsEffects = { scope -> demoSettingsHandler(scope, onEffect) },
        // Single central collector for the shared VM's one-shot effects (the channel is single-
        // consumer): navigation effects push onto the current tab's back stack; everything else is
        // forwarded to the host. Lives in the shell's `effects` slot so one lifecycle-aware collector
        // survives tab swaps.
        effects = { scope ->
            // First-launch onboarding: DemoScreen composes the bare shell (not :ui-app's FrnkAppScaffold,
            // which wires this automatically), so it opts into the same gate helper here — gated on the
            // config flag, which is how a bare-FrnkTabbedNavScaffold host honours
            // FrnkOnboardingConfig.showOnFirstLaunch (the reference pattern). The demo's FakeKeyValueStore
            // is per-session, so each fresh launch replays onboarding-on-first-open.
            FrnkFirstLaunchOnboardingEffect(
                scope = scope,
                gate = rememberOnboardingGate(),
                enabled = config.onboarding.showOnFirstLaunch && config.onboarding.pages.isNotEmpty(),
            )
            DemoEffectCollector(vm, scope, onEffect)
        },
        // Host destinations, registered on the shell's entryProvider. The demo's screens share the one
        // host-scoped DemoViewModel rather than per-entry Koin VMs (see [demoFeatureEntries]). The
        // paywall is mounted here because :demo-shared can't see :ui-app (whose FrnkAppScaffold
        // auto-mounts it) — any host on the bare shell registers it the same way.
        entries = { scope ->
            demoFeatureEntries(scope = scope, state = state, onIntent = vm::send, onEffect = onEffect)
            entry<ToolkitRoute.Paywall> {
                FrnkPaywallDestination(
                    features = demoPaywallFeatures,
                    source = "demo",
                    onMessage = { message -> onEffect(DemoEffect.Toast(message)) },
                    onClose = { scope.back() },
                )
            }
        },
    ) {
        DemoHomeContent(state = state, onIntent = vm::send)
    }
}

/**
 * The demo's center "feature" tab — the "Components" gallery. A stable top-level val (so it can be the
 * shared `feature` for both scaffold hosts without re-`remember`ing): a Compose [ImageVector][Lucide]
 * (Android) + SF-Symbol (iOS) the way a real host configures the one host-configurable tab.
 */
val demoFeatureItem: FrnkFeatureItem =
    FrnkFeatureItem(
        route = DemoRoute.Components,
        label = "Components",
        icon = Lucide.Component,
        iosSystemIcon = "square.grid.2x2",
    )

/**
 * The demo's only host routes: the middle "Components" tab root + its pushed detail. Home / Settings /
 * Onboarding / Paywall are the toolkit-owned `ToolkitRoute` defaults. Returns a fresh `SerializersModule`
 * (no value equality) — callers must `remember` it once and hold it stable.
 */
fun demoHostRoutes(): SerializersModule =
    SerializersModule {
        polymorphic(NavKey::class) {
            subclass(DemoRoute.Components::class, DemoRoute.Components.serializer())
            subclass(DemoRoute.ComponentDetail::class, DemoRoute.ComponentDetail.serializer())
        }
    }

/**
 * The paywall feature bullets shown on `ToolkitRoute.Paywall`. Shared so the bare-shell host
 * (DemoScreen, which mounts the paywall itself) and the FrnkAppScaffold host (demo-android, which feeds
 * these to `FrnkMonetizationConfig.paywallFeatures`) advertise the same list.
 */
val demoPaywallFeatures: List<String> =
    listOf("Unlimited everything", "No ads", "Priority support")

/**
 * The Home tab's top bar — Entry point #1's "Upgrade to Pro" crown action, hidden once the user is Pro.
 * Plain Lucide icon (built outside the theme; the scaffold installs the theme). Shared by both hosts via
 * `FrnkHomeConfig.topBar`.
 */
fun demoHomeTopBar(isPro: Boolean): FrnkTopAppBarState =
    FrnkTopAppBarState(
        title = "frnk",
        actions =
            if (isPro) {
                emptyList()
            } else {
                listOf(FrnkTopAppBarAction(icon = Lucide.Crown, contentDescription = "Upgrade to Pro", key = "upgrade"))
            },
    )

/** Routes the Home tab's [HomeEffect]s — the crown "Upgrade" action opens the paywall via the VM. */
fun demoHandleHomeEffect(
    vm: DemoViewModel,
    effect: HomeEffect,
) {
    when (effect) {
        is HomeEffect.ActionInvoked -> if (effect.key == "upgrade") vm.send(DemoIntent.RequestUpgrade)
        HomeEffect.NavigationInvoked -> Unit
    }
}

/**
 * The single central collector for the shared [DemoViewModel]'s one-shot effects (the channel is
 * single-consumer, so this must be composed in exactly one place per host): navigation effects push onto
 * the current tab's back stack via [scope], everything else forwards to [onEffect]. Shared by both
 * scaffold hosts' `effects` slot.
 */
@Composable
fun DemoEffectCollector(
    vm: DemoViewModel,
    scope: FrnkAppScope,
    onEffect: (DemoEffect) -> Unit,
) {
    EffectCollector(vm.effects) { effect ->
        routeDemoEffect(effect, { route -> scope.navigateTo(route) }, onEffect)
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
fun demoSettingsHandler(
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
 * the items). The top-bar Upgrade action arrives as a `HomeEffect` handled in [DemoScreen]'s
 * `onHomeEffect`.
 */
@Composable
fun DemoHomeContent(
    state: DemoState,
    onIntent: (DemoIntent) -> Unit,
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

    FrnkDivider(state = FrnkDividerState.Horizontal())

    Section(title = "5. Capabilities (Stage 11)") {
        FrnkText(
            state =
                FrnkTextState.Body(
                    text =
                        "New capability modules, all resolved via Koin. RemoteConfigService " +
                            "(:remote-config-api) reads a key→value; the demo installs the no-op default " +
                            "(shows the bundled fallback), androidDemoApp overrides it with the real " +
                            "Firebase remoteConfigModule. :camera and :permissions are api-only scaffolds " +
                            "(no impl yet) — their no-op defaults surface the honest 'not wired' outcome.",
                    color = colorOnSurfaceVariant,
                ),
        )
        FrnkLabeledValue(
            state =
                FrnkLabeledValueState.Content(
                    label = "Remote welcome",
                    value = state.remoteWelcome,
                    orientation = FrnkLabeledValueOrientation.Stacked,
                ),
        )
        FrnkButton(
            state = FrnkButtonState.Content(text = "Fetch Remote Config", variant = FrnkButtonVariant.Outlined),
            onClick = { onIntent(DemoIntent.FetchRemoteConfig) },
        )
        FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Camera", value = state.cameraResult))
        FrnkLabeledValue(
            state = FrnkLabeledValueState.Content(label = "Camera permission", value = state.cameraPermission),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
            FrnkButton(
                state = FrnkButtonState.Content(text = "Capture photo", variant = FrnkButtonVariant.Outlined),
                onClick = { onIntent(DemoIntent.CapturePhoto) },
            )
            FrnkButton(
                state = FrnkButtonState.Content(text = "Request camera", variant = FrnkButtonVariant.Outlined),
                onClick = { onIntent(DemoIntent.RequestCameraPermission) },
            )
        }
    }
}

/**
 * Components tab list — every `Frnk*` atom by name under a searchable top bar. Tapping a row pushes
 * that component's detail destination via [onOpenComponent]. All state (search + the interactive atoms'
 * values) is hoisted into [DemoViewModel]; this composable is stateless. Physical/gesture back closes an
 * open search field, otherwise it falls through to the `FrnkNavDisplay` (which pops the tab back to Home).
 */
@Composable
internal fun ComponentsListScreen(
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
internal fun ComponentDetailScreen(
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
        "FrnkBottomFloatingBar",
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
internal fun ComponentContent(
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
        "FrnkBottomFloatingBar" -> {
            FrnkText(
                state =
                    FrnkTextState.BodySmall(
                        text =
                            "The real adaptive bar — the very same component shown at the foot of every screen " +
                                "(a Material3 floating pill on Android, a native glassy UITabBar on iOS).",
                        color = colorOnSurfaceVariant,
                    ),
            )
            FrnkBottomFloatingBar(
                items =
                    listOf(
                        FrnkNavBarItem("a", Theme[icons][iconSearch], "magnifyingglass", "Search"),
                        FrnkNavBarItem("b", Theme[icons][iconCheck], "checkmark", "Check"),
                        FrnkNavBarItem("c", Theme[icons][iconSettings], "gearshape", "Settings"),
                    ),
                selectedIndex = state.galleryNavIndex,
                onItemSelected = { onIntent(DemoIntent.GalleryNavChanged(it)) },
                modifier = Modifier.fillMaxWidth(),
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
fun rememberDemoSettingsState(
    appearance: dev.jdgarita.frnk.ui.theme.Appearance,
    isPro: Boolean,
    isGodMode: Boolean,
): SettingsScreenState {
    val baseSettings =
        rememberDefaultSettingsState(
            version = "v${Frnk.VERSION}",
            appearance = appearance,
            isPro = isPro,
            title = FrnkStringSource.Raw(""),
            // Exercises the extraSections injection API (default placement: before the Legal section).
            extraSections = demoExtraSettingsSections(),
        )
    // "Developer" section holding the god-mode toggle. The demo always shows it (showDeveloperSection =
    // true). Toggling god mode flips isPro, which recomputes this state; the single settings VM merges
    // it in via SettingsIntent.ConfigChanged (preserving the toggle/dev-reveal). Real apps can instead
    // leave it hidden and use the 7-tap version-footer reveal gesture (the toolkit supports both).
    val developerSection =
        remember(isGodMode) {
            SettingsSectionState(
                title = FrnkStringSource.Raw("Developer"),
                rows =
                    listOf(
                        SettingsToggleRowState(
                            id = GOD_MODE_TOGGLE_ID,
                            icon = FrnkIconSource.Token(iconUpgrade),
                            title = FrnkStringSource.Raw("God mode"),
                            subtitle = FrnkStringSource.Raw("Force Pro on this device (testing)"),
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
    return remember {
        listOf(
            SettingsSectionState(
                title = FrnkStringSource.Raw("Privacy"),
                rows =
                    listOf(
                        SettingsToggleRowState(
                            id = "analytics",
                            icon = FrnkIconSource.Token(iconPrivacy),
                            title = FrnkStringSource.Raw("Share analytics"),
                            subtitle = FrnkStringSource.Raw("Help improve the app"),
                            checked = true,
                        ),
                    ),
            ),
        )
    }
}

/**
 * The built-in onboarding flow ([FrnkTabbedNavScaffold]'s `onboardingPages`) — plain data built with
 * Lucide icons directly (it's constructed outside the theme, so token lookups aren't available; a
 * host that wants token-driven icons builds the pages inside its own themed composable instead).
 */
val demoOnboardingPages: List<OnboardingPageState> =
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
