package dev.jdgarita.frnk.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import dev.jdgarita.frnk.presentation.PresentationConstants

/**
 * @author Vivien Mahe
 * @since 23/08/2023
 */

val Shapes = Shapes(
    small = RoundedCornerShape(PresentationConstants.Size.Shape.Small),
    medium = RoundedCornerShape(PresentationConstants.Size.Shape.Medium),
    large = RoundedCornerShape(PresentationConstants.Size.Shape.Large),
    extraLarge = RoundedCornerShape(PresentationConstants.Size.Shape.ExtraLarge)
)
