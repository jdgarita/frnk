package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkBottomNavItem
import dev.jdgarita.frnk.ui.mvi.EffectCollector
import dev.jdgarita.frnk.ui.scaffolds.BottomNavEffect
import dev.jdgarita.frnk.ui.scaffolds.BottomNavIntent
import dev.jdgarita.frnk.ui.scaffolds.BottomNavScaffoldState
import dev.jdgarita.frnk.ui.scaffolds.BottomNavTab
import dev.jdgarita.frnk.ui.scaffolds.BottomNavViewModel
import dev.jdgarita.frnk.ui.theme.colorBackground
import dev.jdgarita.frnk.ui.theme.colors
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The toolkit's **default** platform-adaptive bottom-navigation scaffold: it owns *which tab is selected*
 * and renders the native [FrnkAdaptiveBottomNavBar] (UITabBar on iOS / Material3 NavigationBar on Android)
 * pinned below the selected destination. The host supplies each tab's screen through [tabContent] —
 * wiring its own per-tab navigation there — so frnk owns the bar + tab switching while the host owns the
 * tabs and their content.
 *
 * Reuses `shared-ui-atoms`' `BottomNavScaffoldState` / `BottomNavViewModel`, so the selected-tab state and
 * the `BottomNavEffect.TabSelected` one-shot (for analytics/navigation) behave exactly like the pill-based
 * `BottomNavScaffold`. Unlike that floating pill, the adaptive bar is an opaque native bar, so content is
 * laid **above** it (not behind it).
 *
 * Build [initialState] with [rememberFrnkBottomNavState] (guarantees Home + Settings). [vmKey] scopes the
 * ViewModel in the host's `ViewModelStore` (a changing key resets the selection); the per-scaffold Koin
 * module `bottomNavScaffoldModule` (in `shared-ui-atoms`) must be installed.
 */
@Composable
fun FrnkAdaptiveBottomNavScaffold(
    initialState: BottomNavScaffoldState,
    modifier: Modifier = Modifier,
    vmKey: String? = null,
    onEffect: (BottomNavEffect) -> Unit = {},
    tabContent: @Composable (tab: BottomNavTab, contentPadding: PaddingValues) -> Unit,
) {
    val vm: BottomNavViewModel = koinViewModel(key = vmKey) { parametersOf(initialState) }
    val state by vm.state.collectAsStateWithLifecycle()

    EffectCollector(vm.effects, onEffect = onEffect)

    FrnkAdaptiveBottomNavScaffoldContent(
        state = state,
        onIntent = vm::send,
        modifier = modifier,
        tabContent = tabContent,
    )
}

/**
 * Stateless renderer for [FrnkAdaptiveBottomNavScaffold] — used by previews and hosts that own their
 * selected-tab state. The selected destination fills the area above the pinned [FrnkAdaptiveBottomNavBar];
 * [tabContent] receives a zero bottom inset because the opaque bar sits below the content rather than over
 * it. [onIntent] receives [BottomNavIntent.TabSelected] when a tab is tapped.
 */
@Composable
fun FrnkAdaptiveBottomNavScaffoldContent(
    state: BottomNavScaffoldState,
    onIntent: (BottomNavIntent) -> Unit,
    modifier: Modifier = Modifier,
    tabContent: @Composable (tab: BottomNavTab, contentPadding: PaddingValues) -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(Theme[colors][colorBackground]),
    ) {
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            tabContent(state.selectedTab, PaddingValues())
        }
        FrnkAdaptiveBottomNavBar(
            items = state.tabs.map { FrnkBottomNavItem(key = it.key, icon = it.icon, label = it.label) },
            selectedIndex = state.selectedIndex,
            onItemSelected = { onIntent(BottomNavIntent.TabSelected(it)) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
