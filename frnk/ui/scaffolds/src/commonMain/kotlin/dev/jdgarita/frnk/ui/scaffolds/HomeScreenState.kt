package dev.jdgarita.frnk.ui.scaffolds

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarAction
import dev.jdgarita.frnk.ui.atoms.FrnkTopAppBarState
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState

/**
 * State for [HomeScreen] — the toolkit's home-tab page template: a [FrnkTopAppBar][topBar] pinned
 * over a vertically scrollable column the host fills through the screen's `content` slot.
 *
 * Skeleton decision (recorded): **non-sealed, no `Skeleton` object** — like [SettingsScreenState] /
 * [OnboardingScreenState] this is screen-template *chrome*, not a content-bearing atom. Loading
 * visuals belong to the host's slot content, which uses the atoms' own sealed `Skeleton` states.
 *
 * @param topBar the pinned top bar (title, optional navigation icon, trailing actions, search mode).
 */
@Immutable
data class HomeScreenState(
    val topBar: FrnkTopAppBarState,
) : UiState

sealed interface HomeIntent : UiIntent {
    /** A trailing top-bar action was tapped. */
    data class TopBarActionClicked(
        val action: FrnkTopAppBarAction,
    ) : HomeIntent

    /** The leading top-bar navigation icon was tapped. */
    data object NavigationClicked : HomeIntent

    /**
     * The host recomputed the chrome (e.g. a top-bar action that appears only while Free) and handed
     * down a fresh [newState]. The VM adopts it wholesale — [HomeScreenState] is pure chrome with no
     * VM-owned interaction state to preserve. Mirrors [SettingsIntent.ConfigChanged]; replaces the old
     * "re-key the ViewModel to re-seed it" approach.
     */
    data class ConfigChanged(
        val newState: HomeScreenState,
    ) : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    /** Re-emission of [HomeIntent.TopBarActionClicked]; [key] is the action's stable key. */
    data class ActionInvoked(
        val key: String,
    ) : HomeEffect

    /** Re-emission of [HomeIntent.NavigationClicked]. */
    data object NavigationInvoked : HomeEffect
}
