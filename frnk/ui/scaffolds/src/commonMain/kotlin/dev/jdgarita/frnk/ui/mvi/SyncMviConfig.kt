package dev.jdgarita.frnk.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

/**
 * Feed a host-recomputed **config** state into a persistent [MviViewModel], reactively, instead of
 * re-creating the ViewModel to re-seed it.
 *
 * A VM-backed scaffold is seeded once with its initial state (via `koinViewModel { parametersOf(...) }`).
 * When the host later recomputes that state — e.g. a Settings/Home catalogue rebuilt because `isPro`
 * flipped — there is no constructor to re-run. This helper bridges the gap: on every later,
 * structurally-distinct [config] it dispatches `asIntent(config)` so the VM adopts the new structure
 * while keeping the interaction state its reducer owns.
 *
 * The seed pass is skipped: on first composition the VM already holds [config] (it was the
 * `parametersOf` seed), so `state.value === config` and nothing is sent. `LaunchedEffect` keys on
 * [config] by **structural** equality, and the toolkit state types are `@Immutable data class`es, so
 * this only relaunches when the config actually changes — a freshly-built-but-equal [config] is a
 * no-op. Keep [config] stably built (`rememberDefaultSettingsState`, `remember(topBar) { … }`, …) so
 * the comparison stays cheap.
 *
 * Single source of truth for the "sync external config into an MVI ViewModel" pattern — both
 * [SettingsScreen][dev.jdgarita.frnk.ui.scaffolds.settings.SettingsScreen] and
 * [HomeScreen][dev.jdgarita.frnk.ui.scaffolds.home.HomeScreen] use it. (Note: the merge itself runs through
 * the VM's intent channel, so a *live* config flip while the screen is composed lands on the next
 * frame; a screen that is (re)entered after the flip seeds correctly and never shows the old config.)
 *
 * @param viewModel the persistent ViewModel to keep in sync.
 * @param config the latest host-built state; pass a stably-remembered value.
 * @param asIntent wraps [config] into the VM's "config changed" intent (e.g. `SettingsIntent::ConfigChanged`).
 */
@Composable
fun <S : UiState, I : UiIntent> SyncMviConfig(
    viewModel: MviViewModel<S, I, *>,
    config: S,
    asIntent: (S) -> I
) {
    LaunchedEffect(config) {
        if (viewModel.state.value !== config) viewModel.send(asIntent(config))
    }
}