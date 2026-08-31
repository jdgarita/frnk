package dev.jdgarita.frnk.demo.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.jdgarita.frnk.monetization.FeatureGate
import dev.jdgarita.frnk.ui.atoms.FrnkButton
import dev.jdgarita.frnk.ui.atoms.FrnkButtonState
import dev.jdgarita.frnk.ui.atoms.FrnkButtonVariant
import dev.jdgarita.frnk.ui.atoms.FrnkDivider
import dev.jdgarita.frnk.ui.atoms.FrnkDividerState
import dev.jdgarita.frnk.ui.atoms.FrnkText
import dev.jdgarita.frnk.ui.atoms.FrnkTextState
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValue
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueOrientation
import dev.jdgarita.frnk.ui.molecules.FrnkLabeledValueState
import dev.jdgarita.frnk.ui.mvi.FrnkScreen
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.scaffolds.LocalFrnkBottomBarInset
import dev.jdgarita.frnk.ui.scaffolds.home.FrnkHomeScreen
import dev.jdgarita.frnk.ui.scaffolds.home.HomeArguments
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.colorOnSurfaceVariant
import dev.jdgarita.frnk.ui.tokens.FrnkSpacing
import org.koin.compose.viewmodel.koinViewModel

/**
 * The demo's Home tab.
 *
 * Two ViewModels meet here, each with its own effect channel (single-consumer, so exactly one collector
 * apiece): the toolkit's pass-through `HomeViewModel` inside [FrnkHomeScreen] re-emits top-bar
 * interactions to [onEffect], while [DemoHomeViewModel] — the demo's own logic — is bound by
 * [FrnkScreen], which attaches it, collects its state lifecycle-aware, and consumes its
 * [DemoHomeEffect]s. Wiring only the first is what previously dropped every [DemoHomeEffect]: the
 * "Open Paywall" button navigated nowhere and no toast ever appeared.
 *
 * @param onOpenPaywall opens the paywall — the destination behind [DemoHomeEffect.Navigate].
 * @param onEffect receives the *toolkit* scaffold's [HomeEffect][dev.jdgarita.frnk.ui.scaffolds.home.HomeEffect]s.
 */
@Composable
fun HomeScreen(
    onOpenPaywall: () -> Unit,
    onEffect: (uiEffect: UiEffect) -> Unit
) {
    val homeViewModel: DemoHomeViewModel = koinViewModel()
    var message by remember { mutableStateOf<DemoMessage?>(null) }
    var messageCount by remember { mutableStateOf(0L) }

    FrnkScreen(
        viewModel = homeViewModel,
        arguments = DemoHomeArguments,
        onEffect = { uiEffect ->
            when (uiEffect) {
                is DemoHomeEffect.Navigate ->
                    // FeatureGate hands back a route *key* rather than a route so :monetization-api
                    // stays Compose- and nav-free; the host maps it onto its own graph.
                    if (uiEffect.routeKey == FeatureGate.PAYWALL_ROUTE_KEY) onOpenPaywall()

                is DemoHomeEffect.Toast -> {
                    messageCount += 1
                    message = DemoMessage(id = messageCount, text = uiEffect.message)
                }

                else -> Unit
            }
        }
    ) { state ->
        Box(modifier = Modifier.fillMaxSize()) {
            FrnkHomeScreen(
                frnkHomeArguments = HomeArguments(topBarTitle = FrnkStringSource.Raw("Frnk")),
                onEffect = onEffect
            ) {
                Section(title = "1. FeatureGate (Pro = ${state.isPro} via ${state.proSource})") {
                    Column(verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                            // Entry point #1 mirror: open the toolkit paywall (also reachable from the
                            // top-right crown action and from Settings).
                            FrnkButton(
                                state = FrnkButtonState.Content(text = "Open Paywall"),
                                onClick = { homeViewModel.send(DemoHomeIntent.RequestUpgrade) }
                            )
                            FrnkButton(
                                state =
                                    FrnkButtonState.Content(
                                        text = "Restore",
                                        variant = FrnkButtonVariant.Outlined
                                    ),
                                onClick = { homeViewModel.send(DemoHomeIntent.RestorePurchases) }
                            )
                        }
                        // God mode: a frnk-level Pro override (independent of RevenueCat), normally reached
                        // via Settings → tap version 7× → Developer. Surfaced here too for demo discoverability.
                        FrnkButton(
                            state =
                                FrnkButtonState.Content(
                                    text = if (state.isGodMode) "God mode: ON" else "God mode: OFF",
                                    variant = FrnkButtonVariant.Outlined
                                ),
                            onClick = { homeViewModel.send(DemoHomeIntent.ToggleGodMode) }
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
                                color = colorOnSurfaceVariant
                            )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                        FrnkButton(
                            state = FrnkButtonState.Content(text = "Add note"),
                            onClick = { homeViewModel.send(DemoHomeIntent.AddNote) }
                        )
                        FrnkButton(
                            state =
                                FrnkButtonState.Content(
                                    text = "Clear",
                                    variant = FrnkButtonVariant.Outlined,
                                    enabled = state.notes.isNotEmpty()
                                ),
                            onClick = { homeViewModel.send(DemoHomeIntent.ClearNotes) }
                        )
                    }
                    if (state.notes.isEmpty()) {
                        FrnkText(
                            state =
                                FrnkTextState.BodySmall(
                                    text = "No notes yet — tap Add note to persist one.",
                                    color = colorOnSurfaceVariant
                                )
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
                                color = colorOnSurfaceVariant
                            )
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                        FrnkButton(
                            state = FrnkButtonState.Content(text = "Track event"),
                            onClick = { homeViewModel.send(DemoHomeIntent.TrackEvent) }
                        )
                        FrnkButton(
                            state = FrnkButtonState.Content(text = "User property", variant = FrnkButtonVariant.Outlined),
                            onClick = { homeViewModel.send(DemoHomeIntent.SetUserProperty) }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                        FrnkButton(
                            state = FrnkButtonState.Content(text = "Log breadcrumb", variant = FrnkButtonVariant.Outlined),
                            onClick = { homeViewModel.send(DemoHomeIntent.LogBreadcrumb) }
                        )
                        FrnkButton(
                            state =
                                FrnkButtonState.Content(
                                    text = "Record non-fatal",
                                    variant = FrnkButtonVariant.Outlined
                                ),
                            onClick = { homeViewModel.send(DemoHomeIntent.RecordTestCrash) }
                        )
                    }
                    FrnkText(
                        state =
                            FrnkTextState.BodySmall(
                                text =
                                    "Force crash throws an UNHANDLED Kotlin exception — on iOS the CrashKiOS hook " +
                                        "(installed with firebaseObservabilityModule) reports it symbolicated; on Android " +
                                        "the Crashlytics SDK catches it. This terminates the app.",
                                color = colorOnSurfaceVariant
                            )
                    )
                    FrnkButton(
                        state =
                            FrnkButtonState.Content(
                                text = "Force crash (unhandled)",
                                variant = FrnkButtonVariant.Outlined
                            ),
                        onClick = { homeViewModel.send(DemoHomeIntent.ForceUnhandledCrash) }
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
                                color = colorOnSurfaceVariant
                            )
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
                                color = colorOnSurfaceVariant
                            )
                    )
                    FrnkLabeledValue(
                        state =
                            FrnkLabeledValueState.Content(
                                label = "Remote welcome",
                                value = state.remoteWelcome,
                                orientation = FrnkLabeledValueOrientation.Stacked
                            )
                    )
                    FrnkButton(
                        state = FrnkButtonState.Content(text = "Fetch Remote Config", variant = FrnkButtonVariant.Outlined),
                        onClick = { homeViewModel.send(DemoHomeIntent.FetchRemoteConfig) }
                    )
                    FrnkLabeledValue(state = FrnkLabeledValueState.Content(label = "Camera", value = state.cameraResult))
                    FrnkLabeledValue(
                        state = FrnkLabeledValueState.Content(label = "Camera permission", value = state.cameraPermission)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
                        FrnkButton(
                            state = FrnkButtonState.Content(text = "Capture photo", variant = FrnkButtonVariant.Outlined),
                            onClick = { homeViewModel.send(DemoHomeIntent.CapturePhoto) }
                        )
                        FrnkButton(
                            state = FrnkButtonState.Content(text = "Request camera", variant = FrnkButtonVariant.Outlined),
                            onClick = { homeViewModel.send(DemoHomeIntent.RequestCameraPermission) }
                        )
                    }
                }
            }

            DemoMessageOverlay(
                message = message,
                onDismissed = { message = null },
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = LocalFrnkBottomBarInset.current + FrnkSpacing.md)
            )
        }
    }
}

@Composable
private fun Section(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(FrnkSpacing.sm)) {
        FrnkText(state = FrnkTextState.Title(text = title))
        content()
    }
}