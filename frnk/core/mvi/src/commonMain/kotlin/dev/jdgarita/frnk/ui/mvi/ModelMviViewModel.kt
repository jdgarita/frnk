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
 * Model-first MVI base — the successor to [MviViewModel].
 *
 * Splits the state into two layers:
 *  - [ModelState] (`M`) — the **data only**, no UI decoration. The ViewModel mutates *this* in
 *    response to external sources (data sources, use cases, …) via [updateModel].
 *  - [UiState] (`S`) — the **rendered, decorated** state the UI collects ([state]). It is *never*
 *    set directly; it is **derived automatically** from the [ModelState] by [mapToUiState], which the
 *    base re-runs on every model change.
 *
 * No subclass receives an initial UiState, nor the initial ModelState directly: the initial model is
 * produced by a [ModelStateFactory], and the initial UiState is the factory's model mapped through
 * [mapToUiState]. So a feature only declares *how to seed the data* (the factory) and *how to decorate
 * it* (the mapper) — the engine owns the wiring.
 *
 * Runtime inputs arrive as [Arguments] (`A`) at **attach time**, not via the constructor: the Compose
 * lifecycle helper (`RememberMviLifecycle`) calls [attach] when the view first appears, which retains the
 * [arguments] and runs the [onAttached] hook once. Override [onAttached] to seed the model from the
 * arguments and start loads — that work runs when the screen is actually shown, not at construction.
 * Service dependencies (managers, trackers) stay as ordinary constructor params, separate from [Arguments].
 *
 * Like [MviViewModel] it also owns a `SharedFlow<I>` for fire-and-forget intents (replay = 0) and a
 * `Channel<E>` for one-shot effects (no replay, conflated). Override [onIntent] for side-effectful
 * work, mutate data with [updateModel], and surface one-shots with [emit].
 *
 * **Purity contract:** [mapToUiState] is invoked from the [state] field initializer (to seed the first
 * UiState), so it must be a *pure function of its [modelState][mapToUiState] argument* — it must not
 * read subclass properties that are initialized after the super constructor runs.
 */
abstract class ModelMviViewModel<A : Arguments, M : ModelState, S : UiState, I : UiIntent, E : UiEffect>(
    factory: ModelStateFactory<M>
) : ViewModel() {
    // The data-only layer. Mutate via [updateModel]; the UI never reads it (it collects [state]).
    private val modelState = MutableStateFlow(factory.initialModelState())

    private val _state = MutableStateFlow(mapToUiState(factory.initialModelState()))

    /** The rendered state the UI collects. Derived from [modelState]; never set directly. */
    val state: StateFlow<S> = _state.asStateFlow()

    private val _intents = MutableSharedFlow<I>(extraBufferCapacity = 16, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val intents = _intents.asSharedFlow()

    private val _effects = Channel<E>(capacity = Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var attached = false

    /** The arguments the view attached with. Available only after [attach]; reading it before throws. */
    protected lateinit var arguments: A
        private set

    init {
        // Trigger: re-derive the UiState automatically whenever the ModelState changes.
        viewModelScope.launch {
            modelState.onEach { _state.value = mapToUiState(it) }.collect()
        }
        viewModelScope.launch {
            _intents.onEach { onIntent(it) }.collect()
        }
    }

    fun send(intent: I) {
        _intents.tryEmit(intent)
    }

    /**
     * Called once by the Compose lifecycle helper when the view first attaches. Guards against re-attach
     * (a config change re-runs the helper, but the ViewModel survives), retains [arguments], then runs
     * [onAttached]. Not for subclasses to override — override [onAttached] instead.
     */
    fun attach(arguments: A) {
        if (attached) return
        attached = true
        this.arguments = arguments
        onAttached(arguments)
    }

    /**
     * Seed the model from [arguments] (e.g. `updateModel { ... }`) and start any loads. Runs **once**,
     * when the view first attaches — not at construction. Default: no-op.
     */
    protected open fun onAttached(arguments: A) {}

    /** The only state mutator: change the data; the UiState re-maps automatically. */
    protected fun updateModel(reducer: M.() -> M) {
        modelState.value = modelState.value.reducer()
    }

    protected fun currentModel(): M = modelState.value

    protected fun currentState(): S = _state.value

    protected suspend fun emit(effect: E) {
        _effects.send(effect)
    }

    /** Map the data-only [ModelState] to the decorated [UiState]. Must be pure in its argument. */
    protected abstract fun mapToUiState(modelState: M): S

    protected abstract suspend fun onIntent(intent: I)
}