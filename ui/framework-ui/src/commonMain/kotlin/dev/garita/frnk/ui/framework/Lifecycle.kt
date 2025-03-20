package dev.garita.frnk.ui.framework

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import dev.jdgarita.frnk.presentation.framework.navigation.NavigationRouter
import dev.jdgarita.frnk.presentation.mvi.Arguments
import dev.jdgarita.frnk.presentation.mvi.ExternalEvent
import dev.jdgarita.frnk.presentation.mvi.MviViewModel

/**
 * A composable that will attach the [viewModel] to the [lifecycleOwner] and will call [viewModel]'s lifecycle methods.
 * It will also collect the [viewModel]'s external events and call [onExternalEvent] for each one.
 *
 * @param viewModel The [MviViewModel] to attach to the [lifecycleOwner]
 * @param arguments The arguments to pass to the [viewModel]
 * @param lifecycleOwner The [LifecycleOwner] to attach to. Defaults to [LocalLifecycleOwner.current]
 */
@Composable
fun <T : Arguments, E : ExternalEvent> RememberLifecycle(
    navigationRouter: NavigationRouter,
    viewModel: MviViewModel<T, *, *, E>,
    arguments: T,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
) {
    val currentOnResume by rememberUpdatedState(viewModel::activate)
    val currentOnPause by rememberUpdatedState(viewModel::deactivate)
    DisposableEffect(lifecycleOwner) {
        viewModel.attachView(arguments)
        viewModel.subscribeExternalEvents { event ->
            navigationRouter.handleExternalEvent(event)
        }

        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> currentOnResume()
                Lifecycle.Event.ON_PAUSE -> currentOnPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.cancelCallbackSubscriptions()
        }
    }
}