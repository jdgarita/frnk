package dev.jdgarita.frnk.presentation.mvi

import dev.jdgarita.frnk.util.common.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The base implementation of the MviModel.  Handles the core state management loop, which takes an
 * [Intent], which updates the internal [ModelState], which produces a new [ViewState].
 *
 * The [MviViewModel] and [LifecycleAware] interfaces are implemented by this class and cannot be
 * overwritten by subclasses.  The methods that should be implemented by subclasses are defined in
 * [ModelInternal].
 *
 * In addition to implementing [ModelInternal], subclasses will typically use the helper methods
 * [updateState] and [handleExternalEvent]
 *
 * @param singleThreadDispatcher The dispatcher to use for all coroutines
 * @param modelStateFactory The factory to use to create the initial [ModelState]
 */
abstract class BaseMviModel<
    TModelState : ModelState,
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ViewState,
    TExternalEvent : ExternalEvent
    >(
    singleThreadDispatcher: CoroutineDispatcher,
    modelStateFactory: ModelStateFactory<TModelState>
) : MviViewModel<TArgs, TIntent, TViewState, TExternalEvent>,
    LifecycleAware<TArgs>,
    ModelInternal<TModelState, TArgs, TIntent, TViewState> {

    /**
     * Scope tied to the lifetime of the attached view.  This scope is active as long as a view is
     * attached
     */
    private val viewScope = CoroutineScope(singleThreadDispatcher + SupervisorJob())

    /**
     * Scope tied to the LifecycleState when the ViewModel is in the [LifecyleState.ACTIVE] state.
     * Cancelled as soon as the state exits [LifecyleState.ACTIVE]
     */
    private val activeScope = CoroutineScope(singleThreadDispatcher + SupervisorJob())

    /**
     * Ensures state update loop happens atomically
     */
    private val stateMutex = Mutex()

    private val subscriptionJobs = mutableListOf<Job>()

    private var _modelState: TModelState = modelStateFactory.initialModelState()
    protected val modelState: TModelState
        get() = _modelState

    override val viewState
        get() = _innerViewStateFlow.value

    private val _innerViewStateFlow by lazy { MutableStateFlow(mapViewState(modelState)) }
    override val viewStateFlow: Flow<TViewState>
        get() = _innerViewStateFlow

    private val _innerExternalEventFlow = MutableSharedFlow<ExternalEvent>()
    override val externalEventFlow: Flow<ExternalEvent>
        get() = _innerExternalEventFlow

    final override fun onIntent(intent: TIntent) {
        Log.v { "${this::class.simpleName} onIntent $intent" }
        launchAttached {
            handleIntent(_modelState, intent)
        }
    }

    final override fun onIntent(intent: CommonIntent) {
        Log.v { "${this::class.simpleName} onIntent Common $intent" }

        launchAttached {
            handleCommonIntent(intent)
        }
    }

    /**
     * Handle an external event.  This simply provides the event via the [externalEventFlow]
     */
    protected fun handleExternalEvent(externalEvent: TExternalEvent) = launchAttached {
        Log.v { "${this::class.simpleName} handleExternalEvent $externalEvent" }
        _innerExternalEventFlow.emit(externalEvent)
    }

    protected fun handleExternalEvent(externalEvent: CommonExternalEvent) = launchAttached {
        Log.v { "${this::class.simpleName} handleExternalEvent Common $externalEvent" }
        _innerExternalEventFlow.emit(externalEvent)
    }

    /**
     * Handle a state update.  This will execute the state update block provided by the caller and
     * set the new state to the returned state.  The [ViewState] is then updated based on this
     * update.  This method executes atomically.
     *
     * @stateUpdate The callback method that returns the new state given the current state. Typically
     * used with currentState.copy()
     */
    protected fun updateState(stateUpdate: (currentState: TModelState) -> TModelState) {
        val lock = Any()
        val locked = stateMutex.tryLock(lock)
        if (locked) {
            _modelState = stateUpdate(_modelState)
            _innerViewStateFlow.value = mapViewState(_modelState)
            if (stateMutex.holdsLock(lock)) {
                stateMutex.unlock(lock)
            }
        } else {
            Log.i { "updateState failed to acquire lock" }
            updateStateAsync(stateUpdate)
        }
    }

    /**
     * Same as [updateState], with an additional block to execute after state update completes.
     */
    protected fun updateStateAfter(
        stateUpdate: (currentState: TModelState) -> TModelState,
        afterUpdate: suspend (() -> Unit)
    ) {
        launchAttached {
            updateStateAsync(stateUpdate).join()
            afterUpdate()
        }
    }

    private fun updateStateAsync(stateUpdate: (currentState: TModelState) -> TModelState): Job =
        launchAttached {
            stateMutex.withLock {
                _modelState = stateUpdate(_modelState)
                _innerViewStateFlow.value = mapViewState(_modelState)
            }
        }

    /**
     * Launch a coroutine that is active while the ViewModel state is [LifecyleState.ATTACHED] or [LifecyleState.ACTIVE]
     */
    protected fun launchAttached(block: suspend () -> Unit) = viewScope.launch { block() }

    /**
     * Launch a coroutine that is active only when the ViewModel state is [LifecyleState.ACTIVE]
     */
    protected fun launchActive(block: suspend () -> Unit) = activeScope.launch { block() }

    private var _lifecycleState: LifecyleState = LifecyleState.UNATTACHED
    override val lifecycleState: LifecyleState
        get() = _lifecycleState

    final override fun attachView(args: TArgs) {
        _lifecycleState = LifecyleState.ATTACHED
        onAttached(args)
        Log.v { "${this::class.simpleName} attachView $args" }
    }

    final override fun activate() {
        if (_lifecycleState == LifecyleState.UNATTACHED) {
            throw IllegalStateException("onActive() called before onViewAttached()")
        }

        _lifecycleState = LifecyleState.ACTIVE
        onActive()
        Log.v { "${this::class.simpleName} activate" }
    }

    override val active: Boolean
        get() = _lifecycleState == LifecyleState.ACTIVE

    final override fun deactivate() {
        if (_lifecycleState == LifecyleState.UNATTACHED) {
            Log.w("${this::class.simpleName} deactivate() called after detachView()")
            return
        }
        Log.v { "${this::class.simpleName} deactivate" }
        _lifecycleState = LifecyleState.ATTACHED

        onInactive()
        activeScope.coroutineContext.cancelChildren()
    }

    final override fun detachView() {
        Log.v { "${this::class.simpleName} detachView" }
        _lifecycleState = LifecyleState.UNATTACHED

        onDetached()

        activeScope.coroutineContext.cancelChildren()
        viewScope.coroutineContext.cancelChildren()
    }

    final override fun subscribeExternalEvents(onEvent: (ExternalEvent) -> Unit) {
        subscriptionJobs.add(
            launchAttached {
                externalEventFlow.collect {
                    onEvent(it)
                }
            }
        )
    }

    final override fun cancelCallbackSubscriptions() {
        subscriptionJobs.forEach {
            it.cancel()
        }
        subscriptionJobs.clear()
    }

    override fun onAttached(args: TArgs) = Unit
    override fun onInactive() = Unit
    override fun onActive() = Unit
    override fun onDetached() = Unit
}

/**
 * Methods to be implemented by an implementing MVI View Model
 */
internal interface ModelInternal<
    TModelState : ModelState,
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ViewState
    > {

    /**
     * Called when a new view is attached
     */
    fun onAttached(args: TArgs)

    /**
     * Called when the view has become active
     */
    fun onActive()

    /**
     * Called when the view has become inactive
     */
    fun onInactive()

    /**
     * Called
     */
    fun onDetached()

    /***
     * Map the current internal Model State to the external View State
     */
    fun mapViewState(currentState: TModelState): TViewState

    /**
     * Handle a new intent given the current state
     */
    suspend fun handleIntent(currentState: TModelState, intent: TIntent)

    /**
     * Handle a new [CommonIntent]
     */
    suspend fun handleCommonIntent(intent: CommonIntent) = Unit
}

/**
 * The internal state of this model, representing all information needed to recreate the state
 */
interface ModelState

interface ModelStateFactory<TModelState : ModelState> {
    fun initialModelState(): TModelState
}