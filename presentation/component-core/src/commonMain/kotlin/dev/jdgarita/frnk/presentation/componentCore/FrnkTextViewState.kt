package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.color.SemanticColor
import kotlinx.serialization.Serializable

@Serializable
data class FrnkTextViewState(
    override val id: String = ViewState.DEFAULT_ID,
    val text: String,
    val typography: SemanticTypography,
    val color: SemanticColor,
    val skeleton: Boolean = false
) : ViewState {

    companion object {
        fun skeleton(id: String = ""): FrnkTextViewState = FrnkTextViewState(
            id = id,
            text = "Lorem ipsum",
            typography = SemanticTypography.BodyRegular,
            color = SemanticColor.LayoutOnSurface,
            skeleton = true
        )
    }
}