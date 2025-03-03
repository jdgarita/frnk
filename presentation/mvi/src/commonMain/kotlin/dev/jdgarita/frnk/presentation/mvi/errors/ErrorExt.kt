package dev.jdgarita.frnk.presentation.mvi.errors

import dev.jdgarita.frnk.domain.framework.error.Error
import dev.jdgarita.frnk.presentation.componentCore.FrnkEmptyStateViewState
import dev.jdgarita.frnk.presentation.componentCore.InContentAlertViewState
import dev.jdgarita.frnk.presentation.mvi.CommonDisplayError
import dev.jdgarita.frnk.presentation.resources.FrnkStringProvider
import dev.jdgarita.frnk.presentation.resources.Strings
import dev.jdgarita.frnk.presentation.resources.color.SemanticColor
import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon

internal fun Error.asAlertDisplayError(
    stringProvider: FrnkStringProvider? = null
) = CommonDisplayError.Alert(
    alertViewState = InContentAlertViewState.Error(
        title = stringProvider?.string(Strings.common_error) ?: "generic error title",
        subtitle = stringProvider?.string(Strings.common_error_subtitle)
    )
)

fun Error.asEmptyStateDisplayError(
    stringProvider: FrnkStringProvider? = null
): CommonDisplayError.EmptyState {
    val (icon, textResId) = when (this) {
        Error.Network -> SemanticIcon.NoWifi to Strings.common_error_no_internet
        else -> SemanticIcon.Warning to Strings.common_error_refresh
    }

    return CommonDisplayError.EmptyState(
        emptyStateViewState = FrnkEmptyStateViewState(
            icon = icon,
            iconColor = SemanticColor.AdditionalFail,
            text = stringProvider?.string(textResId) ?: "generic error text"
        )
    )
}