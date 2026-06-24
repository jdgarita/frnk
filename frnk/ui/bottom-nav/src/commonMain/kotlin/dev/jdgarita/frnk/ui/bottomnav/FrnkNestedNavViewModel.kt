package dev.jdgarita.frnk.ui.bottomnav

import dev.jdgarita.frnk.ui.mvi.MviViewModel

class FrnkNestedNavViewModel :
    MviViewModel<FrnkNestedNavArguments, FrnkNestedNavModelState, FrnkNestedNavScreenState, FrnkNestedNavIntent, FrnkNestedNavEffect>(
        factory = FrnkNestedNavModelStateFactory
    ) {
    override fun onAttached(arguments: FrnkNestedNavArguments) {
        super.onAttached(arguments)
        updateModel {
            copy(items = arguments.items)
        }
    }

    override fun mapToUiState(modelState: FrnkNestedNavModelState): FrnkNestedNavScreenState =
        FrnkNestedNavScreenState(
            items =
                modelState.items.map { frnkNavBarItemModel ->
                    FrnkNavBarItem(
                        key = frnkNavBarItemModel.key,
                        icon = frnkNavBarItemModel.icon,
                        iosSystemIcon = frnkNavBarItemModel.iosSystemIcon,
                        label = frnkNavBarItemModel.label
                    )
                },
            selectedIndex = modelState.selectedIndex
        )

    override suspend fun onIntent(intent: FrnkNestedNavIntent) {
        when (intent) {
            is FrnkNestedNavIntent.Tap -> {
                updateModel { copy(selectedIndex = intent.index) }
                currentModel().items.getOrNull(intent.index)?.let { item ->
                    emit(FrnkNestedNavEffect.Navigate(item.route))
                }
            }
        }
    }
}