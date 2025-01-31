package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.SemanticText
import dev.jdgarita.frnk.presentation.resources.SemanticTypography
import dev.jdgarita.frnk.presentation.resources.color.SemanticColor
import kotlinx.serialization.Serializable

@Serializable
data class FrnkTextViewState(
    override val id: String = ViewState.DEFAULT_ID,
    val semanticText: SemanticText,
    val typography: SemanticTypography,
    val color: SemanticColor,
    val skeleton: Boolean = false
) : ViewState {

    @Deprecated("Use constructor with semanticText instead.")
    constructor(
        id: String = ViewState.DEFAULT_ID,
        text: String,
        typography: SemanticTypography,
        color: SemanticColor,
        skeleton: Boolean = false
    ) : this(
        id = id,
        semanticText = SemanticText.Raw(text),
        typography = typography,
        color = color,
        skeleton = skeleton
    )

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

enum class FakeSwiftlyTextViewState : ViewStateFake<FrnkTextViewState> {
    Short,
    Medium,
    Long,
    Multiline,
    Skeleton;

    override val viewState: FrnkTextViewState
        get() =
            when (this) {
                Short -> FakeTextViewState.short
                Medium -> FakeTextViewState.medium
                Long -> FakeTextViewState.long
                Multiline -> FakeTextViewState.multiline
                Skeleton -> FakeTextViewState.skeleton
            }

    override val id: String get() = viewState.id
}

object FakeTextViewState {
    val short = FrnkTextViewState(
        id = "short",
        text = "Lorem",
        typography = SemanticTypography.BodyRegular,
        color = SemanticColor.LayoutOnSurface
    )

    val medium = FrnkTextViewState(
        id = "medium",
        text = "Lorem ipsum",
        typography = SemanticTypography.BodyRegular,
        color = SemanticColor.LayoutOnSurface
    )

    val long = FrnkTextViewState(
        id = "long",
        text = "Lorem ipsum dolor sit amet",
        typography = SemanticTypography.BodyRegular,
        color = SemanticColor.LayoutOnSurface
    )

    val multiline = FrnkTextViewState(
        id = "multiline",
        text = "Lorem ipsum dolor sit amet,\nconsectetur adipiscing elit.",
        typography = SemanticTypography.BodyRegular,
        color = SemanticColor.LayoutOnSurface
    )

    val skeleton = FrnkTextViewState.skeleton(id = "Skeleton")
}