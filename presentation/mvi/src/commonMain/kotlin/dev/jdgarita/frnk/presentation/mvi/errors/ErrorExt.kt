package dev.jdgarita.frnk.presentation.mvi.errors

import dev.jdgarita.frnk.domain.framework.error.Error
import dev.jdgarita.frnk.presentation.componentCore.FrnkEmptyStateViewState
import dev.jdgarita.frnk.presentation.mvi.CommonDisplayError
import dev.jdgarita.frnk.presentation.resources.color.SemanticColor
import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import dev.jdgarita.frnk.presentation.resources.text.Strings

fun Error.asEmptyStateDisplayError(): CommonDisplayError.EmptyState {
    val (icon, textResId) = when (this) {
        Error.Network -> SemanticIcon.NoWifi to Strings.common_error_no_internet
        else -> SemanticIcon.Warning to Strings.common_error_refresh
    }

    return CommonDisplayError.EmptyState(
        emptyStateViewState = FrnkEmptyStateViewState(
            icon = icon,
            iconColor = SemanticColor.AdditionalFail,
            text = "generic error text"
        )
    )
}