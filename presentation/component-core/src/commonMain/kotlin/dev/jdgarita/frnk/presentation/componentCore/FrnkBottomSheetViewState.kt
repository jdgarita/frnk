package dev.jdgarita.frnk.presentation.componentCore

import kotlinx.serialization.Serializable

@Serializable
data class FrnkBottomSheetViewState(
    override val id: String,
    val title: String? = null,
    val closeButton: FrnkCloseButtonViewState? = null,
    val onDismissRequest: () -> Unit = {},
    val frnkShareSheetViewState: FrnkShareSheetViewState? = null
) : ViewState