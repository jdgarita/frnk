package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.color.SemanticColor
import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlinx.serialization.Serializable

@Serializable
data class FrnkDropdownMenuViewState(
    override val id: String = ViewState.DEFAULT_ID,
    val expanded: Boolean,
    val onDismissRequest: () -> Unit,
    val items: List<DropdownMenuItem>
) : ViewState

@Serializable
sealed class DropdownMenuItem : ViewState {
    override val id: String = ViewState.DEFAULT_ID
    open val labelText: String = ""
    open val labelColor: SemanticColor? = null
    open val icon: SemanticIcon? = null
    open val iconColor: SemanticColor? = null
    open val tapAction: () -> Unit = {}

    data class Destructive(
        override val id: String = ViewState.DEFAULT_ID,
        override val icon: SemanticIcon? = null,
        override val iconColor: SemanticColor? = null,
        override val labelText: String,
        override val labelColor: SemanticColor? = null,
        override val tapAction: () -> Unit = {}
    ) : DropdownMenuItem()
    data class None(
        override val id: String = ViewState.DEFAULT_ID,
        override val icon: SemanticIcon? = null,
        override val iconColor: SemanticColor? = null,
        override val labelText: String,
        override val labelColor: SemanticColor? = null,
        override val tapAction: () -> Unit = {}
    ) : DropdownMenuItem()

    data class DividerItem(
        override val id: String = ViewState.DEFAULT_ID
    ) : DropdownMenuItem()
}