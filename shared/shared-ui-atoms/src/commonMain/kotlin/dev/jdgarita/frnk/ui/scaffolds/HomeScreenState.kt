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
 * @param primaryActionEnabled when `true`, the screen claims the bottom bar's primary-action button
 *   (via `FrnkPrimaryActionHandler`) while it is the active destination — taps surface as
 *   [HomeIntent.PrimaryActionClicked] → [HomeEffect.PrimaryActionInvoked]. Requires the hosting
 *   `FrnkTabbedNavScaffold` to carry a `primaryActionRegistry` (and the `AdaptiveNavBar` engine to
 *   actually render the button).
 */
@Immutable
data class HomeScreenState(
    val topBar: FrnkTopAppBarState,
    val primaryActionEnabled: Boolean = false,
) : UiState

sealed interface HomeIntent : UiIntent {
    /** A trailing top-bar action was tapped. */
    data class TopBarActionClicked(
        val action: FrnkTopAppBarAction,
    ) : HomeIntent

    /** The leading top-bar navigation icon was tapped. */
    data object NavigationClicked : HomeIntent

    /** The bottom bar's primary-action button was tapped while this screen held the claim. */
    data object PrimaryActionClicked : HomeIntent
}

sealed interface HomeEffect : UiEffect {
    /** Re-emission of [HomeIntent.TopBarActionClicked]; [key] is the action's stable key. */
    data class ActionInvoked(
        val key: String,
    ) : HomeEffect

    /** Re-emission of [HomeIntent.NavigationClicked]. */
    data object NavigationInvoked : HomeEffect

    /** Re-emission of [HomeIntent.PrimaryActionClicked] — the host decides what "Create/Add" means. */
    data object PrimaryActionInvoked : HomeEffect
}
