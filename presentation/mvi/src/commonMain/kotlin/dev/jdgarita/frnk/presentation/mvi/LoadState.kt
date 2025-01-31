package dev.jdgarita.frnk.presentation.mvi

import kotlinx.serialization.Serializable

@Serializable
sealed class LoadState {
    @Serializable
    data object Initialized : LoadState()

    @Serializable
    data class Loading(val isRefreshing: Boolean) : LoadState()

    @Serializable
    data object Loaded : LoadState()

    @Serializable
    data object Failed : LoadState()
}