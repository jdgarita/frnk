package dev.garita.frnk.ui.componentLibrary

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape

interface ComposeFrnkShape {

    val roundedCornerStandard: Shape
    val roundedCornerEditorial: Shape
    val roundedCornerMini: Shape
    val roundedCornerLarge: Shape

    companion object {

        val default: ComposeFrnkShape by lazy(mode = LazyThreadSafetyMode.NONE) { default() }

        fun default(
            standardRoundedCorner: Shape = RoundedCornerShape(FrnkRadius.Standard),
            roundedCornerEditorial: Shape = RoundedCornerShape(FrnkRadius.Editorial),
            roundedCornerMini: Shape = RoundedCornerShape(FrnkRadius.Mini),
            roundedCornerLarge: Shape = RoundedCornerShape(FrnkRadius.Large)
        ): ComposeFrnkShape = ComposeFrnkShapeData(
            roundedCornerStandard = standardRoundedCorner,
            roundedCornerEditorial = roundedCornerEditorial,
            roundedCornerMini = roundedCornerMini,
            roundedCornerLarge = roundedCornerLarge
        )
    }
}

private data class ComposeFrnkShapeData(
    override val roundedCornerStandard: Shape,
    override val roundedCornerEditorial: Shape,
    override val roundedCornerMini: Shape,
    override val roundedCornerLarge: Shape
) : ComposeFrnkShape