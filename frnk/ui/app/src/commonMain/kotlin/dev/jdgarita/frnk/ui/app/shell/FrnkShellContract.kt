package dev.jdgarita.frnk.ui.app.shell

import androidx.compose.runtime.Immutable
import dev.jdgarita.frnk.ui.mvi.Arguments
import dev.jdgarita.frnk.ui.mvi.ModelState
import dev.jdgarita.frnk.ui.mvi.ModelStateFactory
import dev.jdgarita.frnk.ui.mvi.UiEffect
import dev.jdgarita.frnk.ui.mvi.UiIntent
import dev.jdgarita.frnk.ui.mvi.UiState

/*
 * The MVI contract of [FrnkShellViewModel]. Internal: hosts drive the shell only through
 * [FrnkShellNavigator], so none of these types reach the public API.
 */

/** The shell takes no runtime inputs; its sheets and routes are composable parameters. */
internal data object FrnkShellArguments : Arguments

/**
 * @param presented the keys of the sheets currently up.
 * @param presentations how many times each sheet has been presented; the shell keys a sheet's content
 *   on it, so every presentation gets a fresh ViewModel store.
 */
@Immutable
internal data class FrnkShellModelState(
    val presented: Set<String>,
    val presentations: Map<String, Int>
) : ModelState

internal object FrnkShellModelStateFactory : ModelStateFactory<FrnkShellModelState> {
    override fun initialModelState() = FrnkShellModelState(presented = emptySet(), presentations = emptyMap())
}

/** What the shell renders: which sheets are up, and which presentation each one's content belongs to. */
@Immutable
internal data class FrnkShellState(
    private val presented: Set<String>,
    private val presentations: Map<String, Int>
) : UiState {
    fun isVisible(key: String): Boolean = key in presented

    /** 0 until the sheet is first presented; bumped by every present, never by a dismiss. */
    fun presentation(key: String): Int = presentations[key] ?: 0
}

internal sealed interface FrnkShellIntent : UiIntent {
    /** Raise the sheet under [key]. A no-op while it is already up. */
    data class Present(
        val key: String
    ) : FrnkShellIntent

    /** Take the sheet under [key] down. Idempotent: a single close can arrive more than once. */
    data class Dismiss(
        val key: String
    ) : FrnkShellIntent
}

/** The shell emits no one-shot effects; its navigator acts on the back stack and the model directly. */
internal sealed interface FrnkShellEffect : UiEffect