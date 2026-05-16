package dev.jdgarita.frnk.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import com.composeunstyled.theme.ThemeToken

/**
 * Host-supplied overrides for the Frnk design system. Every field is optional; empty maps and a
 * `null` font family mean "use the bundled default". Per-axis maps are merged on top of defaults
 * with `Map.plus` semantics — host values win per token.
 *
 * Prefer [Default] over the no-arg constructor at call sites to avoid allocating a fresh instance
 * on every recomposition.
 */
@Immutable
data class FrnkThemeConfig(
    val lightColorOverrides: Map<ThemeToken<Color>, Color> = emptyMap(),
    val darkColorOverrides: Map<ThemeToken<Color>, Color> = emptyMap(),
    val textStyleOverrides: Map<ThemeToken<TextStyle>, TextStyle> = emptyMap(),
    val shapeOverrides: Map<ThemeToken<Shape>, Shape> = emptyMap(),
    val stringOverrides: Map<ThemeToken<String>, String> = emptyMap(),
    val iconOverrides: Map<ThemeToken<ImageVector>, ImageVector> = emptyMap(),
    /**
     * When non-null, every bundled text style is rebuilt with this [FontFamily] before the host
     * [textStyleOverrides] are applied. Avoids forcing hosts to redeclare 15 text-style tokens
     * just to swap a typeface.
     */
    val fontFamily: FontFamily? = null,
) {
    companion object {
        val Default = FrnkThemeConfig()
    }
}
