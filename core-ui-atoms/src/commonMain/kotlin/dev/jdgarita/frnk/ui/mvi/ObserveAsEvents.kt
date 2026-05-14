package dev.jdgarita.frnk.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Compose helper that consumes one-shot effects from an [MviViewModel]
 * without leaking emissions across recompositions.
 */
@Composable
fun <E : UiEffect> ObserveAsEvents(effects: Flow<E>, onEvent: (E) -> Unit) {
    LaunchedEffect(effects) {
        effects.collectLatest { onEvent(it) }
    }
}
