package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.SemanticTypography
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

enum class FakeSwiftlyEmptyStateViewState : ViewStateFake<FrnkEmptyStateViewState> {
    NoAds,
    NoInternet,
    SomethingWrong;

    override val viewState: FrnkEmptyStateViewState get() =
        when (this) {
            NoAds -> FakeEmptyStateViewState.noAds
            NoInternet -> FakeEmptyStateViewState.noInternet
            SomethingWrong -> FakeEmptyStateViewState.somethingWrong
        }

    override val id: String get() = viewState.id
}

object FakeEmptyStateViewState {
    val noAds = FrnkEmptyStateViewState(
        id = "No Ads",
        icon = SemanticIcon.Added,
        iconColor = SemanticColor.AccentsAccent,
        title = "Title",
        text = "Text"
    )

    val noInternet = FrnkEmptyStateViewState(
        id = "No Internet",
        icon = SemanticIcon.NoWifi,
        iconColor = SemanticColor.AdditionalFail,
        title = "Title",
        text = "Text"
    )

    val somethingWrong = FrnkEmptyStateViewState(
        id = "Something Wrong",
        icon = SemanticIcon.Warning,
        iconColor = SemanticColor.AdditionalFail,
        title = "Title",
        text = "Text"
    )
}