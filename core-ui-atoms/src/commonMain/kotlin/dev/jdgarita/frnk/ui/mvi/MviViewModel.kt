package dev.jdgarita.frnk.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel for the MVI engine. Owns three streams:
 *  - state    : hot StateFlow rendered by the composable.
 *  - effects  : single-shot channel-backed flow (navigation, toasts).
 *  - actions  : SharedFlow of incoming user/system actions.
 *
 * Subclasses implement [reduce] to mutate state from an action, and may
 * use [emitEffect] to fire side effects.
 */
abstract class MviViewModel<S : UiState, A : UiAction, E : UiEffect>(
    initialState: S,
) : ViewModel() {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effects = Channel<E>(capacity = Channel.BUFFERED)
    val effects: Flow<E> = _effects.consumeAsFlow()

    private val _actions =
        MutableSharedFlow<A>(
            extraBufferCapacity = 64,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val actions: Flow<A> = _actions.asSharedFlow()

    /** Dispatch a user/system action. */
    fun dispatch(action: A) {
        _actions.tryEmit(action)
        viewModelScope.launch {
            val next = reduce(_state.value, action)
            _state.value = next
            onAction(action)
        }
    }

    /** Pure reducer: given current state + action, return next state. */
    protected abstract fun reduce(
        current: S,
        action: A,
    ): S

    /** Hook for impure work (network, db) triggered by an action. Optional. */
    protected open suspend fun onAction(action: A) = Unit

    protected fun emitEffect(effect: E) {
        viewModelScope.launch { _effects.send(effect) }
    }

    protected fun updateState(transform: (S) -> S) {
        _state.value = transform(_state.value)
    }
}
