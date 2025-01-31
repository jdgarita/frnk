package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.color.SemanticColor
import kotlinx.serialization.Serializable

@Serializable
data class FrnkSpinnerViewState(
    override val id: String = ViewState.UNIQUE_ID
) : ViewState {
    val backgroundColor: SemanticColor = SemanticColor.TheVoid
    val backgroundOpacity: Double = 0.4
    val spinnerColor: SemanticColor = SemanticColor.LayoutLightbox
}

enum class FakeSwiftlySpinnerViewState : ViewStateFake<FrnkSpinnerViewState> {
    Spinner;

    override val viewState: FrnkSpinnerViewState = FrnkSpinnerViewState("Spinner")
    override val id: String
        get() = viewState.id
}