package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBar
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBarDefaults
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavBarState
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.mvi.EffectCollector
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

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
    val state by vm.state.collectAsStateWithLifecycle()

    EffectCollector(vm.effects, onEffect = onEffect)

    BottomNavScaffoldContent(
        state = state,
        onIntent = vm::send,
        modifier = modifier,
        tabContent = tabContent,
    )
}

/**
 * Stateless renderer: the selected destination fills the whole area and the [FrnkBottomNavBar] floats
 * on top, pinned to the bottom and permanently visible. Because the bar's wrapper is transparent, the
 * destination shows through around and behind the pill as it scrolls. [tabContent] is handed a
 * [PaddingValues] whose bottom equals [FrnkBottomNavBarDefaults.reservedHeight] (the pill's body plus
 * the system-nav inset it floats above) — apply it to scrollable content so the last item rests just
 * above the pill rather than under it (or under the system nav). [onIntent] receives
 * [BottomNavIntent.TabSelected] when a tab is tapped.
 */
@Composable
fun BottomNavScaffoldContent(
    state: BottomNavScaffoldState,
    onIntent: (BottomNavIntent) -> Unit,
    modifier: Modifier = Modifier,
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
            // Full bar footprint = body + the system-nav inset the pill floats above, so the last item
            // clears both the pill and the system nav (see FrnkBottomNavBarDefaults.reservedHeight).
            PaddingValues(bottom = FrnkBottomNavBarDefaults.reservedHeight),
        )

        FrnkBottomNavBar(
            state =
                FrnkBottomNavBarState(
                    items = state.tabs.map { FrnkBottomNavItem(key = it.key, icon = it.icon, label = it.label) },
                    selectedIndex = state.selectedIndex,
                ),
            onItemSelected = { onIntent(BottomNavIntent.TabSelected(it)) },
            // Pin the pill above the system navigation bar (edge-to-edge hosts) so it never sits over
            // the system nav buttons. The inset belongs here at the bottom-pinned call site, not in the
            // position-agnostic atom; reservedHeight above adds the same inset so content clears it.
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars),
        )
    }
}
