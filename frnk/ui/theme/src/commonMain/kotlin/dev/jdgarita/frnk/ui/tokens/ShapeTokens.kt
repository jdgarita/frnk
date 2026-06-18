package dev.jdgarita.frnk.ui.tokens

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object FrnkShapes {
    val none: Shape = RectangleShape
    val extraSmall: Shape = RoundedCornerShape(4.dp)
    val small: Shape = RoundedCornerShape(8.dp)
    val medium: Shape = RoundedCornerShape(12.dp)
    val large: Shape = RoundedCornerShape(16.dp)
    val extraLarge: Shape = RoundedCornerShape(24.dp)
    val full: Shape = CircleShape

    val button: Shape = medium
    val card: Shape = medium
    val textField: Shape = small
    val bottomSheet: Shape = large
}