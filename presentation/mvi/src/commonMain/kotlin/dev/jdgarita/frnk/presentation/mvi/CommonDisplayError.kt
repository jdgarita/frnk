package dev.jdgarita.frnk.presentation.mvi

import dev.jdgarita.frnk.presentation.componentCore.FrnkEmptyStateViewState
import kotlinx.serialization.Serializable

@Serializable
sealed class CommonDisplayError {

    @Serializable
    data class EmptyState(
        val emptyStateViewState: FrnkEmptyStateViewState
    ) : CommonDisplayError()
}