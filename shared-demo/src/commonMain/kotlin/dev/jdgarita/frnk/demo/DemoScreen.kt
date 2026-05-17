package dev.jdgarita.frnk.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkButtonVariant
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.atoms.FrnkDividerState
import dev.jdgarita.frnk.ui.atoms.FrnkIcon
import dev.jdgarita.frnk.ui.atoms.FrnkIconButton
import dev.jdgarita.frnk.ui.atoms.FrnkIconButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkIconState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.scaffolds.OnboardingEffect
import dev.jdgarita.frnk.ui.scaffolds.OnboardingPageState
import dev.jdgarita.frnk.ui.scaffolds.OnboardingScreen
import dev.jdgarita.frnk.ui.scaffolds.OnboardingScreenState
import dev.jdgarita.frnk.ui.theme.Appearance
import dev.jdgarita.frnk.ui.theme.AppearanceController
import dev.jdgarita.frnk.ui.theme.LocalAppearanceController
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colorOnBackground
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.theme.colorPrimary
import dev.jdgarita.frnk.ui.theme.colors
import dev.jdgarita.frnk.ui.theme.iconBack
import dev.jdgarita.frnk.ui.theme.iconCheck
import dev.jdgarita.frnk.ui.theme.iconSearch
import dev.jdgarita.frnk.ui.theme.iconSettings
import dev.jdgarita.frnk.ui.theme.icons
import dev.jdgarita.frnk.ui.tokens.FrnkIconSize
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * Smoke harness for the toolkit. Sections, top to bottom:
 *  1. Theming + atoms — FrnkText / FrnkButton / FrnkIcon / FrnkIconButton / FrnkDivider read
 *     tokens from the active FrnkTheme.
 *  2. Koin + FeatureGate — exercises gate.canUse + gate.requestUpgrade.
 *  3. MVI engine — state/intent/effect flow through DemoViewModel.
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
    var showOnboarding by remember { mutableStateOf(false) }
    // Bump on each open so OnboardingScreen resolves a fresh VM instead of reusing the last
    // session's page index. Otherwise dismissing on page 3 and reopening would land back on page 3.
    var onboardingOpenCount by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(Theme[colors][colorBackground])) {
        DemoScreenContent(
            state = state,
            appearanceController = appearanceController,
            onShowOnboarding = {
                onboardingOpenCount++
                showOnboarding = true
            },
            onEffect = onEffect,
            onIntent = vm::send,
        )

        if (showOnboarding) {
            OnboardingScreen(
                initialState = demoOnboardingState(),
                modifier = Modifier.fillMaxSize(),
                vmKey = "demo-onboarding-$onboardingOpenCount",
                onEffect = { effect ->
                    when (effect) {
                        OnboardingEffect.CloseRequested,
                        OnboardingEffect.Completed,
                        -> showOnboarding = false
                    }
                },
            )
        }
    }
}

@Composable
private fun DemoScreenContent(
    state: DemoState,
    appearanceController: AppearanceController,
    onShowOnboarding: () -> Unit,
    onEffect: (DemoEffect) -> Unit,
    onIntent: (DemoIntent) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(FrnkSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(FrnkSpacing.md),
    ) {
        FrnkText(state = FrnkTextState.HeadlineSmall(text = "Frnk Toolkit Demo"))

        FrnkDivider(state = FrnkDividerState.Horizontal())

        Section(title = "Appearance (current: ${appearanceController.appearance.name})") {
            Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                AppearanceButton(Appearance.Light, "Light", appearanceController)
                AppearanceButton(Appearance.Dark, "Dark", appearanceController)
                AppearanceButton(Appearance.System, "System", appearanceController)
            }
        }

        FrnkDivider(state = FrnkDividerState.Horizontal())

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

        Section(title = "3. MVI") {
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

        FrnkDivider(state = FrnkDividerState.Horizontal())

        Section(title = "4. Onboarding scaffold") {
            FrnkText(
                state =
                    FrnkTextState.Body(
                        text =
                            "Fixed-shape paged tour with configurable pages. " +
                                "Tap to launch — X or Get Started to dismiss.",
                        color = colorOnSurfaceVariant,
                    ),
            )
            FrnkButton(
                state = FrnkButtonState(text = "Show Onboarding"),
                onClick = onShowOnboarding,
            )
        }
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

@Composable
private fun AppearanceButton(
    target: Appearance,
    label: String,
    controller: AppearanceController,
) {
    val isCurrent = controller.appearance == target
    FrnkButton(
        state =
            FrnkButtonState(
                text = label,
                variant = if (isCurrent) FrnkButtonVariant.Filled else FrnkButtonVariant.Outlined,
            ),
        onClick = { controller.appearance = target },
    )
}
