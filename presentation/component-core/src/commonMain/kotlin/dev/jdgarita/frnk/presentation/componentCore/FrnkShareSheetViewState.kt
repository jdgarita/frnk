package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.SemanticText
import dev.jdgarita.frnk.presentation.resources.SemanticTypography
import dev.jdgarita.frnk.presentation.resources.color.SemanticColor
import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlinx.serialization.Serializable
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@Serializable
@OptIn(ExperimentalObjCName::class)
sealed class FrnkShareSheetViewState : ViewState {
    abstract val labelStyle: FrnkShareSheetLabelStyle
    abstract val onShareTap: () -> Unit

    @Serializable
    @ObjCName("sharedText")
    data class SharedText(
        override val id: String = ViewState.UNIQUE_ID,
        val previewTitle: String,
        val text: String,
        val image: FrnkImageSource.Semantic? = null,
        override val labelStyle: FrnkShareSheetLabelStyle = FrnkShareSheetLabelStyle.Icon(),
        override val onShareTap: () -> Unit = {}
    ) : FrnkShareSheetViewState()

    @Serializable
    @ObjCName("sharedImage")
    data class SharedImage(
        override val id: String = ViewState.UNIQUE_ID,
        val previewTitle: String,
        val image: FrnkImageSource,
        override val labelStyle: FrnkShareSheetLabelStyle = FrnkShareSheetLabelStyle.Icon(),
        override val onShareTap: () -> Unit = {}
    ) : FrnkShareSheetViewState()

    @Serializable
    @ObjCName("sharedLink")
    data class SharedLink(
        override val id: String = ViewState.UNIQUE_ID,
        val url: String,
        val previewTitle: String? = null,
        val image: FrnkImageSource.Semantic,
        override val labelStyle: FrnkShareSheetLabelStyle = FrnkShareSheetLabelStyle.Icon(),
        override val onShareTap: () -> Unit = {}
    ) : FrnkShareSheetViewState()
}

@Serializable
sealed class FrnkShareSheetLabelStyle {
    data class Icon(
        private val icon: SemanticIcon? = null
    ) : FrnkShareSheetLabelStyle() {
        val iconViewState: FrnkIconViewState
            get() =
                FrnkIconViewState(
                    icon = icon ?: SemanticIcon.Share,
                    typography = SemanticTypography.BodyRegular,
                    foregroundColor = SemanticColor.TopNavBarActionOnTopNavBar
                )
    }

    data class Text(
        private val text: String
    ) : FrnkShareSheetLabelStyle() {
        val textViewState: FrnkTextViewState
            get() =
                FrnkTextViewState(
                    id = ViewState.UNIQUE_ID,
                    semanticText = SemanticText.Raw(text = text),
                    typography = SemanticTypography.BodyRegular,
                    color = SemanticColor.LayoutOnSurface,
                    skeleton = false
                )
    }
}

enum class FakeFrnkShareSheetViewState : ViewStateFake<FrnkShareSheetViewState> {
    SharedTextIcon,
    SharedTextText;

    override val viewState: FrnkShareSheetViewState
        get() =
            when (this) {
                SharedTextIcon -> FakeShareSheetViewState.sharedTextIcon
                SharedTextText -> FakeShareSheetViewState.sharedTextText
            }

    override val id: String get() = viewState.id
}

object FakeShareSheetViewState {
    val sharedTextIcon = FrnkShareSheetViewState.SharedText(
        id = "Shared Text Icon",
        previewTitle = "Preview Title",
        text = "Text to share.",
        labelStyle = FrnkShareSheetLabelStyle.Icon()
    )

    val sharedTextText = FrnkShareSheetViewState.SharedText(
        id = "Shared Text Text",
        previewTitle = "Preview Title",
        text = "Text to share.",
        labelStyle = FrnkShareSheetLabelStyle.Text(text = "Share Your List")
    )
}