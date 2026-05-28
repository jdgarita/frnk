package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBar
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBarDefaults
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBarState
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.math.roundToInt

/**
 * VM-backed convenience wrapper around [BottomNavScaffoldContent]. Resolves a [BottomNavViewModel]
 * from Koin (initialised with [initialState] via `parametersOf`), forwards its state to the
 * stateless renderer, and surfaces one-shot effects to [onEffect].
 *
 * The scaffold owns **which tab is selected and renders the floating bar** — it does not manage a
 * navigation back stack. The host renders each destination through [tabContent], switching on
 * [BottomNavTab.key] (or rendering another scaffold, e.g. `SettingsScreen`, for a given tab).
 *
 * [vmKey] scopes the ViewModel in the host's `ViewModelStore` exactly as in [OnboardingScreen] /
 * [SettingsScreen]; pass a changing key to reset the selected tab on each remount, or leave it null
 * to retain the selection for the lifetime of the enclosing `ViewModelStoreOwner`.
 *
 * Hosts that already own their selected-tab state (or use a different DI/MVI stack) should call
 * [BottomNavScaffoldContent] directly.
 */
@Composable
fun BottomNavScaffold(
    initialState: BottomNavScaffoldState,
    modifier: Modifier = Modifier,
    vmKey: String? = null,
    onEffect: (BottomNavEffect) -> Unit = {},
    tabContent: @Composable (tab: BottomNavTab, contentPadding: PaddingValues) -> Unit,
) {
    val vm: BottomNavViewModel = koinViewModel(key = vmKey) { parametersOf(initialState) }
    val state by vm.state.collectAsState()

    // See OnboardingScreen for why the effect handler is wrapped in rememberUpdatedState: the
    // collector is keyed on `vm`, so a recomposed lambda must be observed without restarting it.
    val currentOnEffect by rememberUpdatedState(onEffect)
    LaunchedEffect(vm) { vm.effects.collect { currentOnEffect(it) } }

    BottomNavScaffoldContent(
        state = state,
        onIntent = vm::send,
        modifier = modifier,
        tabContent = tabContent,
    )
}

/**
 * Stateless renderer: the selected destination fills the whole area and the [FrnkBottomNavBar] floats
 * on top, aligned to the bottom. Because the bar's wrapper is transparent, the destination shows
 * through around and behind the pill as it scrolls. [tabContent] is handed a [PaddingValues] whose
 * bottom equals [FrnkBottomNavBarDefaults.BarHeight] — apply it to scrollable content so the last
 * item can rest just above the pill rather than under it. [onIntent] receives
 * [BottomNavIntent.TabSelected] when a tab is tapped.
 *
 * Pass [collapsibleBars] (the same instance the destinations' [FrnkScreenScaffold]s use) to make the
 * bar slide down off-screen as content scrolls down and back up when it scrolls up — in lock-step with
 * the top bar. Leave it null for a permanently visible bar.
 */
@Composable
fun BottomNavScaffoldContent(
    state: BottomNavScaffoldState,
    onIntent: (BottomNavIntent) -> Unit,
    modifier: Modifier = Modifier,
    collapsibleBars: CollapsibleBarsState? = null,
    tabContent: @Composable (tab: BottomNavTab, contentPadding: PaddingValues) -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Theme[colors][colorBackground]),
    ) {
        tabContent(
            state.selectedTab,
            PaddingValues(bottom = FrnkBottomNavBarDefaults.BarHeight),
        )

        val density = LocalDensity.current
        val barHeightPx = with(density) { FrnkBottomNavBarDefaults.BarHeight.toPx() }
        FrnkBottomNavBar(
            state =
                FrnkBottomNavBarState(
                    items = state.tabs.map { FrnkBottomNavItem(key = it.key, icon = it.icon, label = it.label) },
                    selectedIndex = state.selectedIndex,
                ),
            onItemSelected = { onIntent(BottomNavIntent.TabSelected(it)) },
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .then(
                        if (collapsibleBars != null) {
                            // Positive Y = slide down off the bottom edge to hide.
                            Modifier.offset {
                                IntOffset(x = 0, y = (collapsibleBars.collapseFraction * barHeightPx).roundToInt())
                            }
                        } else {
                            Modifier
                        },
                    ),
        )
    }
}
