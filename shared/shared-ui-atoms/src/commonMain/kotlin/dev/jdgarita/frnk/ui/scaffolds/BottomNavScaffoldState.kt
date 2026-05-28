package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState

/**
 * One destination of the bottom navigation bar. [key] is a stable identifier the host switches on
 * to render the matching content; [label] is the tab's accessibility description (the bar is
 * icon-only). The middle tab is host-supplied; Home and Settings are filled in by
 * [rememberBottomNavScaffoldState] from the toolkit's [dev.jdgarita.frnk.ui.theme.FrnkIcons] /
 * [dev.jdgarita.frnk.ui.theme.FrnkStrings] defaults.
 */
@Immutable
data class BottomNavTab(
    val key: String,
    val icon: ImageVector,
    val label: String,
)

/**
 * Configuration + runtime state for `BottomNavScaffold`.
 *
 * **Invariant:** [tabs] must be non-empty — a nav bar with no destinations has nothing to render or
 * select. Because mutations go through `copy(...)` (which re-runs `init`), the invariant holds for
 * the lifetime of the state.
 */
@Immutable
data class BottomNavScaffoldState(
    val tabs: List<BottomNavTab>,
    val selectedIndex: Int = 0,
) : UiState {
    init {
        require(tabs.isNotEmpty()) { "BottomNavScaffoldState requires at least one tab." }
    }

    /** The currently-selected tab, with [selectedIndex] clamped into range defensively. */
    val selectedTab: BottomNavTab get() = tabs[selectedIndex.coerceIn(0, tabs.lastIndex)]
}

sealed interface BottomNavIntent : UiIntent {
    data class TabSelected(
        val index: Int,
    ) : BottomNavIntent
}

sealed interface BottomNavEffect : UiEffect {
    /** Emitted whenever the selected tab actually changes — hosts react (navigate, log analytics). */
    data class TabSelected(
        val key: String,
        val index: Int,
    ) : BottomNavEffect
}
