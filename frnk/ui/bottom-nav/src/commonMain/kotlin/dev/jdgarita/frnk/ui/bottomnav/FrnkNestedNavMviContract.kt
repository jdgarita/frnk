package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.mvi.Arguments
import dev.jdgarita.frnk.ui.mvi.ModelState
import dev.jdgarita.frnk.ui.mvi.ModelStateFactory
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState
import dev.jdgarita.frnk.ui.theme.FrnkIconSource

@Immutable
data class FrnkNestedNavScreenState(
    val items: List<FrnkNavBarItem>,
    val selectedIndex: Int
) : UiState

@Immutable
data class FrnkNavBarItemModel(
    val key: String,
    val icon: FrnkIconSource,
    val iosSystemIcon: String,
    val label: String
)

@Immutable
data class FrnkNestedNavModelState(
    val items: List<FrnkNavBarItemModel>,
    val selectedIndex: Int
) : ModelState

sealed interface FrnkNestedNavIntent : UiIntent {
    data class Tap(
        val index: Int
    ) : FrnkNestedNavIntent
}

@Immutable
data class FrnkNestedNavArguments(
    val items: List<FrnkNavBarItemModel>
) : Arguments

sealed interface FrnkNestedNavEffect : UiEffect {
    data class Navigate(
        val index: Int
    ) : FrnkNestedNavEffect
}

object FrnkNestedNavModelStateFactory : ModelStateFactory<FrnkNestedNavModelState> {
    override fun initialModelState() =
        FrnkNestedNavModelState(
            items = emptyList(),
            selectedIndex = 0
        )
}