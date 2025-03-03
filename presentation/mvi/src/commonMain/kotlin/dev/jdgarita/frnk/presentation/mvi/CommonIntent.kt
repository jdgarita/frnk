package dev.jdgarita.frnk.presentation.mvi

/**
 * Common [Intent]s that can be used by all [MviViewModel]s.
 */
sealed class CommonIntent : Intent {

    /**
     *  Refresh data.
     */
    data object OnRefresh : CommonIntent()

    /**
     * User pressed back.
     */
    data object OnBackPressed : CommonIntent()
}