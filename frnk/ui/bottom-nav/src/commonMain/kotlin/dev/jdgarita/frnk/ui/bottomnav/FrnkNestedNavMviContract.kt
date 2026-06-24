package dev.jdgarita.frnk.ui.bottomnav

import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
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
    val label: String,
    val route: NavKey
)

/**
 * The model-first source of truth for [FrnkNestedNavViewModel]. Besides the bar `items` + `selectedIndex`,
 * it owns the **per-tab navigation data**:
 *  - [roots] — each tab's root destination, indexed like [items] (Home / custom / Settings),
 *  - [savedStacks] — each tab's saved history (the active tab's live history lives in
 *    [FrnkNestedNavViewModel.backStack] and is snapshotted back into [savedStacks] on a tab switch).
 *
 * All three lists start empty and are seeded in [FrnkNestedNavViewModel.onAttached] from the host's
 * [FrnkNestedNavArguments.customTab]; they are mutated only via `updateModel { copy(...) }`.
 */
@Immutable
data class FrnkNestedNavModelState(
    val items: List<FrnkNavBarItemModel>,
    val roots: List<NavKey>,
    val savedStacks: List<List<NavKey>>,
    val selectedIndex: Int
) : ModelState

sealed interface FrnkNestedNavIntent : UiIntent {
    data class Tap(
        val index: Int
    ) : FrnkNestedNavIntent

    /** System/predictive back at a tab root — handled by the VM (back-from-a-non-home-tab-root → Home). */
    data object Back : FrnkNestedNavIntent
}

/**
 * The host's middle [FrnkCustomTab], delivered to [FrnkNestedNavViewModel] at attach time (via the
 * scaffold's `FrnkScreen`). Home + Settings are toolkit-fixed; only the middle tab is supplied.
 */
@Immutable
data class FrnkNestedNavArguments(
    val customTab: FrnkCustomTab
) : Arguments

/**
 * The scaffold owns navigation internally (per-tab back stacks live in [FrnkNestedNavViewModel]), so it
 * emits no effects. Kept only to satisfy the [UiEffect] type parameter of the MVI base.
 */
sealed interface FrnkNestedNavEffect : UiEffect

object FrnkNestedNavModelStateFactory : ModelStateFactory<FrnkNestedNavModelState> {
    override fun initialModelState() =
        FrnkNestedNavModelState(
            items = emptyList(),
            roots = emptyList(),
            savedStacks = emptyList(),
            selectedIndex = 0
        )
}