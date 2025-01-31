package dev.jdgarita.frnk.presentation.componentCore

import kotlinx.serialization.Serializable

@Serializable
data class FrnkCloseButtonViewState(
    override val id: String = ViewState.DEFAULT_ID,
    val buttonType: FrnkCloseButtonType,
    val clickAction: () -> Unit = {}
) : ViewState

@Serializable
sealed class FrnkCloseButtonType {
    data object OnLight : FrnkCloseButtonType()
    data object OnDark : FrnkCloseButtonType()
}