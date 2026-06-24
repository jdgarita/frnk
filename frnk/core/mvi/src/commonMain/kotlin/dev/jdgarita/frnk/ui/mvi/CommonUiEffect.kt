package dev.jdgarita.frnk.ui.mvi

/**
 * Cross-cutting [UiEffect]s the [MviViewModel] base emits — the counterpart to [CommonUiIntent], so a
 * single effect collector above the screen can handle them (e.g. pop the back stack) uniformly.
 */
sealed class CommonUiEffect : UiEffect {
    /** Emitted in response to [CommonUiIntent.OnBackPressed]; [screenId] optionally tags the origin. */
    data class DidPressBack(
        val screenId: String? = null
    ) : CommonUiEffect()
}