package dev.jdgarita.frnk.presentation.mvi

import dev.jdgarita.frnk.domain.framework.error.Error
import dev.jdgarita.frnk.domain.framework.outcome.Outcome
import dev.jdgarita.frnk.util.common.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

interface FrnkMviModel<
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ViewStateCommon<TViewState>,
    TExternalEvent : ExternalEvent
    > : MviViewModel<TArgs, TIntent, TViewState, TExternalEvent>, DataLoadingModel

/**
 * A common View Model that layers functionality over a basic MviModel
 *
 * Adds the concepts of
 *  - Data Loading. See [DataLoadingModel]
 *  - Common State. See [CommonState]
 */
abstract class BaseFrnkMviModel<
    TModelState : StateCommon<TModelState>,
    TArgs : Arguments,
    TIntent : Intent,
    TViewState : ViewStateCommon<TViewState>,
    TExternalEvent : ExternalEvent
    >(
    modelStateFactory: ModelStateFactory<TModelState>,
    private val viewModelDependencies: ViewModelDependencies
) : BaseMviModel<TModelState, TArgs, TIntent, TViewState, TExternalEvent>(
    modelStateFactory
),
    FrnkMviModel<TArgs, TIntent, TViewState, TExternalEvent>,
    DataLoadingModelInternal {

    protected val dataLoadState
        get() = modelState.commonState.dataLoadState

    private val _loadStateFlow: MutableStateFlow<LoadState> = MutableStateFlow(dataLoadState)
    override val loadStateFlow: Flow<LoadState> = _loadStateFlow

    private var screenName: String? = null

    /**
     * Indicates if the data should be reloaded when the view is activated.
     * Useful for when one wants to refresh the data when going back to a screen.
     */
    protected open val refreshOnActive: Boolean = false

    override suspend fun reloadData() {
        if (dataLoadState == LoadState.Initialized) {
            Log.w { "Reloading data before initial load" }
            return
        }
        updateDataLoadState(LoadState.Initialized)
        onDataCleared()
        startDataLoad(isReloading = true)
    }

    override fun onActive() {
        if (refreshOnActive) {
            updateDataLoadState(LoadState.Initialized)
        }

        (this as? ScreenViewModel<*, *, *, *>)?.let {
            screenName = "screen name"
            // screenName = viewModelDependencies.screenNavigationTracker.currentScreen?.name
            // viewModelDependencies.logger.breadcrumb("+ $screenName")
        }

        launchAttached {
            when (dataLoadState) {
                LoadState.Failed,
                LoadState.Initialized -> startDataLoad(isReloading = false)

                LoadState.Loaded -> onDataLoaded()
                is LoadState.Loading -> {
                }
            }
        }

        super.onActive()
    }

    override fun onInactive() {
        if (dataLoadState is LoadState.Loading) {
            updateDataLoadState(LoadState.Initialized)
        }

        (this as? ScreenViewModel<*, *, *, *>)?.let {
            // viewModelDependencies.logger.breadcrumb("- $screenName")
        }

        modelState.commonState.toastAlertViewState?.let {
            updateCommonState { it.copy(toastAlertViewState = null) }
        }

        super.onInactive()
    }

    private fun updateDataFailedToLoadState(error: Any? = null) =
        updateDataLoadState(loadState = LoadState.Failed, error = error as? Error)

    private fun updateDataLoadState(loadState: LoadState, error: Error? = null) {
        updateCommonState { it.copy(dataLoadState = loadState, error = error) }
    }

    private fun updateDataLoadedState() {
        updateCommonState { it.copy(dataLoadState = LoadState.Loaded, hasSuccessfullyLoaded = true, error = null) }
    }

    private suspend fun startDataLoad(isReloading: Boolean) {
        try {
            loadData(isReloading)
                .doOnSuccess {
                    updateDataLoadedState()
                    onDataLoaded()
                }.doOnError {
                    updateDataFailedToLoadState(it)
                    onDataLoadError()
                }
        } catch (ex: CancellationException) {
            // coroutine was cancelled during load, set it back to initialized
            if (dataLoadState != LoadState.Loaded) {
                updateDataLoadState(LoadState.Initialized)
            }
        }
    }

    override suspend fun handleCommonIntent(intent: CommonIntent) {
        when (intent) {
            is CommonIntent.OnRefresh -> reloadData()
            is CommonIntent.OnBackPressed -> handleExternalEvent(CommonExternalEvent.DidPressBack())
        }
    }

    private fun updateCommonState(block: (CommonState) -> CommonState) =
        updateState { it.copyCommon(block(modelState.commonState)) }
}

/**
 * Indicates the Mvi View Model is data load state aware.
 *
 * It ties data loading into the lifecycle of the view model.  It will attempt
 * to load data when the view is activated.
 *
 * See [LoadState]
 */
interface DataLoadingModel {
    suspend fun reloadData()

    val loadStateFlow: Flow<LoadState>
}

interface DataLoadingModelInternal {

    /**
     * To be implemented by subclass. This method should load the data
     * needed by the view model
     *
     * @param isReloading indicates if the data is being reloaded
     */
    suspend fun loadData(isReloading: Boolean): Outcome<*, *> = Outcome.Success(Unit)

    /**
     * To be implemented by subclass.  This method will be called on data load success
     */
    suspend fun onDataLoaded() = Unit

    /**
     * To be implemented by subclass.  This method will be called on data load error
     */
    suspend fun onDataLoadError() = Unit

    /**
     * To be implemented by subclass.  This method should clear any loaded data
     */
    suspend fun onDataCleared() = Unit
}