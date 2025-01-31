package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlinx.serialization.Serializable

@Serializable
sealed class FrnkTopBarViewState : ViewState {
    override val id: String = ViewState.DEFAULT_ID
    abstract val trailingContent: List<TopBarTrailingContent>?
    abstract val frnkDropdownMenuViewState: FrnkDropdownMenuViewState?

    @Serializable
    data class Simple(
        val title: String,
        val backButton: Boolean = false,
        val backButtonStyle: BackButtonStyle = BackButtonStyle.Back,
        val onBackButtonClick: () -> Unit = {},
        override val trailingContent: List<TopBarTrailingContent>? = null,
        override val frnkDropdownMenuViewState: FrnkDropdownMenuViewState? = null
    ) : FrnkTopBarViewState()

    @Serializable
    data class WithLogo(
        override val trailingContent: List<TopBarTrailingContent>?,
        override val frnkDropdownMenuViewState: FrnkDropdownMenuViewState? = null
    ) : FrnkTopBarViewState() {
        val logo: FrnkLogoViewState = FrnkLogoViewState(logoType = FrnkLogoType.Standard)
    }
}

@Serializable
sealed class TopBarTrailingContent {

    abstract val onTap: (id: String) -> Unit

    @Serializable
    data class Icon(
        val icon: SemanticIcon,
        override val onTap: (id: String) -> Unit = {}
    ) : TopBarTrailingContent()

    @Serializable
    data class Text(
        val text: String,
        override val onTap: (id: String) -> Unit = {}
    ) : TopBarTrailingContent()

    data class ShareSheet(
        val swiftlyShareSheetViewState: FrnkShareSheetViewState,
        override val onTap: (id: String) -> Unit = {}
    ) : TopBarTrailingContent()
}

enum class BackButtonStyle {
    Back,
    Close
}