package dev.jdgarita.frnk.ui.scaffolds

import dev.jdgarita.frnk.ui.mvi.MviViewModel

/**
 * Thin UI-state machine for `BottomNavScaffold`. Owns only the selected-tab index: it reduces a
 * [BottomNavIntent.TabSelected] into state and re-emits it as a [BottomNavEffect.TabSelected] so the
 * host can act on the switch (swap content, navigate, log analytics). No-op taps on the already-
 * selected tab neither change state nor emit an effect.
 */
class BottomNavViewModel(
    initial: BottomNavScaffoldState,
) : MviViewModel<BottomNavScaffoldState, BottomNavIntent, BottomNavEffect>(initial) {
    override suspend fun onIntent(intent: BottomNavIntent) {
        when (intent) {
            is BottomNavIntent.TabSelected -> {
                val index = intent.index.coerceIn(0, currentState().tabs.lastIndex)
                if (index == currentState().selectedIndex) return
                setState { copy(selectedIndex = index) }
                emit(BottomNavEffect.TabSelected(key = currentState().tabs[index].key, index = index))
            }
        }
    }
}
