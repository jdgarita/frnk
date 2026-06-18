package dev.jdgarita.frnk.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow

/**
 * Lifecycle-aware collector for an [MviViewModel]'s one-shot effect stream (or any [Flow] of
 * fire-and-forget events). This is the toolkit's single, correct way to consume effects from a
 * composable — use it instead of hand-rolling `LaunchedEffect(vm) { vm.effects.collect { ... } }`.
 *
 * Two things it gets right that the naive version doesn't:
 *  - **Lifecycle gating.** Collection runs only while the host is at least [minActiveState]
 *    (`STARTED` by default), via [repeatOnLifecycle]. When the screen goes to the background the
 *    collector is cancelled; because [MviViewModel]'s effect channel is `BUFFERED`, emissions queue
 *    and drain on resume rather than firing a navigation/toast at a screen the user can't see.
 *  - **Fresh callback.** [onEffect] is wrapped in [rememberUpdatedState], so a recomposition that
 *    supplies a new lambda (capturing fresh state) is observed by the long-lived collector instead
 *    of the first-composition lambda being captured forever.
 *
 * Note the effect stream is a single-consumer channel — collect it in exactly one place. If a screen
 * is bound by [FrnkMviScreen] with a non-null `onEffect`, don't also call [EffectCollector] on the
 * same view model's effects, and vice versa.
 */
@Composable
fun <E> EffectCollector(
    effects: Flow<E>,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: (E) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnEffect by rememberUpdatedState(onEffect)
    LaunchedEffect(effects, lifecycleOwner, minActiveState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            effects.collect { currentOnEffect(it) }
        }
    }
}