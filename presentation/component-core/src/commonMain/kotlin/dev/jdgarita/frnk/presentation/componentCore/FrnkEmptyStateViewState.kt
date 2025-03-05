package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.color.SemanticColor
import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlinx.serialization.Serializable

@Serializable
data class FrnkEmptyStateViewState(
    override val id: String = ViewState.DEFAULT_ID,
    private val icon: SemanticIcon,
    private val iconColor: SemanticColor,
    private val title: String? = null,
    private val text: String
) : ViewState {
    val iconState = FrnkIconViewState(
        id = id,
        icon = icon,
        typography = SemanticTypography.DisplayLargeSemibold,
        foregroundColor = iconColor
    )

    val titleState: FrnkTextViewState? = title?.let {
        FrnkTextViewState(
            text = it,
            typography = SemanticTypography.DisplaySemibold,
            color = SemanticColor.LayoutOnSurface
        )
    }

    val textState = FrnkTextViewState(
        text = text,
        typography = SemanticTypography.BodyRegular,
        color = SemanticColor.LayoutOnSurface
    )
}