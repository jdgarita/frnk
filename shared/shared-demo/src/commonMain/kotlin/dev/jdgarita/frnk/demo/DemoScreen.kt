package dev.jdgarita.frnk.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.Component
import com.composables.icons.lucide.Lucide
import com.composeunstyled.theme.Theme
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
import dev.jdgarita.frnk.ui.scaffolds.BottomNavIntent
import dev.jdgarita.frnk.ui.scaffolds.BottomNavScaffoldContent
import dev.jdgarita.frnk.ui.scaffolds.BottomNavTab
import dev.jdgarita.frnk.ui.scaffolds.CollapsibleBarsState
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.scaffolds.OnboardingEffect
import dev.jdgarita.frnk.ui.scaffolds.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.OnboardingScreen
import dev.jdgarita.frnk.ui.scaffolds.OnboardingScreenState
import dev.jdgarita.frnk.ui.scaffolds.SettingsAction
import dev.jdgarita.frnk.ui.scaffolds.SettingsClickableRowState
import dev.jdgarita.frnk.ui.scaffolds.SettingsEffect
import dev.jdgarita.frnk.ui.scaffolds.SettingsScreen
import dev.jdgarita.frnk.ui.scaffolds.SettingsScreenState
import dev.jdgarita.frnk.ui.scaffolds.SettingsSectionState
import dev.jdgarita.frnk.ui.scaffolds.SettingsToggleRowState
import dev.jdgarita.frnk.ui.scaffolds.rememberBottomNavScaffoldState
import dev.jdgarita.frnk.ui.scaffolds.rememberCollapsibleBarsState
import dev.jdgarita.frnk.ui.scaffolds.rememberDefaultSettingsState
import dev.jdgarita.frnk.ui.scaffolds.rememberFeedbackEmailLauncher
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import dev.jdgarita.frnk.ui.theme.colorOnBackground
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colorSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconBack
import dev.jdgarita.frnk.ui.theme.iconCheck
import dev.jdgarita.frnk.ui.theme.iconChevronRight
import dev.jdgarita.frnk.ui.theme.iconManageSubscription
import dev.jdgarita.frnk.ui.theme.iconPrivacy
import dev.jdgarita.frnk.ui.theme.iconRestore
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.theme.rememberFrnkRipple
import dev.jdgarita.frnk.ui.theme.shapeCard
import dev.jdgarita.frnk.ui.theme.shapes
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import dev.jdgarita.frnk.utils.Frnk
import org.koin.compose.viewmodel.koinViewModel

/**
 * Smoke harness for the toolkit, structured as a real app would be: the whole screen lives inside the
 * `BottomNavScaffold`, which is the persistent shell, and every tab is topped by a status-bar-safe
 * `FrnkTopAppBar`. Three tabs:
 *  - **Home** — the toolkit showcase (theming + atoms, FeatureGate, MVI engine).
 *  - **Components** — a gallery of every `Frnk*` atom in its different styles/states (back + search).
 *  - **Settings** — the real `SettingsScreen` scaffold (back; Onboarding is launched from here).
 *
 * The demo drives the bottom nav through the **stateless** [BottomNavScaffoldContent] so it owns the
 * selected index — that lets the Settings/Components back buttons return to the Home tab.
 *
 * The host integration story: a real app passes its own [dev.jdgarita.frnk.ui.theme.FrnkThemeConfig]
 * (color/typography/string/icon overrides), binds a real [dev.jdgarita.frnk.monetization.EntitlementManager]
 * (e.g. RevenueCat), and mounts [DemoScreen] under its own NavHost.
 */
@Composable
fun DemoScreen(onEffect: (DemoEffect) -> Unit = {}) {
    val vm: DemoViewModel = koinViewModel()
    val state by vm.state.collectAsState()

    // Same rationale as OnboardingScreen: the collector is keyed on `vm`, so wrap onEffect in
    // rememberUpdatedState so a recomposing caller's new lambda is observed by the long-lived
    // collector instead of capturing the first-composition lambda forever.
    val currentOnEffect by rememberUpdatedState(onEffect)
    LaunchedEffect(vm) { vm.effects.collect { currentOnEffect(it) } }

    val appearanceController = LocalAppearanceController.current

    // Opens the platform mail composer prefilled with app + OS diagnostics. A real host passes its
    // own app name/version and may override `recipient` to route feedback to its own inbox.
    val sendFeedback =
        rememberFeedbackEmailLauncher(
            appName = "frnk",
            appVersion = "v${Frnk.VERSION}",
        )

    // Home is always index 0 (rememberBottomNavScaffoldState builds [Home, middle, Settings]).
    val navState =
        rememberBottomNavScaffoldState(
            middleTab =
                BottomNavTab(
                    key = "components",
                    icon = Lucide.Component,
                    label = "Components",
                ),
        )
    val navigateHome = { vm.send(DemoIntent.NavigateHome) }

    // A single collapse coordinator shared by every tab's top bar and the one floating bottom bar, so
    // they hide/reveal together on scroll. Reset to "shown" whenever the visible tab changes.
    val collapsibleBars = rememberCollapsibleBarsState()
    LaunchedEffect(state.selectedTabIndex) { collapsibleBars.reset() }

    Box(modifier = Modifier.fillMaxSize()) {
        BottomNavScaffoldContent(
            state = navState.copy(selectedIndex = state.selectedTabIndex),
            onIntent = { intent ->
                when (intent) {
                    is BottomNavIntent.TabSelected -> vm.send(DemoIntent.TabSelected(intent.index))
                }
            },
            modifier = Modifier.fillMaxSize(),
            collapsibleBars = collapsibleBars,
        ) { tab, contentPadding ->
            when (tab.key) {
                "components" ->
                    ComponentsTab(
                        state = state,
                        onIntent = vm::send,
                        contentPadding = contentPadding,
                        collapsibleBars = collapsibleBars,
                        onBack = navigateHome,
                        onEffect = onEffect,
                    )
                "settings" -> {
                    // Blank title: the FrnkTopAppBar already shows "Settings", so suppress the
                    // scaffold's own header to avoid a duplicate heading. The default catalog is
                    // extended with demo-only Preferences/Account sections so the screen overflows the
                    // viewport on tall devices — otherwise it fits and the collapsing bars never engage.
                    val baseSettings =
                        rememberDefaultSettingsState(
                            version = "v${Frnk.VERSION}",
                            appearance = appearanceController.appearance,
                            isPro = state.isPro,
                            title = "",
                        )
                    val extraSettings = demoExtraSettingsSections()
                    val settingsState =
                        remember(baseSettings, extraSettings) {
                            baseSettings.copy(sections = baseSettings.sections + extraSettings)
                        }
                    SettingsTab(
                        initialState = settingsState,
                        contentPadding = contentPadding,
                        collapsibleBars = collapsibleBars,
                        onBack = navigateHome,
                        onEffect = { effect ->
                            when (effect) {
                                is SettingsEffect.AppearanceChanged ->
                                    appearanceController.appearance = effect.appearance
                                is SettingsEffect.ToggleChanged ->
                                    onEffect(DemoEffect.Toast("${effect.id} = ${effect.checked}"))
                                is SettingsEffect.ActionInvoked ->
                                    when (effect.action) {
                                        SettingsAction.ShowOnboarding -> vm.send(DemoIntent.ShowOnboarding)
                                        SettingsAction.SendFeedback -> sendFeedback()
                                        else -> onEffect(DemoEffect.Toast("${effect.action} tapped"))
                                    }
                            }
                        },
                    )
                }
                else ->
                    HomeTab(
                        state = state,
                        contentPadding = contentPadding,
                        collapsibleBars = collapsibleBars,
                        onEffect = onEffect,
                        onIntent = vm::send,
                    )
            }
        }

        if (state.showOnboarding) {
            OnboardingScreen(
                initialState = demoOnboardingState(),
                modifier = Modifier.fillMaxSize(),
                // Keyed on onboardingSession (bumped on each open) so OnboardingScreen resolves a fresh
                // VM instead of reusing the last session's page index — otherwise dismissing on page 3
                // and reopening would land back on page 3.
                vmKey = "demo-onboarding-${state.onboardingSession}",
                onEffect = { effect ->
                    when (effect) {
                        OnboardingEffect.CloseRequested,
                        OnboardingEffect.Completed,
                        -> vm.send(DemoIntent.DismissOnboarding)
                    }
                },
            )
        }
    }
}

/**
 * Home tab — the toolkit showcase that used to be the demo's start screen. The appearance toggle was
 * dropped (it lives on the Settings screen now); the screen title moved into the [FrnkTopAppBar].
 */
@Composable
private fun HomeTab(
    state: DemoState,
    contentPadding: PaddingValues,
    collapsibleBars: CollapsibleBarsState,
    onEffect: (DemoEffect) -> Unit,
    onIntent: (DemoIntent) -> Unit,
) {
    FrnkScreenScaffold(
        topBar = FrnkTopAppBarState(title = "frnk"),
        collapsibleBars = collapsibleBars,
        bottomInset = contentPadding.calculateBottomPadding(),
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding),
            verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
        ) {
            Section(title = "1. Theme + Atoms") {
                FrnkText(state = FrnkTextState.TitleMedium(text = "Text variants"))
                FrnkText(state = FrnkTextState.Body(text = "Body — the default reading style."))
                FrnkText(
                    state =
                        FrnkTextState.BodySmall(
                            text = "BodySmall — secondary copy.",
                            color = colorOnSurfaceVariant,
                        ),
                )

                FrnkText(state = FrnkTextState.TitleMedium(text = "Buttons"))
                Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                    FrnkButton(
                        state = FrnkButtonState(text = "Filled"),
                        onClick = { onIntent(DemoIntent.Increment) },
                    )
                    FrnkButton(
                        state = FrnkButtonState(text = "Outlined", variant = FrnkButtonVariant.Outlined),
                        onClick = { onIntent(DemoIntent.Decrement) },
                    )
                    FrnkButton(
                        state =
                            FrnkButtonState(
                                text = "Ghost",
                                variant = FrnkButtonVariant.Ghost,
                                enabled = false,
                            ),
                        onClick = { },
                    )
                }

                FrnkText(state = FrnkTextState.TitleMedium(text = "Icons"))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FrnkIcon(
                        state =
                            FrnkIconState(
                                imageVector = Theme[icons][iconBack],
                                contentDescription = "Back",
                                size = FrnkIconSize.lg,
                                tint = colorPrimary,
                            ),
                    )
                    FrnkIcon(
                        state =
                            FrnkIconState(
                                imageVector = Theme[icons][iconCheck],
                                contentDescription = "Check",
                                size = FrnkIconSize.lg,
                                tint = colorPrimary,
                            ),
                    )
                    FrnkIconButton(
                        state =
                            FrnkIconButtonState(
                                imageVector = Theme[icons][iconSettings],
                                contentDescription = "Settings",
                                tint = colorOnBackground,
                            ),
                        onClick = { onEffect(DemoEffect.Toast("Settings tapped")) },
                    )
                }

                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text = "Count: ${state.count} • Email field: pending FrnkTextField (v2)",
                            color = colorOnSurfaceVariant,
                        ),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            FrnkDivider(state = FrnkDividerState.Horizontal())

            Section(title = "2. FeatureGate (Pro = ${state.isPro})") {
                Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                    FrnkButton(
                        state =
                            FrnkButtonState(
                                text = "Toggle Pro",
                                variant = FrnkButtonVariant.Outlined,
                            ),
                        onClick = { onIntent(DemoIntent.TogglePro) },
                    )
                    FrnkButton(
                        state = FrnkButtonState(text = "Request Upgrade"),
                        onClick = { onIntent(DemoIntent.RequestUpgrade) },
                    )
                }
            }

            FrnkDivider(state = FrnkDividerState.Horizontal())

            Section(title = "3. Persistence (FrnkDB — ${state.notes.size} saved)") {
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
                        state = FrnkButtonState(text = "Add note"),
                        onClick = { onIntent(DemoIntent.AddNote) },
                    )
                    FrnkButton(
                        state =
                            FrnkButtonState(
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

            Section(title = "4. Analytics & Crash") {
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
                        state = FrnkButtonState(text = "Track event"),
                        onClick = { onIntent(DemoIntent.TrackEvent) },
                    )
                    FrnkButton(
                        state = FrnkButtonState(text = "User property", variant = FrnkButtonVariant.Outlined),
                        onClick = { onIntent(DemoIntent.SetUserProperty) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                    FrnkButton(
                        state = FrnkButtonState(text = "Log breadcrumb", variant = FrnkButtonVariant.Outlined),
                        onClick = { onIntent(DemoIntent.LogBreadcrumb) },
                    )
                    FrnkButton(
                        state = FrnkButtonState(text = "Record non-fatal", variant = FrnkButtonVariant.Outlined),
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
                    state = FrnkButtonState(text = "Force crash (unhandled)", variant = FrnkButtonVariant.Outlined),
                    onClick = { onIntent(DemoIntent.ForceUnhandledCrash) },
                )
            }

            FrnkDivider(state = FrnkDividerState.Horizontal())

            Section(title = "5. MVI") {
                FrnkText(
                    state =
                        FrnkTextState.Body(
                            text =
                                "DemoViewModel = MviViewModel<DemoState, DemoIntent, DemoEffect>.\n" +
                                    "Every interaction above flows: Composable → send(Intent) → reducer → State → recomposition.",
                            color = colorOnSurfaceVariant,
                        ),
                )
            }
        }
    }
}

/**
 * Components tab — a vertical list of every `Frnk*` atom by name, under a top bar with a back button
 * (returns to Home) and a search action that filters the list. Tapping a row opens that component's
 * dedicated detail screen ([ComponentDetailScreen]), which shows all of its variants; back there
 * returns to this list. All of this tab's state (search, list ↔ detail selection, and the interactive
 * atoms' values) is hoisted into [DemoViewModel], so it survives navigation and recomposition; this
 * composable is stateless — it reads [state] and emits [onIntent].
 */
@Composable
private fun ComponentsTab(
    state: DemoState,
    onIntent: (DemoIntent) -> Unit,
    contentPadding: PaddingValues,
    collapsibleBars: CollapsibleBarsState,
    onBack: () -> Unit,
    onEffect: (DemoEffect) -> Unit,
) {
    val searchActive = state.searchActive
    val query = state.searchQuery
    val selectedComponent = state.selectedComponent

    // The gallery as (componentName, content) pairs so the search field can filter it by name.
    val components: List<Pair<String, @Composable () -> Unit>> =
        listOf(
            "FrnkText" to {
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
            },
            "FrnkButton" to {
                Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                    FrnkButton(state = FrnkButtonState(text = "Filled"), onClick = {})
                    FrnkButton(
                        state = FrnkButtonState(text = "Outlined", variant = FrnkButtonVariant.Outlined),
                        onClick = {},
                    )
                    FrnkButton(
                        state = FrnkButtonState(text = "Ghost", variant = FrnkButtonVariant.Ghost),
                        onClick = {},
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                    FrnkButton(
                        state = FrnkButtonState(text = "Filled", enabled = false),
                        onClick = {},
                    )
                    FrnkButton(
                        state =
                            FrnkButtonState(
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
                        state = FrnkButtonState(text = "Loading", skeleton = FrnkSkeleton(enabled = true)),
                        onClick = {},
                    )
                    FrnkButton(
                        state =
                            FrnkButtonState(
                                text = "Outlined",
                                variant = FrnkButtonVariant.Outlined,
                                skeleton = FrnkSkeleton(enabled = true),
                            ),
                        onClick = {},
                    )
                }
            },
            "FrnkIcon / FrnkIconButton" to {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FrnkIcon(
                        state =
                            FrnkIconState(
                                imageVector = Theme[icons][iconSearch],
                                contentDescription = "Search",
                                size = FrnkIconSize.md,
                                tint = colorPrimary,
                            ),
                    )
                    FrnkIcon(
                        state =
                            FrnkIconState(
                                imageVector = Theme[icons][iconCheck],
                                contentDescription = "Check",
                                size = FrnkIconSize.lg,
                                tint = colorPrimary,
                            ),
                    )
                    FrnkIconButton(
                        state =
                            FrnkIconButtonState(
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
                            FrnkIconState(
                                imageVector = Theme[icons][iconCheck],
                                contentDescription = null,
                                size = FrnkIconSize.lg,
                                tint = colorPrimary,
                                skeleton = FrnkSkeleton(enabled = true),
                            ),
                    )
                    FrnkIconButton(
                        state =
                            FrnkIconButtonState(
                                imageVector = Theme[icons][iconSettings],
                                contentDescription = "Settings",
                                tint = colorOnBackground,
                                skeleton = FrnkSkeleton(enabled = true),
                            ),
                        onClick = {},
                    )
                }
            },
            "FrnkDivider" to {
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
            },
            "FrnkSwitch" to {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FrnkSwitch(
                        state = FrnkSwitchState(checked = state.gallerySwitchOn),
                        onCheckedChange = { onIntent(DemoIntent.GallerySwitchChanged(it)) },
                    )
                    FrnkText(state = FrnkTextState.BodySmall(text = if (state.gallerySwitchOn) "On" else "Off"))
                    FrnkSwitch(
                        state = FrnkSwitchState(checked = true, enabled = false),
                        onCheckedChange = {},
                    )
                    FrnkText(state = FrnkTextState.BodySmall(text = "Disabled"))
                }
                FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
                FrnkSwitch(
                    state = FrnkSwitchState(checked = true, skeleton = FrnkSkeleton(enabled = true)),
                    onCheckedChange = {},
                )
            },
            "FrnkSegmentedControl" to {
                FrnkSegmentedControl(
                    state =
                        FrnkSegmentedControlState(
                            options = listOf("One", "Two", "Three"),
                            selectedIndex = state.gallerySegmentIndex,
                        ),
                    onOptionSelected = { onIntent(DemoIntent.GallerySegmentChanged(it)) },
                )
                FrnkText(state = FrnkTextState.BodySmall(text = "Skeleton", color = colorOnSurfaceVariant))
                FrnkSegmentedControl(
                    state =
                        FrnkSegmentedControlState(
                            options = listOf("One", "Two", "Three"),
                            selectedIndex = 0,
                            skeleton = FrnkSkeleton(enabled = true),
                        ),
                    onOptionSelected = {},
                )
            },
            "FrnkBottomNavBar" to {
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
            },
            "Ripple" to {
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
            },
        )

    // Navigating into / out of a component detail is a screen change — start it with the bars shown.
    LaunchedEffect(selectedComponent) { collapsibleBars.reset() }

    val selected = components.firstOrNull { it.first == selectedComponent }
    if (selected != null) {
        ComponentDetailScreen(
            name = selected.first,
            bottomInset = contentPadding.calculateBottomPadding(),
            collapsibleBars = collapsibleBars,
            onBack = { onIntent(DemoIntent.ComponentSelected(null)) },
            content = selected.second,
        )
        return
    }

    val trimmedQuery = query.trim()
    val matches = components.filter { it.first.contains(trimmedQuery, ignoreCase = true) }

    // List view (detail path returned above): physical back mirrors the top bar's leading button —
    // close the search field if it's open, otherwise fall through to the back arrow's target (Home).
    DemoBackHandler {
        if (searchActive) {
            onIntent(DemoIntent.SearchClosed)
        } else {
            onBack()
        }
    }

    FrnkScreenScaffold(
        topBar =
            FrnkTopAppBarState(
                title = "Components",
                navigationIcon = Theme[icons][iconBack],
                navigationContentDescription = "Back",
                actions =
                    listOf(
                        FrnkTopAppBarAction(icon = Theme[icons][iconSearch], contentDescription = "Search"),
                    ),
                isSearchActive = searchActive,
                searchQuery = query,
                searchPlaceholder = "Search components",
            ),
        collapsibleBars = collapsibleBars,
        bottomInset = contentPadding.calculateBottomPadding(),
        onNavigationClick = onBack,
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

            matches.forEachIndexed { index, (name, _) ->
                if (index > 0) {
                    FrnkDivider(state = FrnkDividerState.Horizontal())
                }
                ComponentRow(name = name, onClick = { onIntent(DemoIntent.ComponentSelected(name)) })
            }
        }
    }
}

/**
 * A tappable row in the Components list: the component's name with a trailing chevron. Tapping it opens
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
                FrnkIconState(
                    imageVector = Theme[icons][iconChevronRight],
                    contentDescription = null,
                    size = FrnkIconSize.md,
                    tint = colorOnSurfaceVariant,
                ),
        )
    }
}

/**
 * The dedicated detail screen for a single component — its name in the top bar over a scrollable list
 * of all that component's variants. Uses the same [FrnkScreenScaffold] template as every other screen
 * (so its bars collapse on scroll too); back returns to the Components list.
 */
@Composable
private fun ComponentDetailScreen(
    name: String,
    bottomInset: Dp,
    collapsibleBars: CollapsibleBarsState,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    // The Android physical back button / predictive-back gesture (and the iOS swipe-back gesture)
    // mirror the top bar's back arrow: return to the Components list. Without this, system back
    // bypasses the in-app navigation state and pops the whole Activity (exits the app) instead.
    DemoBackHandler { onBack() }

    FrnkScreenScaffold(
        topBar =
            FrnkTopAppBarState(
                title = name,
                navigationIcon = Theme[icons][iconBack],
                navigationContentDescription = "Back",
            ),
        collapsibleBars = collapsibleBars,
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
 * Settings tab — a top bar with a back button (returns to Home) over the real [SettingsScreen]
 * scaffold. [initialState] is built with a blank title so the bar's "Settings" heading isn't doubled.
 */
@Composable
private fun SettingsTab(
    initialState: SettingsScreenState,
    contentPadding: PaddingValues,
    collapsibleBars: CollapsibleBarsState,
    onBack: () -> Unit,
    onEffect: (SettingsEffect) -> Unit,
) {
    // Physical back / predictive-back gesture mirrors the top bar's back arrow: return to Home.
    DemoBackHandler { onBack() }

    FrnkScreenScaffold(
        topBar =
            FrnkTopAppBarState(
                title = "Settings",
                navigationIcon = Theme[icons][iconBack],
                navigationContentDescription = "Back",
            ),
        collapsibleBars = collapsibleBars,
        bottomInset = contentPadding.calculateBottomPadding(),
        onNavigationClick = onBack,
    ) { padding ->
        SettingsScreen(
            initialState = initialState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding,
            onEffect = onEffect,
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
    val hapticsIcon = Theme[icons][iconSettings]
    val analyticsIcon = Theme[icons][iconPrivacy]
    val accountIcon = Theme[icons][iconManageSubscription]
    val signOutIcon = Theme[icons][iconRestore]
    return remember(hapticsIcon, analyticsIcon, accountIcon, signOutIcon) {
        fun rowIcon(vector: ImageVector) = FrnkIconState(imageVector = vector, contentDescription = null, tint = colorPrimary)
        listOf(
            SettingsSectionState(
                title = "Preferences",
                rows =
                    listOf(
                        SettingsToggleRowState(
                            id = "haptics",
                            icon = rowIcon(hapticsIcon),
                            title = "Haptic feedback",
                            subtitle = "Vibrate on interactions",
                            checked = true,
                        ),
                        SettingsToggleRowState(
                            id = "analytics",
                            icon = rowIcon(analyticsIcon),
                            title = "Share analytics",
                            subtitle = "Help improve the app",
                            checked = true,
                        ),
                    ),
            ),
            SettingsSectionState(
                title = "Account",
                rows =
                    listOf(
                        SettingsClickableRowState(
                            id = "manage_account",
                            icon = rowIcon(accountIcon),
                            title = "Manage account",
                            action = SettingsAction.Custom("manage_account"),
                        ),
                        SettingsClickableRowState(
                            id = "sign_out",
                            icon = rowIcon(signOutIcon),
                            title = "Sign out",
                            action = SettingsAction.Custom("sign_out"),
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
                            FrnkIconState(
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
                            FrnkIconState(
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
                            FrnkIconState(
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
 * Bridges the platform back signal — the Android system back button + predictive-back gesture, and
 * the iOS interactive swipe-back — to an in-app navigation action, so the demo's state-driven
 * navigation (it deliberately runs no NavHost) honours the hardware/gesture back the same way it
 * honours the on-screen back arrows.
 *
 * Confines the opt-in for the still-`@ExperimentalComposeUiApi` — and, as of Compose Multiplatform
 * 1.11, soft-deprecated in favour of `androidx.navigationevent`'s `NavigationEventHandler` —
 * [BackHandler] to this one place: call sites stay clean and the eventual migration is a one-function
 * change rather than an edit at every screen.
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
