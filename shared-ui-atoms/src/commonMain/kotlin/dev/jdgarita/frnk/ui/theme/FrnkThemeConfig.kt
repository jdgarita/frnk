package dev.jdgarita.frnk.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import com.composeunstyled.theme.ThemeToken

@Immutable
data class FrnkThemeConfig(
    val lightColorOverrides: Map<ThemeToken<Color>, Color> = emptyMap(),
    val darkColorOverrides: Map<ThemeToken<Color>, Color> = emptyMap(),
    val textStyleOverrides: Map<ThemeToken<TextStyle>, TextStyle> = emptyMap(),
    val shapeOverrides: Map<ThemeToken<Shape>, Shape> = emptyMap(),
    val stringOverrides: Map<ThemeToken<String>, String> = emptyMap(),
    val iconOverrides: Map<ThemeToken<ImageVector>, ImageVector> = emptyMap(),
)
