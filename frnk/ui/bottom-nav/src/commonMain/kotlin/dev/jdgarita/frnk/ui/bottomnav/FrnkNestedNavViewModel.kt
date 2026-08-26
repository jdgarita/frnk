package dev.jdgarita.frnk.ui.bottomnav

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import dev.jdgarita.frnk.ui.mvi.MviViewModel
import dev.jdgarita.frnk.ui.nav.FrnkTabRoute
import dev.jdgarita.frnk.ui.nav.back
import dev.jdgarita.frnk.ui.nav.clearAndNavigateTo
import dev.jdgarita.frnk.ui.theme.FrnkIconSource
import dev.jdgarita.frnk.ui.theme.FrnkStringSource
import dev.jdgarita.frnk.ui.theme.iconNavHome
import dev.jdgarita.frnk.ui.theme.iconNavSettings
import dev.jdgarita.frnk.ui.theme.stringNavHome
import dev.jdgarita.frnk.ui.theme.stringSettings

/**
 * Owns the bar's view state (`items` + `selectedIndex`) **and** the per-tab navigation state for
 * [FrnkNestedNavScaffold]'s fixed three-tab (`Home · <custom> · Settings`) shell.
 *
 * **Model-owned per-tab data:** the per-tab roots and saved histories live in [FrnkNestedNavModelState]
 * (`roots` + `savedStacks`), seeded in [onAttached] from the host's [FrnkNestedNavArguments.customTab].
 * [backStack] is the single live stack the scaffold renders (and hands to the host's nested-nav module);
 * on a tab change the outgoing tab's history is snapshotted into `savedStacks` and the target tab's is
 * swapped into [backStack] — the canonical Navigation3 multiple-back-stack pattern. The Home tab (index 0)
 * starts at [FrnkTabRoute.Home].
 *
 * **Conventions:** re-tapping the active tab pops it back to its root; system/predictive back at a tab
 * root from a non-Home tab returns to Home.
 *
 * **In-memory only:** the stacks survive recomposition and configuration change (the VM outlives them)
 * but not full process death.
 */
class FrnkNestedNavViewModel :
    MviViewModel<FrnkNestedNavArguments, FrnkNestedNavModelState, FrnkNestedNavScreenState, FrnkNestedNavIntent, FrnkNestedNavEffect>(
        factory = FrnkNestedNavModelStateFactory,
        mapper = ::nestedNavScreenState
    ) {
    /** The single live stack the scaffold renders + hands to the host module. Seeded with the Home tab's root. */
    val backStack: NavBackStack<NavKey> = NavBackStack(FrnkTabRoute.Home)

    override fun onAttached(arguments: FrnkNestedNavArguments) {
        super.onAttached(arguments)
        val items =
            listOf(
                FrnkNavBarItemModel(
                    key = "Home",
                    icon = FrnkIconSource.Token(iconNavHome),
                    iosSystemIcon = "house",
                    label = FrnkStringSource.Token(stringNavHome),
                    route = FrnkTabRoute.Home
                ),
                FrnkNavBarItemModel(
                    key = arguments.customTab.key,
                    icon = arguments.customTab.icon,
                    iosSystemIcon = arguments.customTab.iosSystemIcon,
                    label = arguments.customTab.label,
                    route = arguments.customTab.route
                ),
                FrnkNavBarItemModel(
                    key = "Settings",
                    icon = FrnkIconSource.Token(iconNavSettings),
                    iosSystemIcon = "gearshape",
                    label = FrnkStringSource.Token(stringSettings),
                    route = FrnkTabRoute.Settings
                )
            )
        val roots = items.map { it.route }
        updateModel {
            copy(
                items = items,
                roots = roots,
                savedStacks = roots.map { listOf(it) }
            )
        }
    }

    override suspend fun onIntent(intent: FrnkNestedNavIntent) {
        when (intent) {
            is FrnkNestedNavIntent.Tap -> onTap(intent.index)
            FrnkNestedNavIntent.Back -> onBack()
        }
    }

    private fun onTap(index: Int) {
        val model = currentModel()
        if (index == model.selectedIndex) {
            // Re-tap the active tab → pop it back to its root.
            val root = model.roots[index]
            backStack.clearAndNavigateTo(root)
            updateModel { copy(savedStacks = savedStacks.replacedAt(index, listOf(root))) }
            return
        }
        switchTab(from = model.selectedIndex, to = index)
    }

    private fun onBack() {
        if (backStack.size > 1) {
            backStack.back()
            return
        }
        // At a tab root: a non-Home tab returns to Home; the Home root is the terminal back target.
        val current = currentModel().selectedIndex
        if (current != HOME_INDEX) {
            switchTab(from = current, to = HOME_INDEX)
        }
    }

    private fun switchTab(
        from: Int,
        to: Int
    ) {
        val outgoing = backStack.toList()
        updateModel {
            copy(
                savedStacks = savedStacks.replacedAt(from, outgoing),
                selectedIndex = to
            )
        }
        backStack.clear()
        backStack.addAll(currentModel().savedStacks[to])
    }

    private companion object {
        const val HOME_INDEX = 0

        /** Returns a copy of this list with [index] replaced by [value] — keeps `savedStacks` immutable. */
        private fun List<List<NavKey>>.replacedAt(
            index: Int,
            value: List<NavKey>
        ): List<List<NavKey>> = toMutableList().also { it[index] = value }
    }
}

private fun nestedNavScreenState(modelState: FrnkNestedNavModelState): FrnkNestedNavScreenState =
    FrnkNestedNavScreenState(
        items =
            modelState.items.map { item ->
                FrnkNavBarItem(
                    key = item.key,
                    icon = item.icon,
                    iosSystemIcon = item.iosSystemIcon,
                    label = item.label
                )
            },
        selectedIndex = modelState.selectedIndex
    )