package dev.jdgarita.frnk.ui.mvi

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composeunstyled.theme.Theme
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.scaffolds.FrnkScreenScaffold
import dev.jdgarita.frnk.ui.theme.spacing
import dev.jdgarita.frnk.ui.theme.spacingLg

/**
 * The toolkit's standard VM-backed screen — the state-hosting primitive host apps build their own
 * screens on. Binds an [MviViewModel] to the [FrnkScreenScaffold] page template so a host writes one
 * call and gets: lifecycle-aware state collection, a stateless `(state, onIntent)` content slot, the
 * fixed top bar, and (optionally) one-shot effect handling.
 *
 * ```
 * FrnkMviScreen(
 *     viewModel = koinViewModel<ProfileViewModel>(),
 *     topBar = FrnkTopAppBarState(title = "Profile"),
 *     onEffect = { e -> when (e) { is Navigate -> navigate(e.route) } },
 * ) { state, onIntent, padding ->
 *     ProfileContent(state, onIntent, Modifier.padding(padding))
 * }
 * ```
 *
 * **State** is collected with [collectAsStateWithLifecycle] (pauses while the host is below `STARTED`).
 * **Intents** flow back through the `onIntent` handed to [content] (it is `viewModel::send`).
 * **Effects** are consumed by an internal [EffectCollector] **only when [onEffect] is non-null**. The
 * effect stream is a single-consumer channel, so leave [onEffect] null when something else already
 * collects this view model's effects (e.g. a shared view model whose effects are handled centrally by
 * the host); the screen then just binds state + renders the scaffold.
 *
 * The top-bar callbacks ([onNavigationClick] / [onActionClick] / [onSearchQueryChange] /
 * [onSearchClose]) and [bottomInset] / [contentPadding] mirror [FrnkScreenScaffold] one-to-one; wire
 * the callbacks to `viewModel::send` when the bar should drive intents.
 */
@Composable
fun <S : UiState, I : UiIntent, E : UiEffect> FrnkMviScreen(
    viewModel: MviViewModel<S, I, E>,
    topBar: FrnkTopAppBarState,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
    contentPadding: PaddingValues = PaddingValues(Theme[spacing][spacingLg]),
    onNavigationClick: () -> Unit = {},
    onActionClick: (FrnkTopAppBarAction) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {},
    onSearchClose: () -> Unit = {},
    onEffect: ((E) -> Unit)? = null,
    content: @Composable (state: S, onIntent: (I) -> Unit, contentPadding: PaddingValues) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (onEffect != null) {
        EffectCollector(viewModel.effects, onEffect = onEffect)
    }

    FrnkScreenScaffold(
        topBar = topBar,
        modifier = modifier,
        bottomInset = bottomInset,
        contentPadding = contentPadding,
        onNavigationClick = onNavigationClick,
        onActionClick = onActionClick,
        onSearchQueryChange = onSearchQueryChange,
        onSearchClose = onSearchClose,
    ) { padding ->
        content(state, viewModel::send, padding)
    }
}
