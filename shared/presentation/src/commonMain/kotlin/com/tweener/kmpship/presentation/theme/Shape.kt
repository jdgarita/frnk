package com.tweener.kmpship.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import com.tweener.kmpship.presentation.PresentationConstants

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
