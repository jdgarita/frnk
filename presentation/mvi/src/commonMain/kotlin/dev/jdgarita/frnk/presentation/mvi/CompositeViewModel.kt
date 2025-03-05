package dev.jdgarita.frnk.presentation.mvi

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine

interface DynamicViewModelParent {

    // One dimensional list (for now) of child view models
    val dynamicChildViewModels: List<MviViewModel<out Arguments, *, *, *>>

    /**
     * Whether dynamic child view models should be refreshed when parent is refreshed.
     * Usually it's not necessary because dynamic child view models are recreated when parent is refreshed
     * so it might result in duplicated data loading for dynamic child view models.
     */
    val dynamicChildViewModelsRefreshable: Boolean
    fun <TArguments : Arguments> addDynamicChildViewModel(child: MviViewModel<TArguments, *, *, *>, args: TArguments)
    fun clearDynamicChildViewModels()
}

abstract class CompositeViewModel<
    TModelState : CompositeStateCommon<TModelState>,
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ViewStateCommon<TViewState>,
    TExternalEvent : ExternalEvent
    >(
    childViewModels: List<MviViewModel<out Arguments, *, *, *>>,
    private val viewModelDependencies: ViewModelDependencies,
    modelStateFactory: ModelStateFactory<TModelState>
) : BaseFrnkMviModel<TModelState, TArgs, TIntent, TViewState, TExternalEvent>(
    modelStateFactory = modelStateFactory,
    viewModelDependencies = viewModelDependencies
),
    MviViewModel<TArgs, TIntent, TViewState, TExternalEvent>,
    LifecycleAware<TArgs>,
    DynamicViewModelParent {

    abstract fun handleChildExternalEvent(externalEvent: ExternalEvent)

    override val dynamicChildViewModelsRefreshable: Boolean = false

    private val _initialChildViewModels: List<MviViewModel<out Arguments, *, *, *>> = childViewModels
    private val _dynamicChildViewModels: MutableList<Pair<MviViewModel<out Arguments, *, *, *>, Arguments>> =
        mutableListOf()
    override val dynamicChildViewModels: List<MviViewModel<out Arguments, *, *, *>>
        get() = _dynamicChildViewModels.map { it.first }

    private val childViewModels: List<MviViewModel<out Arguments, *, *, *>>
        get() = _initialChildViewModels + _dynamicChildViewModels.map { it.first }

    private var childrenCommonViewStateJob: Job? = null

    override fun <TArguments : Arguments> addDynamicChildViewModel(
        child: MviViewModel<TArguments, *, *, *>,
        args: TArguments
    ) {
        _dynamicChildViewModels.add(Pair(child, args))
        child.attachView(args)

        child.cancelCallbackSubscriptions()
        child.subscribeExternalEvents {
            if (it is CommonExternalEvent) {
                handleExternalEvent(it)
            } else {
                handleChildExternalEvent(it)
            }
        }

        observeChildrenCommonViewState()
        if (active) {
            child.activate()
        }
    }

    override fun clearDynamicChildViewModels() {
        _dynamicChildViewModels.forEach {
            it.first.deactivate()
            it.first.detachView()
        }
        _dynamicChildViewModels.clear()
    }

    override fun onAttached(args: TArgs) {
        super.onAttached(args)

        observeChildrenCommonViewState()
        attachChildren(args)
    }

    private fun observeChildrenCommonViewState() {
        // Take a snapshot of the current child view models before launching the coroutine
        // otherwise we risk concurrent modification exceptions
        val childViewModelsSnapshot = childViewModels
        childrenCommonViewStateJob?.cancel()
        childrenCommonViewStateJob = launchAttached {
            val childCommonViewStateFlows = childViewModelsSnapshot
                .filter { it.viewState is ViewStateCommon<*> }
                .map { it.viewStateFlow }

            combine(childCommonViewStateFlows) { viewStates ->
                val commonStates = viewStates.map { (it as ViewStateCommon<*>).commonViewState }
                val loadingStates = commonStates.filter { it.dataLoadState is LoadState.Loading }
                val isRefreshing = loadingStates.any { (it.dataLoadState as LoadState.Loading).isRefreshing }

                when {
                    loadingStates.isNotEmpty() -> CommonViewState(
                        dataLoadState = LoadState.Loading(isRefreshing)
                    )

                    commonStates.any { it.dataLoadState == LoadState.Failed } -> {
                        val stateWithDisplayError = commonStates.firstOrNull { it.commonDisplayError != null }
                        CommonViewState(
                            dataLoadState = LoadState.Failed,
                            commonDisplayError = stateWithDisplayError?.commonDisplayError
                        )
                    }

                    commonStates.all { it.dataLoadState == LoadState.Loaded } -> {
                        CommonViewState(
                            dataLoadState = LoadState.Loaded
                        )
                    }

                    else -> CommonViewState(
                        dataLoadState = LoadState.Initialized
                    )
                }
            }.collect { commonViewState ->
                handleAggregatedChildrenCommonStateUpdate(commonViewState)
            }
        }
    }

    protected open fun handleAggregatedChildrenCommonStateUpdate(commonViewState: CommonViewState) {
        updateState { state ->
            state.copyCommon(commonViewState)
        }
    }

    private fun attachChildren(args: TArgs) {
        childViewModels.forEach { childViewModel ->
            childViewModel.cancelCallbackSubscriptions()
            childViewModel.subscribeExternalEvents {
                if (it is CommonExternalEvent) {
                    handleExternalEvent(it)
                } else {
                    handleChildExternalEvent(it)
                }
            }
            @Suppress("UNCHECKED_CAST")
            childViewModel as MviViewModel<Arguments, *, *, *>
            childViewModel.attachView(getChildArgumentsInternal(childViewModel, args))
        }
    }

    /**
     * This method should return arguments for given child view model. It is called in [attachView] for every child.
     *
     * @param child child view model
     * @param args arguments passed to [attachView]
     */
    protected abstract fun getChildArguments(child: MviViewModel<Arguments, *, *, *>, args: TArgs): Arguments

    private fun getChildArgumentsInternal(child: MviViewModel<Arguments, *, *, *>, args: TArgs): Arguments =
        _dynamicChildViewModels.find {
            it.first == child
        }?.second ?: getChildArguments(child, args)

    override fun onActive() {
        super.onActive()
        activateChildren()
    }

    private fun activateChildren() {
        childViewModels.forEach {
            try {
                it.activate()
            } catch (ex: IllegalStateException) {
                val exception = IllegalStateException(
                    "onActive() called before onViewAttached() on child ViewModel ${this::class.simpleName}"
                )
//                viewModelDependencies.logger.log(
//                    severity = TelemetryLogger.Severity.ERROR,
//                    message = "onActive() called before onViewAttached() on child ViewModel ${this::class.simpleName}",
//                    throwable = exception
//                )
                throw exception
            }
        }
    }

    override suspend fun handleCommonIntent(intent: CommonIntent) {
        super.handleCommonIntent(intent)

        when (intent) {
            CommonIntent.OnBackPressed -> Unit // Do nothing
            CommonIntent.OnRefresh -> {
                val refreshableChildren = if (dynamicChildViewModelsRefreshable) {
                    childViewModels
                } else {
                    _initialChildViewModels
                }
                refreshableChildren.forEach {
                    if (it is ModelInternal<*, *, *, *>) {
                        it.handleCommonIntent(intent)
                    }
                }
            }

            else -> Unit // Already handled via super
        }
    }

    override fun onInactive() {
//        viewModelDependencies.logger.log(
//            TelemetryLogger.Severity.DEBUG,
//            message = "deactivate ${this::class.simpleName} CompositeViewModel"
//        )
        deactivateChildren()
        super.onInactive()
    }

    private fun deactivateChildren() {
        childViewModels.forEach { it.deactivate() }
    }

    override fun onDetached() {
//        viewModelDependencies.logger.log(
//            TelemetryLogger.Severity.DEBUG,
//            message = "detachView ${this::class.simpleName} CompositeViewModel"
//        )
        detachChildren()
        super.onDetached()
    }

    private fun detachChildren() {
        childViewModels.forEach { it.detachView() }
    }
}