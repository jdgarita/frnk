package dev.jdgarita.frnk.presentation.componentCore

import dev.jdgarita.frnk.presentation.resources.images.SemanticIcon
import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName
import kotlinx.serialization.Serializable

@Serializable
data class FrnkAnimatedIconViewState(
    override val id: String,
    val iconSource: FrnkAnimatedIconSource,
    val size: FrnkAnimatedIconSize,
    val shape: FrnkAnimatedIconShape = FrnkAnimatedIconShape.Default
) : ViewState

@OptIn(ExperimentalObjCName::class)
enum class FrnkAnimatedIconShape {
    @ObjCName("default")
    Default,

    @ObjCName("roundedCorners")
    RoundedCorners,

    @ObjCName("circular")
    Circular
}

@OptIn(ExperimentalObjCName::class)
enum class FrnkAnimatedIconSize {
    @ObjCName("xSmall")
    XSmall,

    @ObjCName("small")
    Small,

    @ObjCName("medium")
    Medium,

    @ObjCName("large")
    Large,

    @ObjCName("xLarge")
    XLarge
}

@OptIn(ExperimentalObjCName::class)
@Serializable
sealed class FrnkAnimatedIconSource {

    @ObjCName("semantic")
    @Serializable
    data class Semantic(val semanticIcon: SemanticIcon) : FrnkAnimatedIconSource()

    @ObjCName("remote")
    @Serializable
    data class Remote(val urlString: String) : FrnkAnimatedIconSource()
}