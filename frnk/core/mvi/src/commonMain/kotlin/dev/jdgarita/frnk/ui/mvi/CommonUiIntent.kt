package dev.jdgarita.frnk.ui.mvi

/**
 * Common [Intent]s that can be used by all [MviViewModel]s.
 */
sealed class CommonUiIntent : UiIntent {
    /**
     * User pressed back.
     */
    data object OnBackPressed : CommonUiIntent()
}