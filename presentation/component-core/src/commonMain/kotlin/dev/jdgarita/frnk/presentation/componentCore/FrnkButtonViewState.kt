package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlinx.serialization.Serializable

@Serializable
data class FrnkButtonViewState(
    override val id: String = ViewState.DEFAULT_ID,
    val content: FrnkButtonContent,
    val style: FrnkButtonStyle,
    val state: FrnkButtonState,
    val height: FrnkButtonHeight,
    val width: FrnkButtonWidth = FrnkButtonWidth.Fill,
    val isEnabled: Boolean = state in setOf(FrnkButtonState.Active, FrnkButtonState.Failed),
    val onClick: (() -> Unit)? = null
) : ViewState {

    companion object {
        fun skeleton(
            id: String = ViewState.DEFAULT_ID,
            height: FrnkButtonHeight = FrnkButtonHeight.Tall
        ): FrnkButtonViewState = FrnkButtonViewState(
            id = id,
            content = FrnkButtonContent.Text(""),
            style = FrnkButtonStyle.Skeleton,
            state = FrnkButtonState.Inactive,
            height = height
        )
    }
}

@Serializable
sealed class FrnkButtonContent {
    @Serializable
    data class Icon(
        val icon: FrnkButtonIcon
    ) : FrnkButtonContent()

    @Serializable
    data class Text(
        val text: String
    ) : FrnkButtonContent()

    @Serializable
    data class IconWithText(
        val icon: FrnkButtonIcon,
        val text: String
    ) : FrnkButtonContent()

    @Serializable
    data class AnimatedIcon(
        val iconState: FrnkAnimatedIconViewState
    ) : FrnkButtonContent()

    @Serializable
    data class AnimatedIconWithText(
        val iconState: FrnkAnimatedIconViewState,
        val iconOrientation: FrnkIconOrientation,
        val text: String
    ) : FrnkButtonContent()
}

enum class FrnkButtonStyle {
    Primary,
    Secondary,
    Tertiary,
    Clip,
    Skeleton
}

enum class FrnkButtonState {
    Active,
    Loading,
    Failed,
    Success,
    Inactive
}

enum class FrnkButtonHeight {
    Tall,
    Short
}

enum class FrnkIconOrientation {
    Leading,
    Trailing,
    None
}

@Serializable
data class FrnkButtonIcon(
    val icon: SemanticIcon,
    val orientation: FrnkIconOrientation
)

@Serializable
enum class FrnkButtonWidth {
    Fill,
    Hug
}

enum class FakeFrnkButtonViewState : ViewStateFake<FrnkButtonViewState> {
    PrimaryActiveTallFill,
    PrimaryActiveShortFill,
    PrimaryActiveShortFillIconText,
    PrimaryActiveTallHug,
    PrimaryLoading,
    PrimaryFailed,
    PrimarySuccess,
    PrimaryInactive,
    SecondaryActive,
    SecondaryLoading,
    SecondaryFailed,
    SecondarySuccess,
    SecondaryInactive,
    TertiaryActive,
    TertiaryActiveIcon,
    TertiaryLoading,
    TertiaryFailed,
    TertiarySuccess,
    TertiaryInactive,
    ClipActive,
    ClipLoading,
    ClipFailed,
    ClipSuccess,
    ClipInactive,
    Skeleton;

    override val viewState: FrnkButtonViewState get() =
        when (this) {
            PrimaryActiveTallFill -> FakeButtonViewState.primaryActiveTallFill
            PrimaryActiveShortFill -> FakeButtonViewState.primaryActiveShortFill
            PrimaryActiveShortFillIconText -> FakeButtonViewState.primaryActiveShortFillIconText
            PrimaryActiveTallHug -> FakeButtonViewState.primaryActiveTallHug
            PrimaryLoading -> FakeButtonViewState.primaryLoading
            PrimaryFailed -> FakeButtonViewState.primaryFailed
            PrimarySuccess -> FakeButtonViewState.primarySuccess
            PrimaryInactive -> FakeButtonViewState.primaryInactive
            SecondaryActive -> FakeButtonViewState.secondaryActive
            SecondaryLoading -> FakeButtonViewState.secondaryLoading
            SecondaryFailed -> FakeButtonViewState.secondaryFailed
            SecondarySuccess -> FakeButtonViewState.secondarySuccess
            SecondaryInactive -> FakeButtonViewState.secondaryInactive
            TertiaryActive -> FakeButtonViewState.tertiaryActive
            TertiaryActiveIcon -> FakeButtonViewState.tertiaryActiveIcon
            TertiaryLoading -> FakeButtonViewState.tertiaryLoading
            TertiaryFailed -> FakeButtonViewState.tertiaryFailed
            TertiarySuccess -> FakeButtonViewState.tertiarySuccess
            TertiaryInactive -> FakeButtonViewState.tertiaryInactive
            ClipActive -> FakeButtonViewState.clipActive
            ClipLoading -> FakeButtonViewState.clipLoading
            ClipFailed -> FakeButtonViewState.clipFailed
            ClipSuccess -> FakeButtonViewState.clipSuccess
            ClipInactive -> FakeButtonViewState.clipInactive
            Skeleton -> FakeButtonViewState.skeleton
        }

    override val id: String get() = viewState.id
}

object FakeButtonViewState {
    val primaryActiveTallFill = FrnkButtonViewState(
        id = "Primary Active Tall Fill",
        content = FrnkButtonContent.Text(text = "Primary Active"),
        style = FrnkButtonStyle.Primary,
        state = FrnkButtonState.Active,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val primaryActiveShortFill = FrnkButtonViewState(
        id = "Primary Active Short Fill",
        content = FrnkButtonContent.Text(text = "Primary Active"),
        style = FrnkButtonStyle.Primary,
        state = FrnkButtonState.Active,
        height = FrnkButtonHeight.Short,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val primaryActiveShortFillIconText = FrnkButtonViewState(
        id = "Primary Active Short Fill",
        content = FrnkButtonContent.IconWithText(
            icon = FrnkButtonIcon(
                icon = SemanticIcon.Add,
                orientation = FrnkIconOrientation.Leading
            ),
            text = "Add to Cart"
        ),
        style = FrnkButtonStyle.Primary,
        state = FrnkButtonState.Active,
        height = FrnkButtonHeight.Short,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val primaryActiveTallHug = FrnkButtonViewState(
        id = "Primary Active Tall Hug",
        content = FrnkButtonContent.Text(text = "Primary Active"),
        style = FrnkButtonStyle.Primary,
        state = FrnkButtonState.Active,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Hug,
        isEnabled = false,
        onClick = {}
    )

    val primaryLoading = FrnkButtonViewState(
        id = "Primary Loading Tall Fill",
        content = FrnkButtonContent.AnimatedIcon(
            iconState = FrnkAnimatedIconViewState(
                id = "id",
                iconSource = FrnkAnimatedIconSource.Semantic(
                    semanticIcon = SemanticIcon.Pending
                ),
                size = FrnkAnimatedIconSize.Medium,
                shape = FrnkAnimatedIconShape.Default
            )
        ),
        style = FrnkButtonStyle.Primary,
        state = FrnkButtonState.Loading,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val primaryFailed = FrnkButtonViewState(
        id = "Primary Failed Tall Fill",
        content = FrnkButtonContent.Text(text = "Primary Failed"),
        style = FrnkButtonStyle.Primary,
        state = FrnkButtonState.Failed,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val primarySuccess = FrnkButtonViewState(
        id = "Primary Success Tall Fill",
        content = FrnkButtonContent.Text(text = "Primary Success"),
        style = FrnkButtonStyle.Primary,
        state = FrnkButtonState.Success,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val primaryInactive = FrnkButtonViewState(
        id = "Primary Inactive Tall Fill",
        content = FrnkButtonContent.Text(text = "Primary Inactive"),
        style = FrnkButtonStyle.Primary,
        state = FrnkButtonState.Inactive,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val secondaryActive = FrnkButtonViewState(
        id = "Secondary Active Tall Fill",
        content = FrnkButtonContent.Text(text = "Secondary Active"),
        style = FrnkButtonStyle.Secondary,
        state = FrnkButtonState.Active,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val secondaryLoading = FrnkButtonViewState(
        id = "Secondary Loading Tall Fill",
        content = FrnkButtonContent.AnimatedIcon(
            iconState = FrnkAnimatedIconViewState(
                id = "id",
                iconSource = FrnkAnimatedIconSource.Semantic(
                    semanticIcon = SemanticIcon.Pending
                ),
                size = FrnkAnimatedIconSize.Medium,
                shape = FrnkAnimatedIconShape.Default
            )
        ),
        style = FrnkButtonStyle.Secondary,
        state = FrnkButtonState.Loading,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val secondaryFailed = FrnkButtonViewState(
        id = "Secondary Failed Tall Fill",
        content = FrnkButtonContent.Text(text = "Secondary Failed"),
        style = FrnkButtonStyle.Secondary,
        state = FrnkButtonState.Failed,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val secondarySuccess = FrnkButtonViewState(
        id = "Secondary Success Tall Fill",
        content = FrnkButtonContent.Text(text = "Secondary Success"),
        style = FrnkButtonStyle.Secondary,
        state = FrnkButtonState.Success,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val secondaryInactive = FrnkButtonViewState(
        id = "Secondary Inactive Tall Fill",
        content = FrnkButtonContent.Text(text = "Secondary Inactive"),
        style = FrnkButtonStyle.Secondary,
        state = FrnkButtonState.Inactive,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val tertiaryActive = FrnkButtonViewState(
        id = "Tertiary Active Tall Fill",
        content = FrnkButtonContent.Text(text = "Tertiary Active"),
        style = FrnkButtonStyle.Tertiary,
        state = FrnkButtonState.Active,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val tertiaryActiveIcon = FrnkButtonViewState(
        id = "Tertiary Active Tall Fill Icon",
        content = FrnkButtonContent.Icon(
            icon = FrnkButtonIcon(
                icon = SemanticIcon.Add,
                orientation = FrnkIconOrientation.None
            )
        ),
        style = FrnkButtonStyle.Tertiary,
        state = FrnkButtonState.Active,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val tertiaryLoading = FrnkButtonViewState(
        id = "Tertiary Loading Tall Fill",
        content = FrnkButtonContent.AnimatedIcon(
            iconState = FrnkAnimatedIconViewState(
                id = "id",
                iconSource = FrnkAnimatedIconSource.Semantic(
                    semanticIcon = SemanticIcon.Pending
                ),
                size = FrnkAnimatedIconSize.Medium,
                shape = FrnkAnimatedIconShape.Default
            )
        ),
        style = FrnkButtonStyle.Tertiary,
        state = FrnkButtonState.Loading,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val tertiaryFailed = FrnkButtonViewState(
        id = "Tertiary Failed Tall Fill",
        content = FrnkButtonContent.Text(text = "Tertiary Failed"),
        style = FrnkButtonStyle.Tertiary,
        state = FrnkButtonState.Failed,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val tertiarySuccess = FrnkButtonViewState(
        id = "Tertiary Success Tall Fill",
        content = FrnkButtonContent.Text(text = "Tertiary Success"),
        style = FrnkButtonStyle.Tertiary,
        state = FrnkButtonState.Success,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val tertiaryInactive = FrnkButtonViewState(
        id = "Tertiary Inactive Tall Fill",
        content = FrnkButtonContent.Text(text = "Tertiary Inactive"),
        style = FrnkButtonStyle.Tertiary,
        state = FrnkButtonState.Inactive,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val clipActive = FrnkButtonViewState(
        id = "Clip Active Tall Fill",
        content = FrnkButtonContent.IconWithText(
            icon = FrnkButtonIcon(
                icon = SemanticIcon.Clip,
                orientation = FrnkIconOrientation.Leading
            ),
            text = "Clip Active"
        ),
        style = FrnkButtonStyle.Clip,
        state = FrnkButtonState.Active,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val clipLoading = FrnkButtonViewState(
        id = "Clip Loading Tall Fill",
        content = FrnkButtonContent.AnimatedIcon(
            iconState = FrnkAnimatedIconViewState(
                id = "id",
                iconSource = FrnkAnimatedIconSource.Semantic(
                    semanticIcon = SemanticIcon.Clip
                ),
                size = FrnkAnimatedIconSize.Medium,
                shape = FrnkAnimatedIconShape.Default
            )
        ),
        style = FrnkButtonStyle.Clip,
        state = FrnkButtonState.Loading,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val clipFailed = FrnkButtonViewState(
        id = "Clip Failed Tall Fill",
        content = FrnkButtonContent.Text(text = "Clip Failed"),
        style = FrnkButtonStyle.Clip,
        state = FrnkButtonState.Failed,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val clipSuccess = FrnkButtonViewState(
        id = "Clip Success Tall Fill",
        content = FrnkButtonContent.Text(text = "Clip Success"),
        style = FrnkButtonStyle.Clip,
        state = FrnkButtonState.Success,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val clipInactive = FrnkButtonViewState(
        id = "Clip Inactive Tall Fill",
        content = FrnkButtonContent.IconWithText(
            icon = FrnkButtonIcon(
                icon = SemanticIcon.Clip,
                orientation = FrnkIconOrientation.Leading
            ),
            text = "Clip Inactive"
        ),
        style = FrnkButtonStyle.Clip,
        state = FrnkButtonState.Inactive,
        height = FrnkButtonHeight.Tall,
        width = FrnkButtonWidth.Fill,
        isEnabled = false,
        onClick = {}
    )

    val skeleton = FrnkButtonViewState.skeleton(
        id = "Skeleton Tall",
        height = FrnkButtonHeight.Tall
    )
}