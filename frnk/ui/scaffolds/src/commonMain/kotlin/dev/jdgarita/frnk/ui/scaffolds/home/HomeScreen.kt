package dev.jdgarita.frnk.ui.scaffolds.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.mvi.EffectCollector
import dev.jdgarita.frnk.ui.mvi.SyncMviConfig
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg
import dev.jdgarita.frnk.ui.theme.spacingMd
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * The toolkit's **home-tab page template**: a pinned [FrnkTopAppBar][dev.jdgarita.frnk.ui.atoms.FrnkTopAppBar]
 * over a vertically scrollable column the host fills through [content] — the standard "app landing
 * page" shape with zero scroll/inset wiring on the host side.
 *
 * VM-backed convenience wrapper around [HomeScreenContent]: resolves a [HomeViewModel] from Koin
 * (initialised with [initialState] via `parametersOf`), forwards its state to the stateless renderer,
 * and surfaces one-shot effects to [onEffect]. Mirrors [dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreen] exactly.
 *
 * **The scaffold owns the vertical scroll** (recorded slot decision): [content] supplies items into a
 * `Column` that already scrolls behind the pinned top bar and the host's floating bottom bar, with
 * the merged padding (status bar + bar heights + [contentPadding] + `LocalFrnkBottomBarInset`)
 * applied — mis-applying that padding is the #1 way content ends up stuck under the bars. A host
 * that needs a `LazyColumn` or custom scroll drops one level to
 * [dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold]/[FrnkMviScreen][dev.jdgarita.frnk.ui.mvi.FrnkMviScreen] instead.
 *
 * Like [dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreen], the VM reacts to [initialState]: seeded once via `parametersOf`, then every
 * later recomposition that yields a *new* [initialState] (e.g. a top-bar action that shows only while
 * Free, after `isPro` flips) is adopted via [HomeIntent.ConfigChanged]. So **leave [vmKey] at its
 * default**; pass a fresh [initialState] (stably `remember`-ed) rather than re-keying to re-seed.
 *
 * [vmKey] scopes the ViewModel inside the host's `ViewModelStore`; change it only to force a fresh VM.
 * [HomeScreenContent] is the module-internal stateless renderer (previews + this wrapper), not host API.
 */
@Composable
fun HomeScreen(
    initialState: HomeScreenState,
    modifier: Modifier = Modifier,
    vmKey: String? = null,
    contentPadding: PaddingValues = PaddingValues(Theme[spacing][spacingLg]),
    onEffect: (HomeEffect) -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val vm: HomeViewModel = koinViewModel(key = vmKey) { parametersOf(initialState) }
    val state by vm.state.collectAsStateWithLifecycle()

    // Sync later chrome recomputes (e.g. a Free-only top-bar action after isPro flipped) into the live VM.
    SyncMviConfig(vm, initialState, HomeIntent::ConfigChanged)

    EffectCollector(vm.effects, onEffect = onEffect)

    HomeScreenContent(
        state = state,
        onIntent = vm::send,
        modifier = modifier,
        contentPadding = contentPadding,
        content = content
    )
}

/**
 * Module-internal stateless renderer behind [HomeScreen] (previews + the VM-backed wrapper):
 * [dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold] with the top bar driven by [state], and a scrolling `Column` that applies the
 * scaffold's merged padding so [content] clears the pinned bars at rest.
 */
@Composable
internal fun HomeScreenContent(
    state: HomeScreenState,
    onIntent: (HomeIntent) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Theme[spacing][spacingLg]),
    content: @Composable ColumnScope.() -> Unit
) {
    FrnkScreenScaffold(
        topBar = state.topBar,
        modifier = modifier,
        contentPadding = contentPadding,
        onNavigationClick = { onIntent(HomeIntent.NavigationClicked) },
        onActionClick = { onIntent(HomeIntent.TopBarActionClicked(it)) }
    ) { mergedPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(mergedPadding),
            verticalArrangement = Arrangement.spacedBy(Theme[spacing][spacingMd]),
            content = content
        )
    }
}