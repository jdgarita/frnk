package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.SemanticTypography
import dev.jdgarita.frnk.presentation.resources.color.SemanticColor
import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlinx.serialization.Serializable

@Serializable
data class FrnkIconViewState(
    override val id: String = ViewState.UNIQUE_ID,
    val icon: SemanticIcon,
    val typography: SemanticTypography,
    val foregroundColor: SemanticColor
) : ViewState