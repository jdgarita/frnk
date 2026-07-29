package dev.jdgarita.frnk.ui.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.backhandler.BackHandler
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle

/**
 * Signals that an ancestor already owns system back for this subtree — a `FrnkNavDisplay` (which pops its
 * own stack) and, above a tabbed surface, the scaffold's back convention. A [FrnkScreen] rendered under
 * such a host should therefore **not** install its own back handler by default, so back flows to the
 * NavDisplay (within-stack pops) / the host (e.g. the tab-root → Home convention) instead of being
 * swallowed at the leaf.
 *
 * Defaults to `false`, so a **standalone** `FrnkScreen` (no NavDisplay ancestor) keeps its built-in
 * handler. `FrnkNavDisplay` provides `true` around its entries. A screen that genuinely needs to intercept
 * system back can still opt back in with `FrnkScreen(..., handleBackPressed = true)`.
 */
val LocalFrnkBackHandledByHost = compositionLocalOf { false }

/**
 * Screen wrapper that attaches a [MviViewModel] to the composition's lifecycle (calling
 * [MviViewModel.attach] once on first attach), collects its state lifecycle-aware, consumes its effect
 * channel, and renders [content] with the collected state.
 *
 * It also installs a `BackHandler` (routing system back to [CommonUiIntent.OnBackPressed]) **when**
 * [handleBackPressed] is true. That flag defaults to `!`[LocalFrnkBackHandledByHost]`.current`, so a
 * standalone screen keeps the handler while a screen rendered inside a `FrnkNavDisplay` defers system back
 * to the display + host. Pass it explicitly to override (e.g. `false` for a host that owns back itself, or
 * `true` to force-intercept inside a NavDisplay).
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun <A : Arguments, M : ModelState, S : UiState, I : UiIntent, E : UiEffect> FrnkScreen(
    viewModel: MviViewModel<A, M, S, I, E>,
    arguments: A,
    initialLifecycleState: Lifecycle.State = Lifecycle.State.STARTED,
    handleBackPressed: Boolean = !LocalFrnkBackHandledByHost.current,
    onEffect: (uiEffect: UiEffect) -> Unit,
    content: @Composable (state: S) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val state by viewModel.state.collectAsStateWithLifecycle()

    val currentOnEffect by rememberUpdatedState(onEffect)

    if (handleBackPressed) {
        BackHandler(enabled = true) {
            viewModel.onIntent(CommonUiIntent.OnBackPressed)
        }
    }

    DisposableEffect(lifecycleOwner, viewModel) {
        viewModel.attach(arguments)
        onDispose { }
    }

    LaunchedEffect(viewModel.effects, lifecycleOwner, initialLifecycleState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(initialLifecycleState) {
            viewModel.effects.collect { newUiEffect ->
                currentOnEffect(newUiEffect)
            }
        }
    }

    LaunchedEffect(viewModel.commonEffects, lifecycleOwner, initialLifecycleState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(initialLifecycleState) {
            viewModel.commonEffects.collect { newUiEffect ->
                currentOnEffect(newUiEffect)
            }
        }
    }

    content(state)
}