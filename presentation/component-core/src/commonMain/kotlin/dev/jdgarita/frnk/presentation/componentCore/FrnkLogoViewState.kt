package dev.jdgarita.frnk.presentation.componentCore

import kotlinx.serialization.Serializable

@Serializable
data class FrnkLogoViewState(
    override val id: String = ViewState.DEFAULT_ID,
    val logoType: FrnkLogoType
) : ViewState

@Serializable
sealed class FrnkLogoType {
    data object Standard : FrnkLogoType()
    data object Large : FrnkLogoType()
}