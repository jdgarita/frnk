package dev.jdgarita.frnk.ui.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.jdgarita.frnk.ui.mvi.ext.collect
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Redux-lite MVI base. Features:
 *  - StateFlow<S> for the rendered state
 *  - SharedFlow<I> for fire-and-forget intents (replay = 0)
 *  - Channel<E> for one-shot effects (no replay, conflated)
 *
 * Override [onIntent] to perform side-effectful work and emit new state via [setState].
 */
abstract class MviViewModel<S : UiState, I : UiIntent, E : UiEffect>(
    initial: S,
) : ViewModel() {
    private val _state = MutableStateFlow(initial)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _intents = MutableSharedFlow<I>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val intents = _intents.asSharedFlow()

    private val _effects = Channel<E>(capacity = Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        viewModelScope.launch {
            _intents.onEach { onIntent(it) }.collect()
        }
    }

    fun send(intent: I) {
        _intents.tryEmit(intent)
    }

    protected fun setState(reducer: S.() -> S) {
        _state.value = _state.value.reducer()
    }

    protected fun currentState(): S = _state.value

    protected suspend fun emit(effect: E) {
        _effects.send(effect)
    }

    protected abstract suspend fun onIntent(intent: I)
}
