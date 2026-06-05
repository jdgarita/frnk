package dev.jdgarita.frnk.ui.theme

import androidx.compose.foundation.Indication
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
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
     * Per-token overrides for the spacing/padding axis (`Theme[spacing][spacingMd]`, …). Defaults
     * come from `FrnkSpacing`; supply only the tokens you want to restyle. Lets a host rescale the
     * toolkit's whitespace (e.g. a denser layout) without forking atoms.
     */
    val spacingOverrides: Map<ThemeToken<Dp>, Dp> = emptyMap(),
    /**
     * Per-token overrides for the icon-size axis (`Theme[iconSizes][iconSizeMd]`, …). Defaults come
     * from `FrnkIconSize`; supply only the tokens you want to restyle.
     */
    val iconSizeOverrides: Map<ThemeToken<Dp>, Dp> = emptyMap(),
    /**
     * Host-supplied tokens for the optional `shadows` axis (`Theme[shadows][...]`). The toolkit's own
     * atoms don't use elevation shadows, so this axis ships empty — a host with an elevation/shadow
     * design language registers its own `ThemeToken<Shadow>` tokens here and reads them back under
     * [FrnkTheme]. Tokens are matched by name (compose-unstyled `ThemeToken` is a by-name data class).
     */
    val shadowOverrides: Map<ThemeToken<Shadow>, Shadow> = emptyMap(),
    /**
     * Host-supplied tokens for the optional `indications` axis (`Theme[indications][...]`). Separate
     * from [indication] (the single ambient `LocalIndication`): this lets a host register *named*
     * indication variants (e.g. a bright vs. dim ripple) and select them per call site. Ships empty.
     */
    val indicationOverrides: Map<ThemeToken<Indication>, Indication> = emptyMap(),
    /**
     * When non-null, every bundled text style is rebuilt with this [FontFamily] before the host
     * [textStyleOverrides] are applied. Avoids forcing hosts to redeclare 15 text-style tokens
     * just to swap a typeface.
     */
    val fontFamily: FontFamily? = null,
    /**
     * The press-feedback [Indication] installed as the ambient `LocalIndication` for the whole theme,
     * so every interactive atom (and any host `Modifier.clickable` under [FrnkTheme]) reacts to touch.
     * `null` (the default) uses the bundled Frnk ripple ([rememberFrnkRipple]). Pass a custom
     * [Indication] to restyle it, or a no-op [Indication] to disable press feedback entirely. Hosts
     * can also scope `CompositionLocalProvider(LocalIndication provides …)` around a subtree instead.
     */
    val indication: Indication? = null,
) {
    companion object {
        val Default = FrnkThemeConfig()
    }
}
