package dev.jdgarita.frnk.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner

/**
 * Attaches a [ModelMviViewModel] to the composition's lifecycle, supplying its [Arguments]. On first
 * composition it calls [ModelMviViewModel.attach], which retains the arguments and runs the VM's
 * `onAttached` hook **once** (the VM guards re-attach, so a config change that re-runs this effect is a
 * no-op). This is the toolkit's single, correct way to feed runtime arguments into a model-first VM —
 * call it once per screen, above (or alongside) state collection.
 *
 * `onDispose` is intentionally empty for now: the VM's coroutines live in `viewModelScope` and are
 * cancelled on `onCleared`, so there's nothing to tear down here. Resume/pause (activate/deactivate)
 * hooks can be added later via a [androidx.lifecycle.LifecycleEventObserver] in the same effect.
 */
@Composable
fun <A : Arguments> RememberMviLifecycle(
    viewModel: ModelMviViewModel<A, *, *, *, *>,
    arguments: A,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
) {
    DisposableEffect(lifecycleOwner, viewModel) {
        viewModel.attach(arguments)
        onDispose { }
    }
}
